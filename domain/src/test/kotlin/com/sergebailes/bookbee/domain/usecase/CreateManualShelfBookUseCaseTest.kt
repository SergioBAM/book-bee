package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.OwnershipStatus
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.model.WishlistItem
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateManualShelfBookUseCaseTest {
    @Test
    fun `returns a validation error when title is blank`() = runBlocking {
        val repository = RecordingShelfRepository()
        val wishlistRepository = RecordingWishlistRepository()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            wishlistRepository = wishlistRepository,
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
        val wishlistRepository = RecordingWishlistRepository()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            wishlistRepository = wishlistRepository,
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
        val wishlistRepository = RecordingWishlistRepository()
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val ids = listOf(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
        ).iterator()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            wishlistRepository = wishlistRepository,
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

        assertEquals(CreateManualShelfBookResult.Success(), result)
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
        val wishlistRepository = RecordingWishlistRepository()
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val ids = listOf(
            UUID.fromString("00000000-0000-0000-0000-000000000011"),
            UUID.fromString("00000000-0000-0000-0000-000000000012"),
            UUID.fromString("00000000-0000-0000-0000-000000000013"),
        ).iterator()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            wishlistRepository = wishlistRepository,
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

        assertEquals(CreateManualShelfBookResult.Success(), result)
        assertEquals(ReadStatus.READING, repository.createdOwnership?.readStatus)
        assertEquals(1, repository.createdIdentifiers.size)
        assertEquals("9780441478125", repository.createdIdentifiers.single().value)
    }

    @Test
    fun `reuses a wishlist book and removes the wishlist item when exact isbn becomes owned`() = runBlocking {
        val repository = RecordingShelfRepository()
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000301")
        val bookId = UUID.fromString("00000000-0000-0000-0000-000000000302")
        val wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000303")
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val wishlistRepository = RecordingWishlistRepository(
            wishlistBookByIsbn = WishlistBook(
                item = WishlistItem(
                    id = wishlistItemId,
                    userId = userId,
                    bookId = bookId,
                    notes = "Gift idea",
                    createdAt = now,
                    updatedAt = now,
                ),
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
                identifiers = listOf(
                    BookIdentifier(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000304"),
                        bookId = bookId,
                        type = com.sergebailes.bookbee.domain.model.IdentifierType.ISBN_13,
                        value = "9780441172719",
                    )
                ),
            )
        )
        val ids = listOf(
            UUID.fromString("00000000-0000-0000-0000-000000000305"),
            UUID.fromString("00000000-0000-0000-0000-000000000306"),
        ).iterator()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            wishlistRepository = wishlistRepository,
            clock = { now },
            idProvider = { ids.next() },
        )

        val result = useCase(
            CreateManualShelfBookCommand(
                userId = userId,
                title = "Dune",
                author = "Frank Herbert",
                notes = "Bought today",
                isbn = "978-0-441-17271-9",
            )
        )

        assertEquals(
            CreateManualShelfBookResult.Success(
                message = "\"Dune\" moved from Wishlist to Shelf.",
            ),
            result,
        )
        assertEquals(bookId, repository.createdExistingOwnershipBook?.id)
        assertEquals(wishlistItemId, wishlistRepository.deletedWishlistItemId)
    }

    @Test
    fun `preserves wishlist book title and authors when exact isbn promotion form is sparse`() = runBlocking {
        val repository = RecordingShelfRepository()
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000401")
        val bookId = UUID.fromString("00000000-0000-0000-0000-000000000402")
        val wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000403")
        val createdAt = Instant.parse("2026-05-15T10:15:30Z")
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val wishlistRepository = RecordingWishlistRepository(
            wishlistBookByIsbn = WishlistBook(
                item = WishlistItem(
                    id = wishlistItemId,
                    userId = userId,
                    bookId = bookId,
                    notes = "Gift idea",
                    createdAt = createdAt,
                    updatedAt = createdAt,
                ),
                book = Book(
                    id = bookId,
                    userId = userId,
                    title = "Dune: Deluxe Edition",
                    subtitle = null,
                    authors = listOf("Frank Herbert", "Brian Herbert"),
                    description = null,
                    publisher = null,
                    publishedDate = null,
                    pageCount = null,
                    thumbnailUrl = null,
                    createdAt = createdAt,
                    updatedAt = createdAt,
                ),
                identifiers = listOf(
                    BookIdentifier(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000404"),
                        bookId = bookId,
                        type = com.sergebailes.bookbee.domain.model.IdentifierType.ISBN_13,
                        value = "9780441172719",
                    )
                ),
            )
        )
        val ids = listOf(
            UUID.fromString("00000000-0000-0000-0000-000000000405"),
        ).iterator()
        val useCase = CreateManualShelfBookUseCase(
            shelfRepository = repository,
            wishlistRepository = wishlistRepository,
            clock = { now },
            idProvider = { ids.next() },
        )

        val result = useCase(
            CreateManualShelfBookCommand(
                userId = userId,
                title = "Dune",
                author = "",
                notes = "Bought today",
                isbn = "978-0-441-17271-9",
            )
        )

        assertEquals(
            CreateManualShelfBookResult.Success(
                message = "\"Dune: Deluxe Edition\" moved from Wishlist to Shelf.",
            ),
            result,
        )
        assertEquals("Dune: Deluxe Edition", repository.createdExistingOwnershipBook?.title)
        assertEquals(
            listOf("Frank Herbert", "Brian Herbert"),
            repository.createdExistingOwnershipBook?.authors,
        )
        assertEquals(createdAt, repository.createdExistingOwnershipBook?.createdAt)
        assertEquals(now, repository.createdExistingOwnershipBook?.updatedAt)
        assertEquals("9780441172719", repository.createdIdentifiers.single().value)
        assertEquals(wishlistItemId, wishlistRepository.deletedWishlistItemId)
    }

    private class RecordingShelfRepository : ShelfRepository {
        var createdBook: Book? = null
        var createdExistingOwnershipBook: Book? = null
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

        override suspend fun createOwnershipForExistingBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) {
            createdExistingOwnershipBook = book
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

        override suspend fun findOwnedBookByExactIsbn(
            userId: UUID,
            isbn: com.sergebailes.bookbee.domain.isbn.ValidatedIsbn,
        ): ShelfBook? = null
    }

    private class RecordingWishlistRepository(
        private val wishlistBookByIsbn: WishlistBook? = null,
    ) : WishlistRepository {
        var deletedWishlistItemId: UUID? = null

        override fun observeWishlistBooks(userId: UUID): Flow<List<WishlistBook>> {
            return flowOf(wishlistBookByIsbn?.let(::listOf) ?: emptyList())
        }

        override suspend fun getWishlistBookById(
            userId: UUID,
            wishlistItemId: UUID,
        ): WishlistBook? = wishlistBookByIsbn?.takeIf { it.item.id == wishlistItemId }

        override suspend fun saveWishlistBook(
            book: Book,
            wishlistItem: WishlistItem,
            identifiers: List<BookIdentifier>,
        ) = Unit

        override suspend fun deleteWishlistItem(wishlistItemId: UUID): WishlistBook? {
            deletedWishlistItemId = wishlistItemId
            return wishlistBookByIsbn?.takeIf { it.item.id == wishlistItemId }
        }

        override suspend fun findWishlistBookByExactIsbn(
            userId: UUID,
            isbn: com.sergebailes.bookbee.domain.isbn.ValidatedIsbn,
        ): WishlistBook? = wishlistBookByIsbn
    }
}
