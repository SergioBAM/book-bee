package com.sergebailes.bookbee.ui.wishlist
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.OwnershipStatus
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.model.UserProfile
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.model.WishlistItem
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.UserProfileRepository
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import com.sergebailes.bookbee.domain.usecase.DeleteWishlistItemUseCase
import com.sergebailes.bookbee.domain.usecase.MoveWishlistItemToShelfUseCase
import com.sergebailes.bookbee.domain.usecase.SaveWishlistItemUseCase
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful removal deletes immediately and exposes generic undo feedback`() = runTest(dispatcher) {
        val firstItem = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000701"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000702"),
            title = "Ancillary Justice",
            createdAt = Instant.parse("2026-05-10T10:00:00Z"),
        )
        val secondItem = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000703"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000704"),
            title = "The Dispossessed",
            createdAt = Instant.parse("2026-05-09T10:00:00Z"),
        )
        val wishlistRepository = FakeWishlistRepository(listOf(firstItem, secondItem))
        val viewModel = createViewModel(wishlistRepository = wishlistRepository)

        advanceUntilIdle()
        viewModel.onDeleteWishlistItemClicked(firstItem.item.id)
        advanceUntilIdle()

        assertEquals(listOf(secondItem.item.id), viewModel.uiState.value.items.map(WishlistListItem::id))
        assertEquals(
            WishlistRemovalFeedback(
                id = 1L,
                message = "Wishlist item removed.",
                actionLabel = "Undo",
            ),
            viewModel.uiState.value.removalFeedback,
        )
        assertNull(viewModel.uiState.value.message)
        assertEquals(listOf(firstItem.item.id), wishlistRepository.deletedWishlistItemIds)
    }

    @Test
    fun `undo restores the exact item in the same recency order`() = runTest(dispatcher) {
        val firstItem = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000711"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000712"),
            title = "A Memory Called Empire",
            createdAt = Instant.parse("2026-05-10T10:00:00Z"),
            updatedAt = Instant.parse("2026-05-10T12:00:00Z"),
        )
        val secondItem = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000713"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000714"),
            title = "The Telling",
            createdAt = Instant.parse("2026-05-09T10:00:00Z"),
        )
        val wishlistRepository = FakeWishlistRepository(listOf(firstItem, secondItem))
        val viewModel = createViewModel(wishlistRepository = wishlistRepository)

        advanceUntilIdle()
        viewModel.onDeleteWishlistItemClicked(firstItem.item.id)
        advanceUntilIdle()
        viewModel.onUndoWishlistRemovalClicked()
        advanceUntilIdle()

        assertEquals(
            listOf(firstItem.item.id, secondItem.item.id),
            viewModel.uiState.value.items.map(WishlistListItem::id),
        )
        assertNull(viewModel.uiState.value.removalFeedback)
        assertEquals(firstItem.item, wishlistRepository.savedWishlistItems.single().item)
        assertEquals(firstItem.identifiers, wishlistRepository.savedWishlistItems.single().identifiers)
        assertEquals(firstItem.book, wishlistRepository.savedWishlistItems.single().book)
    }

    @Test
    fun `newer removal replaces older pending undo state`() = runTest(dispatcher) {
        val firstItem = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000721"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000722"),
            title = "Use of Weapons",
            createdAt = Instant.parse("2026-05-10T10:00:00Z"),
        )
        val secondItem = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000723"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000724"),
            title = "The Fifth Season",
            createdAt = Instant.parse("2026-05-09T10:00:00Z"),
        )
        val wishlistRepository = FakeWishlistRepository(listOf(firstItem, secondItem))
        val viewModel = createViewModel(wishlistRepository = wishlistRepository)

        advanceUntilIdle()
        viewModel.onDeleteWishlistItemClicked(firstItem.item.id)
        advanceUntilIdle()
        viewModel.onDeleteWishlistItemClicked(secondItem.item.id)
        advanceUntilIdle()

        assertEquals(
            WishlistRemovalFeedback(
                id = 2L,
                message = "Wishlist item removed.",
                actionLabel = "Undo",
            ),
            viewModel.uiState.value.removalFeedback,
        )

        viewModel.onUndoWishlistRemovalClicked()
        advanceUntilIdle()

        assertEquals(listOf(secondItem.item.id), viewModel.uiState.value.items.map(WishlistListItem::id))
        assertTrue(viewModel.uiState.value.items.none { it.id == firstItem.item.id })
        assertEquals(secondItem.item, wishlistRepository.savedWishlistItems.single().item)
        assertNull(viewModel.uiState.value.removalFeedback)
    }

    @Test
    fun `undo does not restore deleted row when same isbn was readded`() = runTest(dispatcher) {
        val deletedItem = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000751"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000752"),
            title = "Dune",
            isbnValue = "9780441172719",
            createdAt = Instant.parse("2026-05-10T10:00:00Z"),
        )
        val readdedItem = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000753"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000754"),
            title = "Dune",
            isbnValue = "9780441172719",
            createdAt = Instant.parse("2026-05-11T10:00:00Z"),
        )
        val wishlistRepository = FakeWishlistRepository(listOf(deletedItem))
        val viewModel = createViewModel(wishlistRepository = wishlistRepository)

        advanceUntilIdle()
        viewModel.onDeleteWishlistItemClicked(deletedItem.item.id)
        advanceUntilIdle()
        wishlistRepository.saveWishlistBook(
            book = readdedItem.book,
            wishlistItem = readdedItem.item,
            identifiers = readdedItem.identifiers,
        )
        advanceUntilIdle()
        viewModel.onUndoWishlistRemovalClicked()
        advanceUntilIdle()

        assertEquals(listOf(readdedItem.item.id), viewModel.uiState.value.items.map(WishlistListItem::id))
        assertEquals("Wishlist already contains this ISBN.", viewModel.uiState.value.message)
        assertEquals(listOf(readdedItem.item.id), wishlistRepository.wishlistItemIds())
    }

    @Test
    fun `entering add form clears pending removal feedback and disables undo`() = runTest(dispatcher) {
        val item = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000761"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000762"),
            title = "The Left Hand of Darkness",
            createdAt = Instant.parse("2026-05-10T10:00:00Z"),
        )
        val wishlistRepository = FakeWishlistRepository(listOf(item))
        val viewModel = createViewModel(wishlistRepository = wishlistRepository)

        advanceUntilIdle()
        viewModel.onDeleteWishlistItemClicked(item.item.id)
        advanceUntilIdle()
        viewModel.onAddWishlistItemClicked()
        viewModel.onUndoWishlistRemovalClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isShowingForm)
        assertNull(viewModel.uiState.value.removalFeedback)
        assertEquals(emptyList<WishlistListItem>(), viewModel.uiState.value.items)
        assertTrue(wishlistRepository.savedWishlistItems.isEmpty())
    }

    @Test
    fun `failed removal keeps item visible and shows inline failure`() = runTest(dispatcher) {
        val item = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000731"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000732"),
            title = "The Goblin Emperor",
            createdAt = Instant.parse("2026-05-10T10:00:00Z"),
        )
        val wishlistRepository = FakeWishlistRepository(listOf(item), failDelete = true)
        val viewModel = createViewModel(wishlistRepository = wishlistRepository)

        advanceUntilIdle()
        viewModel.onDeleteWishlistItemClicked(item.item.id)
        advanceUntilIdle()

        assertEquals(listOf(item.item.id), viewModel.uiState.value.items.map(WishlistListItem::id))
        assertEquals("Wishlist item could not be removed.", viewModel.uiState.value.message)
        assertNull(viewModel.uiState.value.removalFeedback)
    }

    @Test
    fun `removing an on shelf wishlist row does not mutate shelf ownership`() = runTest(dispatcher) {
        val item = wishlistBook(
            wishlistItemId = UUID.fromString("00000000-0000-0000-0000-000000000741"),
            bookId = UUID.fromString("00000000-0000-0000-0000-000000000742"),
            title = "Paladin of Souls",
            createdAt = Instant.parse("2026-05-10T10:00:00Z"),
        )
        val shelfRepository = FakeShelfRepository(
            shelfBooks = listOf(
                shelfBook(
                    book = item.book,
                    ownershipId = UUID.fromString("00000000-0000-0000-0000-000000000743"),
                )
            )
        )
        val wishlistRepository = FakeWishlistRepository(listOf(item))
        val viewModel = createViewModel(
            wishlistRepository = wishlistRepository,
            shelfRepository = shelfRepository,
        )

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.items.single().isOnShelf)

        viewModel.onDeleteWishlistItemClicked(item.item.id)
        advanceUntilIdle()

        assertEquals(0, shelfRepository.mutationCount)
        assertEquals(emptyList<WishlistListItem>(), viewModel.uiState.value.items)
        assertEquals("Wishlist item removed.", viewModel.uiState.value.removalFeedback?.message)
    }

    private fun createViewModel(
        wishlistRepository: FakeWishlistRepository,
        shelfRepository: FakeShelfRepository = FakeShelfRepository(),
        userProfileRepository: UserProfileRepository = FakeUserProfileRepository(),
    ): WishlistViewModel {
        return WishlistViewModel(
            userProfileRepository = userProfileRepository,
            shelfRepository = shelfRepository,
            wishlistRepository = wishlistRepository,
            saveWishlistItemUseCase = SaveWishlistItemUseCase(
                shelfRepository = shelfRepository,
                wishlistRepository = wishlistRepository,
                clock = { Instant.parse("2026-05-17T00:00:00Z") },
                idProvider = { UUID.fromString("00000000-0000-0000-0000-000000000799") },
            ),
            deleteWishlistItemUseCase = DeleteWishlistItemUseCase(wishlistRepository),
            moveWishlistItemToShelfUseCase = MoveWishlistItemToShelfUseCase(
                shelfRepository = shelfRepository,
                wishlistRepository = wishlistRepository,
                clock = { Instant.parse("2026-05-17T00:00:00Z") },
                idProvider = { UUID.fromString("00000000-0000-0000-0000-000000000798") },
            ),
        )
    }

    private fun wishlistBook(
        wishlistItemId: UUID,
        bookId: UUID,
        title: String,
        isbnValue: String = "978000000000${title.length % 10}",
        createdAt: Instant,
        updatedAt: Instant = createdAt,
    ): WishlistBook {
        val userId = UUID.fromString("00000000-0000-0000-0000-000000000700")
        return WishlistBook(
            item = WishlistItem(
                id = wishlistItemId,
                userId = userId,
                bookId = bookId,
                notes = "First edition if possible",
                createdAt = createdAt,
                updatedAt = updatedAt,
            ),
            book = Book(
                id = bookId,
                userId = userId,
                title = title,
                subtitle = null,
                authors = listOf("Author"),
                description = null,
                publisher = null,
                publishedDate = null,
                pageCount = null,
                thumbnailUrl = null,
                createdAt = createdAt,
                updatedAt = updatedAt,
            ),
            identifiers = listOf(
                BookIdentifier(
                    id = UUID.nameUUIDFromBytes(bookId.toString().toByteArray()),
                    bookId = bookId,
                    type = IdentifierType.ISBN_13,
                    value = isbnValue,
                )
            ),
        )
    }

    private fun shelfBook(
        book: Book,
        ownershipId: UUID,
    ): ShelfBook {
        val now = Instant.parse("2026-05-11T00:00:00Z")
        return ShelfBook(
            book = book,
            ownership = Ownership(
                id = ownershipId,
                userId = book.userId,
                bookId = book.id,
                quantity = 1,
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

    private class FakeUserProfileRepository : UserProfileRepository {
        override suspend fun getOrCreateDefaultUser(): UserProfile {
            val now = Instant.parse("2026-05-01T00:00:00Z")
            return UserProfile(
                id = UUID.fromString("00000000-0000-0000-0000-000000000700"),
                displayName = null,
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    private class FakeShelfRepository(
        shelfBooks: List<ShelfBook> = emptyList(),
    ) : ShelfRepository {
        private val shelfBooksFlow = MutableStateFlow(shelfBooks)
        var mutationCount = 0
            private set

        override suspend fun createBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) {
            mutationCount += 1
        }

        override suspend fun createOwnershipForExistingBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) {
            mutationCount += 1
        }

        override suspend fun updateBook(
            book: Book,
            ownership: Ownership,
            identifiers: List<BookIdentifier>,
        ) {
            mutationCount += 1
        }

        override suspend fun archiveOwnership(
            ownershipId: UUID,
            archivedAt: Instant,
        ) {
            mutationCount += 1
        }

        override fun observeOwnedBooks(userId: UUID): Flow<List<ShelfBook>> {
            return shelfBooksFlow
        }

        override suspend fun getBookDetailById(
            userId: UUID,
            bookId: UUID,
        ): ShelfBook? = shelfBooksFlow.value.firstOrNull { it.book.id == bookId }

        override suspend fun findOwnedBookByExactIsbn(
            userId: UUID,
            isbn: com.sergebailes.bookbee.domain.isbn.ValidatedIsbn,
        ): ShelfBook? = null
    }

    private class FakeWishlistRepository(
        wishlistBooks: List<WishlistBook>,
        private val failDelete: Boolean = false,
    ) : WishlistRepository {
        private val wishlistBooksFlow = MutableStateFlow(sortWishlistBooks(wishlistBooks))
        val deletedWishlistItemIds = mutableListOf<UUID>()
        val savedWishlistItems = mutableListOf<WishlistBook>()

        fun wishlistItemIds(): List<UUID> {
            return wishlistBooksFlow.value.map { it.item.id }
        }

        override fun observeWishlistBooks(userId: UUID): Flow<List<WishlistBook>> {
            return wishlistBooksFlow.map { items ->
                items.filter { it.item.userId == userId }
            }
        }

        override suspend fun getWishlistBookById(
            userId: UUID,
            wishlistItemId: UUID,
        ): WishlistBook? {
            return wishlistBooksFlow.value.firstOrNull {
                it.item.userId == userId && it.item.id == wishlistItemId
            }
        }

        override suspend fun saveWishlistBook(
            book: Book,
            wishlistItem: WishlistItem,
            identifiers: List<BookIdentifier>,
        ) {
            val savedWishlistBook = WishlistBook(
                item = wishlistItem,
                book = book,
                identifiers = identifiers,
            )
            savedWishlistItems += savedWishlistBook
            wishlistBooksFlow.value = sortWishlistBooks(
                wishlistBooksFlow.value
                    .filterNot { it.item.id == wishlistItem.id }
                    .plus(savedWishlistBook)
            )
        }

        override suspend fun deleteWishlistItem(wishlistItemId: UUID): WishlistBook? {
            if (failDelete) {
                throw IllegalStateException("delete failed")
            }

            val deletedWishlistBook = wishlistBooksFlow.value.firstOrNull { it.item.id == wishlistItemId }
                ?: return null
            deletedWishlistItemIds += wishlistItemId
            wishlistBooksFlow.value = wishlistBooksFlow.value.filterNot { it.item.id == wishlistItemId }
            return deletedWishlistBook
        }

        override suspend fun findWishlistBookByExactIsbn(
            userId: UUID,
            isbn: com.sergebailes.bookbee.domain.isbn.ValidatedIsbn,
        ): WishlistBook? {
            return wishlistBooksFlow.value.firstOrNull { wishlistBook ->
                wishlistBook.item.userId == userId &&
                    wishlistBook.identifiers.any { identifier ->
                        identifier.type == isbn.type && identifier.value == isbn.value
                    }
            }
        }

        companion object {
            private fun sortWishlistBooks(items: List<WishlistBook>): List<WishlistBook> {
                return items.sortedByDescending { it.item.createdAt }
            }
        }
    }
}
