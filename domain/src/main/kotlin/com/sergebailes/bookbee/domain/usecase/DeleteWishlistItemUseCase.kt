package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.isbn.parseIsbn
import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import java.util.UUID

sealed interface RestoreWishlistItemResult {
    data object Restored : RestoreWishlistItemResult
    data object AlreadyExistsForExactIsbn : RestoreWishlistItemResult
}

class DeleteWishlistItemUseCase(
    private val wishlistRepository: WishlistRepository,
) {
    suspend operator fun invoke(wishlistItemId: UUID): WishlistBook? {
        return wishlistRepository.deleteWishlistItem(wishlistItemId)
    }

    suspend fun restore(deletedWishlistBook: WishlistBook): RestoreWishlistItemResult {
        val exactIsbn = deletedWishlistBook.identifiers
            .asSequence()
            .filter { it.type == IdentifierType.ISBN_10 || it.type == IdentifierType.ISBN_13 }
            .mapNotNull { parseIsbn(it.value) }
            .firstOrNull()

        val existingWishlistBook = exactIsbn?.let { isbn ->
            wishlistRepository.findWishlistBookByExactIsbn(
                userId = deletedWishlistBook.item.userId,
                isbn = isbn,
            )
        }
        if (
            existingWishlistBook != null &&
            existingWishlistBook.item.id != deletedWishlistBook.item.id
        ) {
            return RestoreWishlistItemResult.AlreadyExistsForExactIsbn
        }

        wishlistRepository.saveWishlistBook(
            book = deletedWishlistBook.book,
            wishlistItem = deletedWishlistBook.item,
            identifiers = deletedWishlistBook.identifiers,
        )
        return RestoreWishlistItemResult.Restored
    }
}
