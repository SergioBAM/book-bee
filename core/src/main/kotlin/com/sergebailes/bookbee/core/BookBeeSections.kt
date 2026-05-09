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
            headline = "Books to revisit later.",
            supportingText = "This section will hold books you want to track without adding them to the owned shelf."
        )
    )
}
