package com.sergebailes.bookbee.core

data class BookBeeSection(
    val title: String,
    val headline: String,
    val supportingText: String
)

object BookBeeSections {
    val all: List<BookBeeSection> = listOf(
        BookBeeSection(
            title = "Shelf",
            headline = "Your owned books will live here.",
            supportingText = "This section will become the local-first shelf for searching and checking what you already own."
        ),
        BookBeeSection(
            title = "Scan",
            headline = "The ownership check starts here.",
            supportingText = "This placeholder marks the future scan flow where ISBN results will answer the core question quickly."
        ),
        BookBeeSection(
            title = "Wishlist",
            headline = "Books you want without marking them owned.",
            supportingText = "Wishlist keeps lightweight intent with notes, while Shelf stays the stronger truth once a book becomes yours."
        )
    )
}
