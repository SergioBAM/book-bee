package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.OwnershipStatus
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import java.time.Instant
import java.util.UUID

data class MoveWishlistItemToShelfCommand(
    val userId: UUID,
    val wishlistItemId: UUID,
    val notes: String,
    val readStatus: ReadStatus = ReadStatus.UNREAD,
)

sealed interface MoveWishlistItemToShelfResult {
    data class Success(
        val message: String,
    ) : MoveWishlistItemToShelfResult

    data object WishlistItemNotFound : MoveWishlistItemToShelfResult
}

class MoveWishlistItemToShelfUseCase(
    private val shelfRepository: ShelfRepository,
    private val wishlistRepository: WishlistRepository,
    private val clock: () -> Instant = Instant::now,
    private val idProvider: () -> UUID = UUID::randomUUID,
) {
    suspend operator fun invoke(command: MoveWishlistItemToShelfCommand): MoveWishlistItemToShelfResult {
        val wishlistBook = wishlistRepository.getWishlistBookById(
            userId = command.userId,
            wishlistItemId = command.wishlistItemId,
        ) ?: return MoveWishlistItemToShelfResult.WishlistItemNotFound

        val now = clock()
        val ownership = Ownership(
            id = idProvider(),
            userId = command.userId,
            bookId = wishlistBook.book.id,
            quantity = 1,
            status = OwnershipStatus.OWNED,
            readStatus = command.readStatus,
            dateAdded = now,
            archivedAt = null,
            notes = command.notes.trim().takeIf(String::isNotBlank),
            createdAt = now,
            updatedAt = now,
        )
        val updatedBook = wishlistBook.book.copy(updatedAt = now)

        shelfRepository.createOwnershipForExistingBook(
            book = updatedBook,
            ownership = ownership,
            identifiers = wishlistBook.identifiers,
        )
        wishlistRepository.deleteWishlistItem(wishlistBook.item.id)

        return MoveWishlistItemToShelfResult.Success(
            message = "\"${wishlistBook.book.title}\" added to Shelf and removed from Wishlist.",
        )
    }
}
