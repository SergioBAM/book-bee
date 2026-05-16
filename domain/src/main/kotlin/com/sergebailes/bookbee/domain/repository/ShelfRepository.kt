package com.sergebailes.bookbee.domain.repository

import com.sergebailes.bookbee.domain.isbn.ValidatedIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.ShelfBook
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface ShelfRepository {
    suspend fun createBook(
        book: Book,
        ownership: Ownership,
        identifiers: List<BookIdentifier> = emptyList(),
    )

    suspend fun createOwnershipForExistingBook(
        book: Book,
        ownership: Ownership,
        identifiers: List<BookIdentifier> = emptyList(),
    )

    suspend fun updateBook(
        book: Book,
        ownership: Ownership,
        identifiers: List<BookIdentifier> = emptyList(),
    )

    suspend fun archiveOwnership(
        ownershipId: UUID,
        archivedAt: Instant,
    )

    fun observeOwnedBooks(userId: UUID): Flow<List<ShelfBook>>

    suspend fun getBookDetailById(
        userId: UUID,
        bookId: UUID,
    ): ShelfBook?

    suspend fun findOwnedBookByExactIsbn(
        userId: UUID,
        isbn: ValidatedIsbn,
    ): ShelfBook?
}
