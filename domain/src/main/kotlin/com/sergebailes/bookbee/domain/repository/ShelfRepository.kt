package com.sergebailes.bookbee.domain.repository

import com.sergebailes.bookbee.domain.isbn.ValidatedIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.ShelfBook
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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

    suspend fun restoreArchivedOwnership(
        ownershipId: UUID,
        restoredAt: Instant,
    ): RestoreArchivedOwnershipResult = RestoreArchivedOwnershipResult.ArchivedOwnershipNotFound

    suspend fun hardDeleteArchivedOwnership(
        ownershipId: UUID,
    ): HardDeleteArchivedOwnershipResult = HardDeleteArchivedOwnershipResult.ArchivedOwnershipNotFound

    fun observeOwnedBooks(userId: UUID): Flow<List<ShelfBook>>

    fun observeArchivedBooks(userId: UUID): Flow<List<ShelfBook>> = emptyFlow()

    suspend fun getBookDetailById(
        userId: UUID,
        bookId: UUID,
    ): ShelfBook?

    suspend fun getArchivedBookDetailById(
        userId: UUID,
        bookId: UUID,
    ): ShelfBook? = null

    suspend fun findOwnedBookByExactIsbn(
        userId: UUID,
        isbn: ValidatedIsbn,
    ): ShelfBook?

    suspend fun findArchivedBookByExactIsbn(
        userId: UUID,
        isbn: ValidatedIsbn,
    ): ShelfBook? = null
}

sealed interface RestoreArchivedOwnershipResult {
    data class Success(
        val restoredBook: ShelfBook,
    ) : RestoreArchivedOwnershipResult

    data class ActiveExactIsbnConflict(
        val activeBook: ShelfBook,
    ) : RestoreArchivedOwnershipResult

    data object ArchivedOwnershipNotFound : RestoreArchivedOwnershipResult
}

sealed interface HardDeleteArchivedOwnershipResult {
    data class Success(
        val deletedBookId: UUID,
        val deletedWishlistItemCount: Int,
        val deletedBookAggregate: Boolean,
    ) : HardDeleteArchivedOwnershipResult

    data object ArchivedOwnershipNotFound : HardDeleteArchivedOwnershipResult
}
