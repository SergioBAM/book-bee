package com.sergebailes.bookbee.domain.repository

import com.sergebailes.bookbee.domain.isbn.ValidatedIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.model.WishlistItem
import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface WishlistRepository {
    fun observeWishlistBooks(userId: UUID): Flow<List<WishlistBook>>

    suspend fun getWishlistBookById(
        userId: UUID,
        wishlistItemId: UUID,
    ): WishlistBook?

    suspend fun saveWishlistBook(
        book: Book,
        wishlistItem: WishlistItem,
        identifiers: List<BookIdentifier> = emptyList(),
    )

    suspend fun deleteWishlistItem(wishlistItemId: UUID): WishlistBook?

    suspend fun findWishlistBookByExactIsbn(
        userId: UUID,
        isbn: ValidatedIsbn,
    ): WishlistBook?
}
