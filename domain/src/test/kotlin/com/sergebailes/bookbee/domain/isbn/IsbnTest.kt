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
    fun `derives isbn 10 from an equivalent isbn 13`() {
        val isbn = parseIsbn("978-0-441-17271-9")

        assertEquals(
            listOf(
                ValidatedIsbn("9780441172719", IdentifierType.ISBN_13),
                ValidatedIsbn("0441172717", IdentifierType.ISBN_10),
            ),
            isbn?.exactIdentityForms(),
        )
    }

    @Test
    fun `derives isbn 13 from an equivalent isbn 10`() {
        val isbn = parseIsbn("0-9752298-0-X")

        assertEquals(
            listOf(
                ValidatedIsbn("097522980X", IdentifierType.ISBN_10),
                ValidatedIsbn("9780975229804", IdentifierType.ISBN_13),
            ),
            isbn?.exactIdentityForms(),
        )
    }

    @Test
    fun `keeps a valid isbn 13 without an isbn 10 pair as itself`() {
        val isbn = parseIsbn("9791090636071")

        assertEquals(
            listOf(ValidatedIsbn("9791090636071", IdentifierType.ISBN_13)),
            isbn?.exactIdentityForms(),
        )
    }

    @Test
    fun `rejects an isbn with an invalid checksum`() {
        assertNull(parseIsbn("9781400033417"))
    }
}
