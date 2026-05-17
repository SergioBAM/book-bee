package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.isbn.ValidatedIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.OwnershipStatus
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ManageShelfCopiesUseCasesTest {
    @Test
    fun `add another copy increments quantity and preserves date added`() = runBlocking {
        val original = shelfBook(quantity = 1)
        val repository = RecordingShelfRepository(original)
        val useCase = AddShelfCopyUseCase(
            shelfRepository = repository,
            clock = { Instant.parse("2026-05-17T10:00:00Z") },
        )

        val result = useCase(original.ownership.userId, original.book.id)

        assertEquals(
            AddShelfCopyResult.Success(
                title = "Dune",
                quantity = 2,
            ),
            result,
        )
        assertEquals(2, repository.updatedOwnership?.quantity)
        assertEquals(original.ownership.dateAdded, repository.updatedOwnership?.dateAdded)
        assertNull(repository.updatedOwnership?.archivedAt)
    }

    @Test
    fun `undo add another copy decrements quantity without archiving`() = runBlocking {
        val original = shelfBook(quantity = 2)
        val repository = RecordingShelfRepository(original)
        val useCase = UndoAddShelfCopyUseCase(
            shelfRepository = repository,
            clock = { Instant.parse("2026-05-17T10:00:00Z") },
        )

        val result = useCase(original.ownership.userId, original.book.id)

        assertEquals(
            UndoAddShelfCopyResult.Success(
                title = "Dune",
                quantity = 1,
            ),
            result,
        )
        assertEquals(1, repository.updatedOwnership?.quantity)
        assertEquals(OwnershipStatus.OWNED, repository.updatedOwnership?.status)
    }

    @Test
    fun `remove copy asks for archive confirmation when decrementing from one to zero`() = runBlocking {
        val original = shelfBook(quantity = 1)
        val repository = RecordingShelfRepository(original)
        val useCase = RemoveShelfCopyUseCase(repository)

        val result = useCase(original.ownership.userId, original.book.id)

        assertEquals(
            RemoveShelfCopyResult.ArchiveConfirmationRequired(title = "Dune"),
            result,
        )
        assertNull(repository.updatedOwnership)
    }

    @Test
    fun `archive shelf book archives the active ownership without changing quantity`() = runBlocking {
        val original = shelfBook(quantity = 1)
        val repository = RecordingShelfRepository(original)
        val useCase = ArchiveShelfBookUseCase(
            shelfRepository = repository,
            clock = { Instant.parse("2026-05-17T10:00:00Z") },
        )

        val result = useCase(original.ownership.userId, original.book.id)

        assertEquals(ArchiveShelfBookResult.Success(title = "Dune"), result)
        assertEquals(1, repository.updatedOwnership?.quantity)
        assertEquals(OwnershipStatus.ARCHIVED, repository.updatedOwnership?.status)
        assertEquals(Instant.parse("2026-05-17T10:00:00Z"), repository.updatedOwnership?.archivedAt)
    }

    private class RecordingShelfRepository(
        private val shelfBook: ShelfBook,
    ) : ShelfRepository {
        var updatedOwnership: Ownership? = null

        override suspend fun createBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) = Unit

        override suspend fun createOwnershipForExistingBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) = Unit

        override suspend fun updateBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) {
            updatedOwnership = ownership
        }

        override suspend fun archiveOwnership(
            ownershipId: UUID,
            archivedAt: Instant,
        ) = Unit

        override fun observeOwnedBooks(userId: UUID): Flow<List<ShelfBook>> = emptyFlow()

        override suspend fun getBookDetailById(
            userId: UUID,
            bookId: UUID,
        ): ShelfBook? = shelfBook.takeIf { it.ownership.userId == userId && it.book.id == bookId }

        override suspend fun findOwnedBookByExactIsbn(
            userId: UUID,
            isbn: ValidatedIsbn,
        ): ShelfBook? = null
    }

    private fun shelfBook(
        quantity: Int,
    ): ShelfBook {
        val now = Instant.parse("2026-05-16T10:00:00Z")
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000901")
        val bookId = UUID.fromString("00000000-0000-0000-0000-000000000902")
        return ShelfBook(
            book = Book(
                id = bookId,
                userId = userId,
                title = "Dune",
                subtitle = null,
                authors = listOf("Frank Herbert"),
                description = null,
                publisher = null,
                publishedDate = null,
                pageCount = null,
                thumbnailUrl = null,
                createdAt = now,
                updatedAt = now,
            ),
            ownership = Ownership(
                id = UUID.fromString("00000000-0000-0000-0000-000000000903"),
                userId = userId,
                bookId = bookId,
                quantity = quantity,
                status = OwnershipStatus.OWNED,
                readStatus = ReadStatus.UNREAD,
                dateAdded = now,
                archivedAt = null,
                notes = null,
                createdAt = now,
                updatedAt = now,
            ),
            identifiers = emptyList(),
        )
    }
}
