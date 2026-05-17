package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.isbn.buildIsbnIdentifiers
import com.sergebailes.bookbee.domain.isbn.parseIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.WishlistItem
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import java.time.Instant
import java.util.UUID

data class SaveWishlistItemCommand(
    val userId: UUID,
    val wishlistItemId: UUID? = null,
    val title: String,
    val author: String,
    val notes: String,
    val isbn: String,
    val allowOwnedOverlap: Boolean = false,
)

sealed interface SaveWishlistItemResult {
    data class Success(
        val wishlistItemId: UUID,
    ) : SaveWishlistItemResult

    data class ValidationFailed(
        val titleError: String? = null,
        val isbnError: String? = null,
    ) : SaveWishlistItemResult

    data class RequiresOwnedOverlapConfirmation(
        val title: String,
        val authorLine: String?,
    ) : SaveWishlistItemResult

    data object WishlistItemNotFound : SaveWishlistItemResult
}

class SaveWishlistItemUseCase(
    private val shelfRepository: ShelfRepository,
    private val wishlistRepository: WishlistRepository,
    private val clock: () -> Instant = Instant::now,
    private val idProvider: () -> UUID = UUID::randomUUID,
) {
    suspend operator fun invoke(command: SaveWishlistItemCommand): SaveWishlistItemResult {
        val trimmedTitle = command.title.trim()
        if (trimmedTitle.isBlank()) {
            return SaveWishlistItemResult.ValidationFailed(
                titleError = "Title is required",
            )
        }

        val validatedIsbn = command.isbn.trim()
            .takeIf(String::isNotBlank)
            ?.let(::parseIsbn)
        if (command.isbn.isNotBlank() && validatedIsbn == null) {
            return SaveWishlistItemResult.ValidationFailed(
                isbnError = "Enter a valid ISBN-10 or ISBN-13",
            )
        }

        val existingWishlistBook = command.wishlistItemId?.let { wishlistItemId ->
            wishlistRepository.getWishlistBookById(
                userId = command.userId,
                wishlistItemId = wishlistItemId,
            )
        } ?: run {
            null
        }

        if (command.wishlistItemId != null && existingWishlistBook == null) {
            return SaveWishlistItemResult.WishlistItemNotFound
        }

        val exactWishlistBook = validatedIsbn?.let { isbn ->
            wishlistRepository.findWishlistBookByExactIsbn(
                userId = command.userId,
                isbn = isbn,
            )
        }
        val targetWishlistBook = exactWishlistBook ?: existingWishlistBook

        val exactOwnedBook = validatedIsbn?.let { isbn ->
            shelfRepository.findOwnedBookByExactIsbn(
                userId = command.userId,
                isbn = isbn,
            )
        }

        if (
            exactOwnedBook != null &&
            exactOwnedBook.book.id != targetWishlistBook?.book?.id &&
            !command.allowOwnedOverlap
        ) {
            return SaveWishlistItemResult.RequiresOwnedOverlapConfirmation(
                title = exactOwnedBook.book.title,
                authorLine = exactOwnedBook.book.authors.takeIf(List<String>::isNotEmpty)?.joinToString(", "),
            )
        }

        val now = clock()
        val targetBook = when {
            exactOwnedBook != null -> preserveSharedBookMetadata(
                book = exactOwnedBook.book,
                updatedAt = now,
            )

            targetWishlistBook != null -> mergeWishlistFormMetadata(
                book = targetWishlistBook.book,
                title = trimmedTitle,
                author = command.author,
                updatedAt = now,
            )

            else -> Book(
                id = idProvider(),
                userId = command.userId,
                title = trimmedTitle,
                subtitle = null,
                authors = command.author.trim().takeIf(String::isNotBlank)?.let(::listOf) ?: emptyList(),
                description = null,
                publisher = null,
                publishedDate = null,
                pageCount = null,
                thumbnailUrl = null,
                createdAt = now,
                updatedAt = now,
            )
        }
        val wishlistItemId = targetWishlistBook?.item?.id ?: idProvider()
        val identifiers = buildIsbnIdentifiers(
            bookId = targetBook.id,
            isbn = validatedIsbn,
            existingIdentifiers = when {
                exactOwnedBook != null -> exactOwnedBook.identifiers
                targetWishlistBook != null -> targetWishlistBook.identifiers
                else -> emptyList()
            },
            idProvider = idProvider,
        )
        val wishlistItem = WishlistItem(
            id = wishlistItemId,
            userId = command.userId,
            bookId = targetBook.id,
            notes = command.notes.trim().takeIf(String::isNotBlank),
            createdAt = targetWishlistBook?.item?.createdAt ?: now,
            updatedAt = now,
        )

        wishlistRepository.saveWishlistBook(
            book = targetBook,
            wishlistItem = wishlistItem,
            identifiers = identifiers,
        )

        return SaveWishlistItemResult.Success(wishlistItemId = wishlistItem.id)
    }

    private fun preserveSharedBookMetadata(
        book: Book,
        updatedAt: Instant,
    ): Book {
        return book.copy(updatedAt = updatedAt)
    }

    private fun mergeWishlistFormMetadata(
        book: Book,
        title: String,
        author: String,
        updatedAt: Instant,
    ): Book {
        return book.copy(
            title = title,
            authors = author.trim().takeIf(String::isNotBlank)?.let(::listOf) ?: book.authors,
            updatedAt = updatedAt,
        )
    }

}
