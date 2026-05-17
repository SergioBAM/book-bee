package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.isbn.ValidatedIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.IdentifierType
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
import org.junit.Test

class SaveWishlistItemUseCaseTest {
    @Test
    fun `requires confirmation before saving a same-edition owned book to wishlist`() = runBlocking {
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000501")
        val ownedBook = ownedShelfBook(
            userId = userId,
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000502"),
            now = now,
        )
        val shelfRepository = RecordingShelfRepository(ownedBook)
        val wishlistRepository = RecordingWishlistRepository()
        val useCase = SaveWishlistItemUseCase(
            shelfRepository = shelfRepository,
            wishlistRepository = wishlistRepository,
            clock = { now },
            idProvider = { UUID.randomUUID() },
        )

        val result = useCase(
            SaveWishlistItemCommand(
                userId = userId,
                title = "Dune",
                author = "Frank Herbert",
                notes = "Gift copy",
                isbn = "978-0-441-17271-9",
            )
        )

        assertEquals(
            SaveWishlistItemResult.RequiresOwnedOverlapConfirmation(
                title = "Dune",
                authorLine = "Frank Herbert",
            ),
            result,
        )
        assertNull(wishlistRepository.savedWishlistItem)
    }

    @Test
    fun `saves against the existing owned book after confirmation`() = runBlocking {
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000511")
        val ownedBook = ownedShelfBook(
            userId = userId,
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000512"),
            now = now,
        )
        val shelfRepository = RecordingShelfRepository(ownedBook)
        val wishlistRepository = RecordingWishlistRepository()
        val ids = listOf(
            UUID.fromString("00000000-0000-0000-0000-000000000513"),
            UUID.fromString("00000000-0000-0000-0000-000000000514"),
        ).iterator()
        val useCase = SaveWishlistItemUseCase(
            shelfRepository = shelfRepository,
            wishlistRepository = wishlistRepository,
            clock = { now },
            idProvider = { ids.next() },
        )

        val result = useCase(
            SaveWishlistItemCommand(
                userId = userId,
                title = "Dune",
                author = "Frank Herbert",
                notes = "Gift copy",
                isbn = "978-0-441-17271-9",
                allowOwnedOverlap = true,
            )
        )

        assertEquals(
            SaveWishlistItemResult.Success(
                wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000513"),
            ),
            result,
        )
        assertEquals(ownedBook.book.id, wishlistRepository.savedBook?.id)
        assertEquals(ownedBook.book.id, wishlistRepository.savedWishlistItem?.bookId)
        assertEquals("Gift copy", wishlistRepository.savedWishlistItem?.notes)
        assertEquals(
            listOf(
                "9780441172719",
                "0441172717",
            ),
            wishlistRepository.savedIdentifiers.map { it.value },
        )
    }

    @Test
    fun `preserves owned book title and authors when confirmed wishlist save is sparse`() = runBlocking {
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000521")
        val ownedBook = ownedShelfBook(
            userId = userId,
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000522"),
            now = now,
            title = "Dune: Deluxe Edition",
            authors = listOf("Frank Herbert", "Brian Herbert"),
        )
        val shelfRepository = RecordingShelfRepository(ownedBook)
        val wishlistRepository = RecordingWishlistRepository()
        val ids = listOf(
            UUID.fromString("00000000-0000-0000-0000-000000000523"),
        ).iterator()
        val useCase = SaveWishlistItemUseCase(
            shelfRepository = shelfRepository,
            wishlistRepository = wishlistRepository,
            clock = { now },
            idProvider = { ids.next() },
        )

        val result = useCase(
            SaveWishlistItemCommand(
                userId = userId,
                title = "Dune",
                author = "",
                notes = "Buy a reading copy",
                isbn = "978-0-441-17271-9",
                allowOwnedOverlap = true,
            )
        )

        assertEquals(
            SaveWishlistItemResult.Success(
                wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000523"),
            ),
            result,
        )
        assertEquals("Dune: Deluxe Edition", wishlistRepository.savedBook?.title)
        assertEquals(listOf("Frank Herbert", "Brian Herbert"), wishlistRepository.savedBook?.authors)
    }

