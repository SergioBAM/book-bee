package com.sergebailes.bookbee.data.repository

import androidx.room3.withWriteTransaction
import com.sergebailes.bookbee.data.database.BookBeeDatabase
import com.sergebailes.bookbee.data.database.dao.BookDao
import com.sergebailes.bookbee.data.database.dao.BookIdentifierDao
import com.sergebailes.bookbee.data.database.dao.OwnershipDao
import com.sergebailes.bookbee.data.database.dao.ShelfDao
import com.sergebailes.bookbee.data.database.entity.OwnershipStatus
import com.sergebailes.bookbee.data.repository.mapper.toDataModel
import com.sergebailes.bookbee.data.repository.mapper.toDomainModel
import com.sergebailes.bookbee.domain.isbn.ValidatedIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomShelfRepository(
    private val database: BookBeeDatabase,
    private val bookDao: BookDao = database.bookDao(),
    private val bookIdentifierDao: BookIdentifierDao = database.bookIdentifierDao(),
    private val ownershipDao: OwnershipDao = database.ownershipDao(),
    private val shelfDao: ShelfDao = database.shelfDao(),
) : ShelfRepository {
    override suspend fun createBook(
        book: Book,
        ownership: Ownership,
        identifiers: List<BookIdentifier>,
    ) {
        validateAggregate(
            book = book,
            ownership = ownership,
            identifiers = identifiers,
        )

        database.withWriteTransaction {
            check(bookDao.getById(book.id) == null) {
                "Book with id ${book.id} already exists"
            }
            check(
                ownershipDao.getByUserIdAndBookId(
                    userId = ownership.userId,
                    bookId = ownership.bookId,
                ).isEmpty()
            ) {
                "Ownership for book ${ownership.bookId} already exists"
            }

            bookDao.insert(book.toDataModel())
            ownershipDao.insert(ownership.toDataModel())
            bookIdentifierDao.insertAll(identifiers.map(BookIdentifier::toDataModel))
        }
    }

    override suspend fun createOwnershipForExistingBook(
        book: Book,
        ownership: Ownership,
        identifiers: List<BookIdentifier>,
    ) {
        validateAggregate(
            book = book,
            ownership = ownership,
            identifiers = identifiers,
        )

        database.withWriteTransaction {
            check(bookDao.getById(book.id) != null) {
                "Book with id ${book.id} does not exist"
            }
            check(
                ownershipDao.getByUserIdAndBookId(
                    userId = ownership.userId,
                    bookId = ownership.bookId,
                ).isEmpty()
            ) {
                "Ownership for book ${ownership.bookId} already exists"
            }

            bookDao.update(book.toDataModel())
            bookIdentifierDao.deleteByBookId(book.id)
            bookIdentifierDao.insertAll(identifiers.map(BookIdentifier::toDataModel))
            ownershipDao.insert(ownership.toDataModel())
        }
    }

    override suspend fun updateBook(
        book: Book,
        ownership: Ownership,
        identifiers: List<BookIdentifier>,
    ) {
        validateAggregate(
            book = book,
            ownership = ownership,
            identifiers = identifiers,
        )

        database.withWriteTransaction {
            check(bookDao.getById(book.id) != null) {
                "Book with id ${book.id} does not exist"
            }
            check(
                ownershipDao.getByUserIdAndBookId(
                    userId = ownership.userId,
                    bookId = ownership.bookId,
                ).isNotEmpty()
            ) {
                "Ownership for book ${ownership.bookId} does not exist"
            }

            bookDao.update(book.toDataModel())
            ownershipDao.update(ownership.toDataModel())
            bookIdentifierDao.deleteByBookId(book.id)
            bookIdentifierDao.insertAll(identifiers.map(BookIdentifier::toDataModel))
        }
    }

    override suspend fun archiveOwnership(
        ownershipId: UUID,
        archivedAt: Instant,
    ) {
        val existingOwnership = ownershipDao.getById(ownershipId) ?: return

        ownershipDao.update(
            existingOwnership.copy(
                status = OwnershipStatus.ARCHIVED,
                archivedAt = archivedAt,
                updatedAt = archivedAt,
            )
        )
    }

    override fun observeOwnedBooks(userId: UUID): Flow<List<ShelfBook>> {
        return shelfDao.observeByUserIdAndStatus(
            userId = userId,
            status = OwnershipStatus.OWNED,
        ).map { shelfBooks ->
            shelfBooks.map { it.toDomainModel() }
        }
    }

    override suspend fun getBookDetailById(
        userId: UUID,
        bookId: UUID,
    ): ShelfBook? {
        return shelfDao.getByUserIdAndBookIdAndStatus(
            userId = userId,
            bookId = bookId,
            status = OwnershipStatus.OWNED,
        )?.toDomainModel()
    }

    override suspend fun findOwnedBookByExactIsbn(
        userId: UUID,
        isbn: ValidatedIsbn,
    ): ShelfBook? {
        val matchingIdentifiers = bookIdentifierDao.findByTypeAndValue(
            type = com.sergebailes.bookbee.data.database.entity.IdentifierType.valueOf(isbn.type.name),
            value = isbn.value,
        )

        for (identifier in matchingIdentifiers) {
            val shelfBook = shelfDao.getByUserIdAndBookIdAndStatus(
                userId = userId,
                bookId = identifier.bookId,
                status = OwnershipStatus.OWNED,
            )
            if (shelfBook != null) {
                return shelfBook.toDomainModel()
            }
        }

        return null
    }

    private fun validateAggregate(
        book: Book,
        ownership: Ownership,
        identifiers: List<BookIdentifier>,
    ) {
        require(ownership.bookId == book.id) {
            "ownership.bookId must match book.id"
        }
        require(ownership.userId == book.userId) {
            "ownership.userId must match book.userId"
        }
        require(identifiers.all { it.bookId == book.id }) {
            "All identifiers must belong to the provided book"
        }
    }
}
