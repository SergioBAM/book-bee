package com.sergebailes.bookbee.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.UUID

class BookTest {
    @Test
    fun bookDefaultsNormalizedFieldsFromTitleAndAuthors() {
        val book = Book(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            title = "The Hobbit: Or, There and Back Again",
            subtitle = null,
            authors = listOf("J. R. R. Tolkien"),
            description = null,
            publisher = null,
            publishedDate = null,
            pageCount = null,
            thumbnailUrl = null,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

        assertEquals("the hobbit or there and back again", book.normalizedTitle)
        assertEquals(listOf("j r r tolkien"), book.normalizedAuthors)
    }
}