    @Test
    fun `reuses existing wishlist item when saving the same exact isbn again`() = runBlocking {
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000531")
        val wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000532")
        val bookId = UUID.fromString("00000000-0000-0000-0000-000000000533")
        val existingWishlistBook = wishlistBook(
            userId = userId,
            bookId = bookId,
            wishlistItemId = wishlistItemId,
            now = now,
            title = "Dune",
            authors = listOf("Frank Herbert"),
            notes = "Original note",
        )
        val shelfRepository = RecordingShelfRepository()
        val wishlistRepository = RecordingWishlistRepository(
            wishlistBookByIsbn = existingWishlistBook,
        )
        val useCase = SaveWishlistItemUseCase(
            shelfRepository = shelfRepository,
            wishlistRepository = wishlistRepository,
            clock = { now },
            idProvider = {
                throw AssertionError("No new ids should be needed when reusing the existing wishlist ISBN")
            },
        )

        val result = useCase(
            SaveWishlistItemCommand(
                userId = userId,
                title = "Dune",
                author = "",
                notes = "Updated note",
                isbn = "978-0-441-17271-9",
            )
        )

        assertEquals(
            SaveWishlistItemResult.Success(wishlistItemId = wishlistItemId),
            result,
        )
        assertEquals(bookId, wishlistRepository.savedBook?.id)
        assertEquals(wishlistItemId, wishlistRepository.savedWishlistItem?.id)
        assertEquals(bookId, wishlistRepository.savedWishlistItem?.bookId)
        assertEquals("Updated note", wishlistRepository.savedWishlistItem?.notes)
        assertEquals(now, wishlistRepository.savedWishlistItem?.createdAt)
        assertEquals(listOf("Frank Herbert"), wishlistRepository.savedBook?.authors)
    }

    private fun ownedShelfBook(
        userId: UUID,
        bookId: UUID,
        now: Instant,
        title: String = "Dune",
        authors: List<String> = listOf("Frank Herbert"),
    ): ShelfBook {
        return ShelfBook(
            book = Book(
                id = bookId,
                userId = userId,
                title = title,
                subtitle = null,
                authors = authors,
                description = null,
                publisher = null,
                publishedDate = null,
                pageCount = null,
                thumbnailUrl = null,
                createdAt = now,
                updatedAt = now,
            ),
            ownership = Ownership(
                id = UUID.fromString("00000000-0000-0000-0000-000000000599"),
                userId = userId,
                bookId = bookId,
                quantity = 1,
                status = OwnershipStatus.OWNED,
                readStatus = ReadStatus.UNREAD,
                dateAdded = now,
                archivedAt = null,
                notes = null,
                createdAt = now,
                updatedAt = now,
            ),
            identifiers = listOf(
                BookIdentifier(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000598"),
                    bookId = bookId,
                    type = IdentifierType.ISBN_13,
                    value = "9780441172719",
                ),
                BookIdentifier(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000596"),
                    bookId = bookId,
                    type = IdentifierType.ISBN_10,
                    value = "0441172717",
                )
            ),
        )
    }

    private fun wishlistBook(
        userId: UUID,
        bookId: UUID,
        wishlistItemId: UUID,
        now: Instant,
        title: String,
        authors: List<String>,
        notes: String?,
    ): WishlistBook {
        return WishlistBook(
            item = WishlistItem(
                id = wishlistItemId,
                userId = userId,
                bookId = bookId,
                notes = notes,
                createdAt = now,
                updatedAt = now,
            ),
            book = Book(
                id = bookId,
                userId = userId,
                title = title,
                subtitle = null,
                authors = authors,
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
                    id = UUID.fromString("00000000-0000-0000-0000-000000000597"),
                    bookId = bookId,
                    type = IdentifierType.ISBN_13,
                    value = "9780441172719",
                ),
                BookIdentifier(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000595"),
                    bookId = bookId,
                    type = IdentifierType.ISBN_10,
                    value = "0441172717",
                )
            ),
        )
    }

    private class RecordingShelfRepository(
        private val exactOwnedBook: ShelfBook? = null,
    ) : ShelfRepository {
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
            isbn: ValidatedIsbn,
        ): ShelfBook? = exactOwnedBook
    }

    private class RecordingWishlistRepository(
        private val wishlistBookById: WishlistBook? = null,
        private val wishlistBookByIsbn: WishlistBook? = null,
    ) : WishlistRepository {
        var savedBook: Book? = null
        var savedWishlistItem: WishlistItem? = null
        var savedIdentifiers: List<BookIdentifier> = emptyList()

        override fun observeWishlistBooks(userId: UUID): Flow<List<WishlistBook>> = flowOf(emptyList())

        override suspend fun getWishlistBookById(
            userId: UUID,
            wishlistItemId: UUID,
        ): WishlistBook? = wishlistBookById?.takeIf { it.item.id == wishlistItemId }

        override suspend fun saveWishlistBook(
            book: Book,
            wishlistItem: WishlistItem,
            identifiers: List<BookIdentifier>,
        ) {
            savedBook = book
            savedWishlistItem = wishlistItem
            savedIdentifiers = identifiers
        }

        override suspend fun deleteWishlistItem(wishlistItemId: UUID): WishlistBook? = null

        override suspend fun findWishlistBookByExactIsbn(
            userId: UUID,
            isbn: ValidatedIsbn,
        ): WishlistBook? = wishlistBookByIsbn
    }
}
