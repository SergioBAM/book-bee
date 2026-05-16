package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.repository.WishlistRepository
import java.util.UUID

class DeleteWishlistItemUseCase(
    private val wishlistRepository: WishlistRepository,
) {
    suspend operator fun invoke(wishlistItemId: UUID): String? {
        val deletedItem = wishlistRepository.deleteWishlistItem(wishlistItemId) ?: return null
        return "\"${deletedItem.book.title}\" removed from Wishlist."
    }
}
