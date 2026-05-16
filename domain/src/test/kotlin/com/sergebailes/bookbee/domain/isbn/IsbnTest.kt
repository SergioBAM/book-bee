package com.sergebailes.bookbee.domain.isbn

import com.sergebailes.bookbee.domain.model.IdentifierType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IsbnTest {
    @Test
    fun `parses a normalized valid isbn 13`() {
        assertEquals(
            ValidatedIsbn(
                value = "9781400033416",
                type = IdentifierType.ISBN_13,
            ),
            parseIsbn("978-1-4000-3341-6"),
        )
    }

    @Test
    fun `rejects an isbn with an invalid checksum`() {
        assertNull(parseIsbn("9781400033417"))
    }
}
