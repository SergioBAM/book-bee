package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.isbn.parseIsbn
import com.sergebailes.bookbee.domain.isbn.exactIdentityForms
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.normalization.normalizeIsbn
import com.sergebailes.bookbee.domain.normalization.normalizeTitle
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

enum class LibrarySearchBadge {
    ON_SHELF,
    WISHLIST,
}

enum class LibrarySearchTarget {
    SHELF,
    WISHLIST,
    HISTORY,
}

data class LibrarySearchResult(
    val bookId: UUID,
    val title: String,
    val authorLine: String?,
    val isbn: String?,
    val badges: Set<LibrarySearchBadge>,
    val target: LibrarySearchTarget,
    val relevance: Int,
    val shelfBook: ShelfBook? = null,
    val wishlistBook: WishlistBook? = null,
    val archivedBook: ShelfBook? = null,
)

class SearchActiveLibraryUseCase(
    private val shelfRepository: ShelfRepository,
    private val wishlistRepository: WishlistRepository,
) {
    operator fun invoke(
        userId: UUID,
        query: String,
    ): Flow<List<LibrarySearchResult>> {
        return combine(
            shelfRepository.observeOwnedBooks(userId),
            wishlistRepository.observeWishlistBooks(userId),
        ) { shelfBooks, wishlistBooks ->
            searchActive(
                query = query,
                shelfBooks = shelfBooks,
                wishlistBooks = wishlistBooks,
            )
        }
    }
}

class SearchHistoryUseCase(
    private val shelfRepository: ShelfRepository,
) {
    operator fun invoke(
        userId: UUID,
        query: String,
    ): Flow<List<LibrarySearchResult>> {
        return shelfRepository.observeArchivedBooks(userId).map { archivedBooks ->
            searchHistory(
                query = query,
                archivedBooks = archivedBooks,
            )
        }
    }
}

internal fun searchActive(
    query: String,
    shelfBooks: List<ShelfBook>,
    wishlistBooks: List<WishlistBook>,
): List<LibrarySearchResult> {
    val normalizedQuery = normalizeSearchQuery(query)
    val isbnQuery = query.takeIf(String::isNotBlank)?.let(::parseIsbn)
    val booksById = linkedMapOf<UUID, ActiveContexts>()

    shelfBooks.forEach { shelfBook ->
        val contexts = booksById.getOrPut(shelfBook.book.id) { ActiveContexts(book = shelfBook.book) }
        contexts.shelfBook = shelfBook
    }
    wishlistBooks.forEach { wishlistBook ->
        val contexts = booksById.getOrPut(wishlistBook.book.id) { ActiveContexts(book = wishlistBook.book) }
        contexts.wishlistBook = wishlistBook
    }

    return booksById.values
        .mapNotNull { contexts ->
            val allIdentifiers = contexts.shelfBook?.identifiers ?: contexts.wishlistBook?.identifiers.orEmpty()
            val score = activeScore(
                normalizedQuery = normalizedQuery,
                isbnQuery = isbnQuery,
                book = contexts.book,
                identifiers = allIdentifiers,
                shelfBook = contexts.shelfBook,
                wishlistBook = contexts.wishlistBook,
            )
            if (score == null) {
                null
            } else {
                LibrarySearchResult(
                    bookId = contexts.book.id,
                    title = contexts.book.title,
                    authorLine = contexts.book.authorLine,
                    isbn = allIdentifiers.displayIsbn(),
                    badges = buildSet {
                        if (contexts.shelfBook != null) add(LibrarySearchBadge.ON_SHELF)
                        if (contexts.wishlistBook != null) add(LibrarySearchBadge.WISHLIST)
                    },
                    target = if (contexts.shelfBook != null) {
                        LibrarySearchTarget.SHELF
                    } else {
                        LibrarySearchTarget.WISHLIST
                    },
                    relevance = score,
                    shelfBook = contexts.shelfBook,
                    wishlistBook = contexts.wishlistBook,
                )
            }
        }
        .sortedWith(
            compareByDescending<LibrarySearchResult> { it.relevance }
                .thenByDescending { LibrarySearchBadge.ON_SHELF in it.badges }
                .thenBy { it.title.lowercase() }
        )
}

