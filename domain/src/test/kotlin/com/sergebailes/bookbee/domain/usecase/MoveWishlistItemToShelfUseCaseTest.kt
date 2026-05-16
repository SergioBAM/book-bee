package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.model.Ownership
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

class MoveWishlistItemToShelfUseCaseTest {
    @Test
    fun `creates ownership from a wishlist item and removes the wishlist entry`() = runBlocking {
        val now = Instant.parse("2026-05-16T10:15:30Z")
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000401")
        val bookId = UUID.fromString("00000000-0000-0000-0000-000000000402")
        val wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000403")
        val wishlistBook = WishlistBook(
            item = WishlistItem(
                id = wishlistItemId,
                userId = userId,
                bookId = bookId,
                notes = "Prefer paperback",
                createdAt = now,
                updatedAt = now,
            ),
            book = Book(
                id = bookId,
                userId = userId,
                title = "The Left Hand of Darkness",
                subtitle = null,
                authors = listOf("Ursula K. Le Guin"),
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
                    id = UUID.fromString("00000000-0000-0000-0000-000000000404"),
                    bookId = bookId,
                    type = IdentifierType.ISBN_13,
                    value = "9780441478125",
                )
            ),
        )
        val shelfRepository = RecordingShelfRepository()
        val wishlistRepository = RecordingWishlistRepository(wishlistBook)
        val useCase = MoveWishlistItemToShelfUseCase(
            shelfRepository = shelfRepository,
            wishlistRepository = wishlistRepository,
            clock = { now },
            idProvider = { UUID.fromString("00000000-0000-0000-0000-000000000405") },
        )

        val result = useCase(
            MoveWishlistItemToShelfCommand(
                userId = userId,
                wishlistItemId = wishlistItemId,
                notes = "  Prefer hardcover now  ",
                readStatus = ReadStatus.READING,
            )
        )

        assertEquals(
            MoveWishlistItemToShelfResult.Success(
                message = "\"The Left Hand of Darkness\" added to Shelf and removed from Wishlist.",
            ),
            result,
        )
        assertEquals(bookId, shelfRepository.createdBook?.id)
        assertEquals("Prefer hardcover now", shelfRepository.createdOwnership?.notes)
        assertEquals(ReadStatus.READING, shelfRepository.createdOwnership?.readStatus)
        assertEquals(wishlistItemId, wishlistRepository.deletedWishlistItemId)
    }

    @Test
    fun `returns not found when the wishlist item no longer exists`() = runBlocking {
        val useCase = MoveWishlistItemToShelfUseCase(
            shelfRepository = RecordingShelfRepository(),
            wishlistRepository = RecordingWishlistRepository(null),
            clock = { Instant.parse("2026-05-16T10:15:30Z") },
            idProvider = { UUID.randomUUID() },
        )

        val result = useCase(
            MoveWishlistItemToShelfCommand(
                userId = UUID.randomUUID(),
                wishlistItemId = UUID.randomUUID(),
                notes = "",
            )
        )

        assertEquals(MoveWishlistItemToShelfResult.WishlistItemNotFound, result)
    }

    private class RecordingShelfRepository : ShelfRepository {
        var createdBook: Book? = null
        var createdOwnership: Ownership? = null

        override suspend fun createBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) = Unit

        override suspend fun createOwnershipForExistingBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) {
            createdBook = book
            createdOwnership = ownership
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
        private val wishlistBook: WishlistBook?,
    ) : WishlistRepository {
        var deletedWishlistItemId: UUID? = null

        override fun observeWishlistBooks(userId: UUID): Flow<List<WishlistBook>> {
            return flowOf(wishlistBook?.let(::listOf) ?: emptyList())
        }

        override suspend fun getWishlistBookById(
            userId: UUID,
            wishlistItemId: UUID,
        ): WishlistBook? = wishlistBook?.takeIf { it.item.id == wishlistItemId }

        override suspend fun saveWishlistBook(
            book: Book,
            wishlistItem: WishlistItem,
            identifiers: List<BookIdentifier>,
        ) = Unit

        override suspend fun deleteWishlistItem(wishlistItemId: UUID): WishlistBook? {
            deletedWishlistItemId = wishlistItemId
            return wishlistBook?.takeIf { it.item.id == wishlistItemId }
        }

        override suspend fun findWishlistBookByExactIsbn(
            userId: UUID,
            isbn: com.sergebailes.bookbee.domain.isbn.ValidatedIsbn,
        ): WishlistBook? = null
    }
}
