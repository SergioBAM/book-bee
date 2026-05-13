package com.sergebailes.bookbee.domain.model

data class ShelfBook(
    val book: Book,
    val ownership: Ownership,
    val identifiers: List<BookIdentifier>,
)
