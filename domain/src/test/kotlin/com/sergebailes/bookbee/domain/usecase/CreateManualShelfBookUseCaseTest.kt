package com.sergebailes.bookbee.domain.usecase

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
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateManualShelfBookUseCaseTest {
    @Test
    fun `returns a validation error when title is blank`() = runBlocking {
        val repository = RecordingShelfRepository()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            clock = { Instant.parse("2026-05-16T10:15:30Z") },
            idProvider = { UUID.randomUUID() },
        )

        val result = useCase(
            CreateManualShelfBookCommand(
                userId = UUID.randomUUID(),
                title = "   ",
                author = "",
                notes = "",
                isbn = "",
            )
        )

        assertEquals(
            CreateManualShelfBookResult.ValidationFailed(
                titleError = "Title is required",
            ),
            result,
        )
        assertNull(repository.createdBook)
        assertNull(repository.createdOwnership)
        assertTrue(repository.createdIdentifiers.isEmpty())
    }

    @Test
    fun `returns a validation error when isbn is invalid`() = runBlocking {
        val repository = RecordingShelfRepository()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            clock = { Instant.parse("2026-05-16T10:15:30Z") },
            idProvider = { UUID.randomUUID() },
        )

        val result = useCase(
            CreateManualShelfBookCommand(
                userId = UUID.randomUUID(),
                title = "Dune",
                author = "Frank Herbert",
                notes = "",
                isbn = "9781400033417",
            )
        )

        assertEquals(
            CreateManualShelfBookResult.ValidationFailed(
                isbnError = "Enter a valid ISBN-10 or ISBN-13",
            ),
            result,
        )
        assertNull(repository.createdBook)
        assertNull(repository.createdOwnership)
        assertTrue(repository.createdIdentifiers.isEmpty())
    }

    @Test
    fun `creates an owned shelf record with MVP defaults when isbn is omitted`() = runBlocking {
        val repository = RecordingShelfRepository()
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val ids = listOf(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
        ).iterator()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            clock = { now },
            idProvider = { ids.next() },
        )
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000099")

        val result = useCase(
            CreateManualShelfBookCommand(
                userId = userId,
                title = "  Dune  ",
                author = " Frank Herbert ",
                notes = "  Signed copy  ",
                isbn = "",
            )
        )

        assertEquals(CreateManualShelfBookResult.Success, result)
        assertEquals("Dune", repository.createdBook?.title)
        assertEquals(listOf("Frank Herbert"), repository.createdBook?.authors)
        assertEquals(1, repository.createdOwnership?.quantity)
        assertEquals(OwnershipStatus.OWNED, repository.createdOwnership?.status)
        assertEquals(ReadStatus.UNREAD, repository.createdOwnership?.readStatus)
        assertEquals("Signed copy", repository.createdOwnership?.notes)
        assertEquals(now, repository.createdOwnership?.dateAdded)
        assertTrue(repository.createdIdentifiers.isEmpty())
        assertNull(repository.createdBook?.subtitle)
    }

    @Test
    fun `stores a normalized isbn and explicit read status when provided`() = runBlocking {
        val repository = RecordingShelfRepository()
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val ids = listOf(
            UUID.fromString("00000000-0000-0000-0000-000000000011"),
            UUID.fromString("00000000-0000-0000-0000-000000000012"),
            UUID.fromString("00000000-0000-0000-0000-000000000013"),
        ).iterator()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            clock = { now },
            idProvider = { ids.next() },
        )
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000199")

        val result = useCase(
            CreateManualShelfBookCommand(
                userId = userId,
                title = "The Left Hand of Darkness",
                author = "Ursula K. Le Guin",
                notes = "",
                isbn = "978-0-441-47812-5",
                readStatus = ReadStatus.READING,
            )
        )

        assertEquals(CreateManualShelfBookResult.Success, result)
        assertEquals(ReadStatus.READING, repository.createdOwnership?.readStatus)
        assertEquals(1, repository.createdIdentifiers.size)
        assertEquals("9780441478125", repository.createdIdentifiers.single().value)
    }

    private class RecordingShelfRepository : ShelfRepository {
        var createdBook: Book? = null
        var createdOwnership: Ownership? = null
        var createdIdentifiers: List<BookIdentifier> = emptyList()

        override suspend fun createBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) {
            createdBook = book
            createdOwnership = ownership
            createdIdentifiers = identifiers
        }

        override suspend fun updateBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) = Unit

        override suspend fun archiveOwnership(
            ownershipId: UUID,
            archivedAt: Instant,
        ) = Unit

        override fun observeOwnedBooks(userId: UUID): Flow<List<ShelfBook>> = emptyFlow()

        override suspend fun getBookDetailById(
            userId: UUID,
            bookId: UUID,
        ): ShelfBook? = null
    }
}
