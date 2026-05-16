package com.sergebailes.bookbee.domain.model

data class WishlistBook(
    val item: WishlistItem,
    val book: Book,
    val identifiers: List<BookIdentifier>,
)
