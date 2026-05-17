package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.OwnershipStatus
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.model.WishlistItem
import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchLibraryUseCasesTest {
    @Test
    fun `active search consolidates shelf and wishlist for shared book with badges`() {
        val sharedBook = book(
            id = UUID.fromString("00000000-0000-0000-0000-000000000821"),
            title = "Dune",
        )

        val results = searchActive(
            query = "dune",
            shelfBooks = listOf(shelfBook(sharedBook)),
            wishlistBooks = listOf(wishlistBook(sharedBook)),
        )

        assertEquals(1, results.size)
        assertEquals(setOf(LibrarySearchBadge.ON_SHELF, LibrarySearchBadge.WISHLIST), results.single().badges)
        assertEquals(LibrarySearchTarget.SHELF, results.single().target)
    }

    @Test
    fun `active search ranks exact isbn before text matches and shelf before wishlist on ties`() {
        val exactBook = book(
            id = UUID.fromString("00000000-0000-0000-0000-000000000822"),
            title = "Other Title",
        )
        val shelfTieBook = book(
            id = UUID.fromString("00000000-0000-0000-0000-000000000823"),
            title = "Foundation",
        )
        val wishlistTieBook = book(
            id = UUID.fromString("00000000-0000-0000-0000-000000000824"),
            title = "Foundation and Empire",
        )

        val results = searchActive(
            query = "9780441172719",
            shelfBooks = listOf(
                shelfBook(exactBook, isbn = "9780441172719"),
                shelfBook(shelfTieBook),
            ),
            wishlistBooks = listOf(wishlistBook(wishlistTieBook)),
        )

        assertEquals(exactBook.id, results.first().bookId)

        val tieResults = searchActive(
            query = "foundation",
            shelfBooks = listOf(shelfBook(shelfTieBook)),
            wishlistBooks = listOf(wishlistBook(wishlistTieBook)),
        )
        assertEquals(LibrarySearchTarget.SHELF, tieResults.first().target)
    }

    @Test
    fun `active search matches active notes but does not include archived notes`() {
        val activeBook = book(
            id = UUID.fromString("00000000-0000-0000-0000-000000000825"),
            title = "Plain Title",
        )
        val archivedBook = book(
            id = UUID.fromString("00000000-0000-0000-0000-000000000826"),
            title = "Archived Title",
        )

        val activeResults = searchActive(
            query = "signed",
            shelfBooks = listOf(shelfBook(activeBook, notes = "Signed copy")),
            wishlistBooks = emptyList(),
        )
        val excludedResults = searchActive(
            query = "archived",
            shelfBooks = emptyList(),
            wishlistBooks = emptyList(),
        )
        val historyResults = searchHistory(
            query = "archived",
            archivedBooks = listOf(shelfBook(archivedBook, status = OwnershipStatus.ARCHIVED, notes = "Archived note")),
        )

        assertEquals(activeBook.id, activeResults.single().bookId)
        assertTrue(excludedResults.isEmpty())
        assertEquals(archivedBook.id, historyResults.single().bookId)
        assertFalse(historyResults.single().badges.contains(LibrarySearchBadge.ON_SHELF))
    }

    private fun book(
        id: UUID,
        title: String,
    ): Book {
        return Book(
            id = id,
            userId = userId,
            title = title,
            subtitle = null,
            authors = listOf("Author"),
            description = null,
            publisher = null,
            publishedDate = null,
            pageCount = null,
            thumbnailUrl = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun shelfBook(
        book: Book,
        isbn: String = "9780000000002",
        status: OwnershipStatus = OwnershipStatus.OWNED,
        notes: String? = null,
    ): ShelfBook {
        return ShelfBook(
            book = book,
            ownership = Ownership(
                id = UUID.nameUUIDFromBytes("${book.id}-ownership".toByteArray()),
                userId = userId,
                bookId = book.id,
                quantity = 1,
                status = status,
                readStatus = ReadStatus.UNREAD,
                dateAdded = now,
                archivedAt = if (status == OwnershipStatus.ARCHIVED) now else null,
                notes = notes,
                createdAt = now,
                updatedAt = now,
            ),
            identifiers = identifiers(book.id, isbn),
        )
    }

    private fun wishlistBook(
        book: Book,
        isbn: String = "9780000000002",
    ): WishlistBook {
        return WishlistBook(
            item = WishlistItem(
                id = UUID.nameUUIDFromBytes("${book.id}-wishlist".toByteArray()),
                userId = userId,
                bookId = book.id,
                notes = null,
                createdAt = now,
                updatedAt = now,
            ),
            book = book,
            identifiers = identifiers(book.id, isbn),
        )
    }

    private fun identifiers(
        bookId: UUID,
        isbn: String,
    ): List<BookIdentifier> {
        return listOf(
            BookIdentifier(
                id = UUID.nameUUIDFromBytes("$bookId-$isbn".toByteArray()),
                bookId = bookId,
                type = IdentifierType.ISBN_13,
                value = isbn,
            )
        )
    }

    companion object {
        private val userId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000820")
        private val now: Instant = Instant.parse("2026-05-17T00:00:00Z")
    }
}
