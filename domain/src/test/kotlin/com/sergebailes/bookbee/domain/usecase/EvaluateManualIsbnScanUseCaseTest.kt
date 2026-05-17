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
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateManualIsbnScanUseCaseTest {
    @Test
    fun `valid isbn returns owned for exact active ownership`() = runBlocking {
        val ownedBook = shelfBook(status = OwnershipStatus.OWNED)
        val useCase = EvaluateManualIsbnScanUseCase(
            shelfRepository = RecordingShelfRepository(ownedBook = ownedBook),
            wishlistRepository = RecordingWishlistRepository(),
        )

        val result = useCase(userId, "9780441172719")

        assertTrue((result as ManualIsbnScanResult.Success).ownership is ScanOwnershipResult.Owned)
    }

    @Test
    fun `valid isbn returns not owned with previous context for exact archived ownership`() = runBlocking {
        val archivedBook = shelfBook(status = OwnershipStatus.ARCHIVED)
        val useCase = EvaluateManualIsbnScanUseCase(
            shelfRepository = RecordingShelfRepository(archivedBook = archivedBook),
            wishlistRepository = RecordingWishlistRepository(),
        )

        val result = useCase(userId, "9780441172719")

        val ownership = (result as ManualIsbnScanResult.Success).ownership as ScanOwnershipResult.NotOwned
        assertEquals(archivedBook.book.id, ownership.previouslyOwnedBook?.book?.id)
    }

    @Test
    fun `wishlist context is separate from ownership state`() = runBlocking {
        val wishlistBook = wishlistBook()
        val useCase = EvaluateManualIsbnScanUseCase(
            shelfRepository = RecordingShelfRepository(),
            wishlistRepository = RecordingWishlistRepository(wishlistBook = wishlistBook),
        )

        val result = useCase(userId, "9780441172719")

        val success = result as ManualIsbnScanResult.Success
        assertTrue(success.ownership is ScanOwnershipResult.NotOwned)
        assertEquals(wishlistBook.book.id, success.wishlistBook?.book?.id)
    }

    @Test
    fun `invalid isbn does not query repositories`() = runBlocking {
        val shelfRepository = RecordingShelfRepository()
        val wishlistRepository = RecordingWishlistRepository()
        val useCase = EvaluateManualIsbnScanUseCase(
            shelfRepository = shelfRepository,
            wishlistRepository = wishlistRepository,
        )

        val result = useCase(userId, "not an isbn")

        assertTrue(result is ManualIsbnScanResult.ValidationFailed)
        assertNull(shelfRepository.lastOwnedIsbn)
        assertNull(wishlistRepository.lastWishlistIsbn)
    }

    private class RecordingShelfRepository(
        private val ownedBook: ShelfBook? = null,
        private val archivedBook: ShelfBook? = null,
    ) : ShelfRepository {
        var lastOwnedIsbn: ValidatedIsbn? = null

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
        ): ShelfBook? {
            lastOwnedIsbn = isbn
            return ownedBook
        }

        override suspend fun findArchivedBookByExactIsbn(
            userId: UUID,
            isbn: ValidatedIsbn,
        ): ShelfBook? = archivedBook
    }

    private class RecordingWishlistRepository(
        private val wishlistBook: WishlistBook? = null,
    ) : WishlistRepository {
        var lastWishlistIsbn: ValidatedIsbn? = null

        override fun observeWishlistBooks(userId: UUID): Flow<List<WishlistBook>> = emptyFlow()

        override suspend fun getWishlistBookById(
            userId: UUID,
            wishlistItemId: UUID,
        ): WishlistBook? = null

        override suspend fun saveWishlistBook(
            book: Book,
            wishlistItem: WishlistItem,
            identifiers: List<BookIdentifier>,
        ) = Unit

        override suspend fun deleteWishlistItem(wishlistItemId: UUID): WishlistBook? = null

        override suspend fun findWishlistBookByExactIsbn(
            userId: UUID,
            isbn: ValidatedIsbn,
        ): WishlistBook? {
            lastWishlistIsbn = isbn
            return wishlistBook
        }
    }

    companion object {
        private val userId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000801")
        private val bookId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000802")
        private val now: Instant = Instant.parse("2026-05-17T00:00:00Z")

        private fun shelfBook(status: OwnershipStatus): ShelfBook {
            return ShelfBook(
                book = book(title = "Dune"),
                ownership = Ownership(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000803"),
                    userId = userId,
                    bookId = bookId,
                    quantity = 1,
                    status = status,
                    readStatus = ReadStatus.READ,
                    dateAdded = now,
                    archivedAt = if (status == OwnershipStatus.ARCHIVED) now else null,
                    notes = "Pocket copy",
                    createdAt = now,
                    updatedAt = now,
                ),
                identifiers = identifiers(bookId),
            )
        }

        private fun wishlistBook(): WishlistBook {
            return WishlistBook(
                item = WishlistItem(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000804"),
                    userId = userId,
                    bookId = bookId,
                    notes = "Want a hardcover",
                    createdAt = now,
                    updatedAt = now,
                ),
                book = book(title = "Dune"),
                identifiers = identifiers(bookId),
            )
        }

        private fun book(title: String): Book {
            return Book(
                id = bookId,
                userId = userId,
                title = title,
                subtitle = null,
                authors = listOf("Frank Herbert"),
                description = null,
                publisher = null,
                publishedDate = null,
                pageCount = null,
                thumbnailUrl = null,
                createdAt = now,
                updatedAt = now,
            )
        }

        private fun identifiers(bookId: UUID): List<BookIdentifier> {
            return listOf(
                BookIdentifier(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000805"),
                    bookId = bookId,
                    type = IdentifierType.ISBN_13,
                    value = "9780441172719",
                )
            )
        }
    }
}
