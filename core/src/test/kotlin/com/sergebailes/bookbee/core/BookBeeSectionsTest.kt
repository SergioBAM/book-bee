package com.sergebailes.bookbee.core

import org.junit.Assert.assertEquals
import org.junit.Test

class BookBeeSectionsTest {
    @Test
    fun sectionsStayInExpectedOrder() {
        assertEquals(
            listOf("Shelf", "Scan", "Wishlist", "History"),
            BookBeeSections.all.map(BookBeeSection::title)
        )
    }
}
