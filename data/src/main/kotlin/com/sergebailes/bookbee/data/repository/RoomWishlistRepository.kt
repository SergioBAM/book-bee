package com.sergebailes.bookbee.data.repository

import androidx.room3.withWriteTransaction
import com.sergebailes.bookbee.data.database.BookBeeDatabase
import com.sergebailes.bookbee.data.database.dao.BookDao
import com.sergebailes.bookbee.data.database.dao.BookIdentifierDao
import com.sergebailes.bookbee.data.database.dao.OwnershipDao
import com.sergebailes.bookbee.data.database.dao.WishlistItemDao
import com.sergebailes.bookbee.data.repository.mapper.toDataModel
import com.sergebailes.bookbee.data.repository.mapper.toDomainModel
import com.sergebailes.bookbee.domain.isbn.ValidatedIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.model.WishlistItem
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomWishlistRepository(
    private val database: BookBeeDatabase,
    private val bookDao: BookDao = database.bookDao(),
    private val bookIdentifierDao: BookIdentifierDao = database.bookIdentifierDao(),
    private val ownershipDao: OwnershipDao = database.ownershipDao(),
    private val wishlistItemDao: WishlistItemDao = database.wishlistItemDao(),
) : WishlistRepository {
    override fun observeWishlistBooks(userId: UUID): Flow<List<WishlistBook>> {
        return wishlistItemDao.observeRelationsByUserId(userId).map { wishlistBooks ->
            wishlistBooks.map { it.toDomainModel() }
        }
    }

    override suspend fun getWishlistBookById(
        userId: UUID,
        wishlistItemId: UUID,
    ): WishlistBook? {
        val relation = wishlistItemDao.getRelationById(wishlistItemId) ?: return null
        return relation.takeIf { it.wishlistItem.userId == userId }?.toDomainModel()
    }

    override suspend fun saveWishlistBook(
        book: Book,
        wishlistItem: WishlistItem,
        identifiers: List<BookIdentifier>,
    ) {
        require(book.userId == wishlistItem.userId) {
            "wishlist item must belong to the same user as the book"
        }
        require(book.id == wishlistItem.bookId) {
            "wishlist item must reference the provided book"
        }
        require(identifiers.all { it.bookId == book.id }) {
            "All identifiers must belong to the provided book"
        }

        val previousRelation = wishlistItemDao.getRelationById(wishlistItem.id)
        val previousBookId = previousRelation?.wishlistItem?.bookId

        database.withWriteTransaction {
            val existingBook = bookDao.getById(book.id)
            if (existingBook == null) {
                bookDao.insert(book.toDataModel())
            } else {
                bookDao.update(book.toDataModel())
            }

            bookIdentifierDao.deleteByBookId(book.id)
            if (identifiers.isNotEmpty()) {
                bookIdentifierDao.insertAll(identifiers.map(BookIdentifier::toDataModel))
            }

            wishlistItemDao.insert(wishlistItem.toDataModel())

            if (previousBookId != null && previousBookId != book.id) {
                pruneBookIfUnreferenced(previousBookId)
            }
        }
    }

    override suspend fun deleteWishlistItem(wishlistItemId: UUID): WishlistBook? {
        val relation = wishlistItemDao.getRelationById(wishlistItemId) ?: return null

        database.withWriteTransaction {
            wishlistItemDao.deleteById(wishlistItemId)
            pruneBookIfUnreferenced(relation.wishlistItem.bookId)
        }

        return relation.toDomainModel()
    }

    override suspend fun findWishlistBookByExactIsbn(
        userId: UUID,
        isbn: ValidatedIsbn,
    ): WishlistBook? {
        val matchingIdentifiers = bookIdentifierDao.findByTypeAndValue(
            type = com.sergebailes.bookbee.data.database.entity.IdentifierType.valueOf(isbn.type.name),
            value = isbn.value,
        )

        for (identifier in matchingIdentifiers) {
            val relation = wishlistItemDao.getRelationByUserIdAndBookId(
                userId = userId,
                bookId = identifier.bookId,
            )
            if (relation != null) {
                return relation.toDomainModel()
            }
        }

        return null
    }

    private suspend fun pruneBookIfUnreferenced(bookId: UUID) {
        val ownershipCount = ownershipDao.countByBookId(bookId)
        val wishlistCount = wishlistItemDao.countByBookId(bookId)
        if (ownershipCount == 0 && wishlistCount == 0) {
            bookIdentifierDao.deleteByBookId(bookId)
            bookDao.deleteById(bookId)
        }
    }
}