internal fun searchHistory(
    query: String,
    archivedBooks: List<ShelfBook>,
): List<LibrarySearchResult> {
    val normalizedQuery = normalizeSearchQuery(query)
    val isbnQuery = query.takeIf(String::isNotBlank)?.let(::parseIsbn)

    return archivedBooks
        .mapNotNull { archivedBook ->
            val score = historyScore(
                normalizedQuery = normalizedQuery,
                isbnQuery = isbnQuery,
                shelfBook = archivedBook,
            )
            if (score == null) {
                null
            } else {
                LibrarySearchResult(
                    bookId = archivedBook.book.id,
                    title = archivedBook.book.title,
                    authorLine = archivedBook.book.authorLine,
                    isbn = archivedBook.identifiers.displayIsbn(),
                    badges = emptySet(),
                    target = LibrarySearchTarget.HISTORY,
                    relevance = score,
                    archivedBook = archivedBook,
                )
            }
        }
        .sortedWith(
            compareByDescending<LibrarySearchResult> { it.relevance }
                .thenByDescending { it.archivedBook?.ownership?.archivedAt }
                .thenByDescending { it.archivedBook?.ownership?.dateAdded }
                .thenBy { it.title.lowercase() }
        )
}

private data class ActiveContexts(
    val book: Book,
    var shelfBook: ShelfBook? = null,
    var wishlistBook: WishlistBook? = null,
)

private fun activeScore(
    normalizedQuery: String,
    isbnQuery: com.sergebailes.bookbee.domain.isbn.ValidatedIsbn?,
    book: Book,
    identifiers: List<BookIdentifier>,
    shelfBook: ShelfBook?,
    wishlistBook: WishlistBook?,
): Int? {
    if (normalizedQuery.isBlank() && isbnQuery == null) {
        return 0
    }

    val exactIsbn = isbnQuery?.exactlyMatches(identifiers) == true
    if (exactIsbn) {
        return 1_000
    }

    val normalizedIsbnQuery = normalizeIsbn(normalizedQuery)
    val isbnContains = normalizedIsbnQuery.isNotBlank() &&
        identifiers.any { it.value.contains(normalizedIsbnQuery) }
    val textMatches = textMatches(
        normalizedQuery = normalizedQuery,
        book = book,
        notes = listOfNotNull(shelfBook?.ownership?.notes, wishlistBook?.item?.notes),
    )

    return when {
        isbnContains -> 700
        textMatches -> 500
        else -> null
    }
}

private fun historyScore(
    normalizedQuery: String,
    isbnQuery: com.sergebailes.bookbee.domain.isbn.ValidatedIsbn?,
    shelfBook: ShelfBook,
): Int? {
    if (normalizedQuery.isBlank() && isbnQuery == null) {
        return 0
    }

    if (isbnQuery?.exactlyMatches(shelfBook.identifiers) == true) {
        return 1_000
    }

    val normalizedIsbnQuery = normalizeIsbn(normalizedQuery)
    val isbnContains = normalizedIsbnQuery.isNotBlank() &&
        shelfBook.identifiers.any { it.value.contains(normalizedIsbnQuery) }
    val textMatches = textMatches(
        normalizedQuery = normalizedQuery,
        book = shelfBook.book,
        notes = listOfNotNull(shelfBook.ownership.notes),
    )

    return when {
        isbnContains -> 700
        textMatches -> 500
        else -> null
    }
}

private fun textMatches(
    normalizedQuery: String,
    book: Book,
    notes: List<String>,
): Boolean {
    if (normalizedQuery.isBlank()) {
        return false
    }

    return book.normalizedTitle.contains(normalizedQuery) ||
        book.normalizedAuthors.any { it.contains(normalizedQuery) } ||
        notes.any { normalizeTitle(it).contains(normalizedQuery) }
}

private fun com.sergebailes.bookbee.domain.isbn.ValidatedIsbn.exactlyMatches(
    identifiers: List<BookIdentifier>,
): Boolean {
    val exactKeys = exactIdentityForms()
        .map { it.type to it.value }
        .toSet()
    return identifiers.any { it.type to it.value in exactKeys }
}

private fun List<BookIdentifier>.displayIsbn(): String? {
    return firstOrNull { it.type == IdentifierType.ISBN_13 }?.value
        ?: firstOrNull { it.type == IdentifierType.ISBN_10 }?.value
}

private fun normalizeSearchQuery(query: String): String = normalizeTitle(query)

private val Book.authorLine: String?
    get() = authors.takeIf(List<String>::isNotEmpty)?.joinToString(", ")
