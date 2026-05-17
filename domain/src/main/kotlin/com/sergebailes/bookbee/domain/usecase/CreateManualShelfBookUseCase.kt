package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.isbn.buildIsbnIdentifiers
import com.sergebailes.bookbee.domain.isbn.parseIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.OwnershipStatus
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import java.time.Instant
import java.util.UUID

data class CreateManualShelfBookCommand(
    val userId: UUID,
    val title: String,
    val author: String,
    val notes: String,
    val isbn: String,
    val readStatus: ReadStatus = ReadStatus.UNREAD,
)

sealed interface CreateManualShelfBookResult {
    data class Success(
        val message: String? = null,
    ) : CreateManualShelfBookResult

    data class ValidationFailed(
        val titleError: String? = null,
        val isbnError: String? = null,
    ) : CreateManualShelfBookResult

    data class DuplicateActiveOwned(
        val bookId: UUID,
        val title: String,
        val authorLine: String?,
    ) : CreateManualShelfBookResult
}

class CreateManualShelfBookUseCase(
    private val shelfRepository: ShelfRepository,
    private val wishlistRepository: WishlistRepository,
    private val clock: () -> Instant = Instant::now,
    private val idProvider: () -> UUID = UUID::randomUUID,
) {
    suspend operator fun invoke(command: CreateManualShelfBookCommand): CreateManualShelfBookResult {
        val trimmedTitle = command.title.trim()
        if (trimmedTitle.isBlank()) {
            return CreateManualShelfBookResult.ValidationFailed(
                titleError = "Title is required",
            )
        }
        val validatedIsbn = command.isbn.trim()
            .takeIf(String::isNotBlank)
            ?.let(::parseIsbn)
        if (command.isbn.isNotBlank() && validatedIsbn == null) {
            return CreateManualShelfBookResult.ValidationFailed(
                isbnError = "Enter a valid ISBN-10 or ISBN-13",
            )
        }

        val exactOwnedBook = validatedIsbn?.let { isbn ->
            shelfRepository.findOwnedBookByExactIsbn(
                userId = command.userId,
                isbn = isbn,
            )
        }
        if (exactOwnedBook != null) {
            return CreateManualShelfBookResult.DuplicateActiveOwned(
                bookId = exactOwnedBook.book.id,
                title = exactOwnedBook.book.title,
                authorLine = exactOwnedBook.book.authors.takeIf(List<String>::isNotEmpty)?.joinToString(", "),
            )
        }

        val now = clock()
        val matchingWishlistBook = validatedIsbn?.let { isbn ->
            wishlistRepository.findWishlistBookByExactIsbn(
                userId = command.userId,
                isbn = isbn,
            )
        }
        val bookId = matchingWishlistBook?.book?.id ?: idProvider()
        val book = matchingWishlistBook?.book?.copy(
            updatedAt = now,
        ) ?: Book(
            id = bookId,
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
        val ownership = Ownership(
            id = idProvider(),
            userId = command.userId,
            bookId = bookId,
            quantity = 1,
            status = OwnershipStatus.OWNED,
            readStatus = command.readStatus,
            dateAdded = now,
            archivedAt = null,
            notes = command.notes.trim().takeIf(String::isNotBlank),
            createdAt = now,
            updatedAt = now,
        )
        val existingIdentifiers = matchingWishlistBook?.identifiers?.filter { it.bookId == bookId } ?: emptyList()
        val identifiers = buildIsbnIdentifiers(
            bookId = bookId,
            isbn = validatedIsbn,
            existingIdentifiers = existingIdentifiers,
            idProvider = idProvider,
        )

        if (matchingWishlistBook == null) {
            shelfRepository.createBook(
                book = book,
                ownership = ownership,
                identifiers = identifiers,
            )

            return CreateManualShelfBookResult.Success()
        }

        shelfRepository.createOwnershipForExistingBook(
            book = book,
            ownership = ownership,
            identifiers = identifiers,
        )
        wishlistRepository.deleteWishlistItem(matchingWishlistBook.item.id)

        return CreateManualShelfBookResult.Success(
            message = "\"${book.title}\" moved from Wishlist to Shelf.",
        )
    }
}
