package com.sergebailes.bookbee.domain.normalization

import org.junit.Assert.assertEquals
import org.junit.Test

class BookNormalizationTest {
    @Test
    fun normalizeTitleStripsPunctuationCollapsesWhitespaceAndLowercases() {
        assertEquals(
            "do androids dream of electric sheep",
            normalizeTitle("  Do Androids Dream of Electric Sheep?  ")
        )
    }

    @Test
    fun normalizeAuthorStripsPunctuationCollapsesWhitespaceAndLowercases() {
        assertEquals(
            "ursula k le guin",
            normalizeAuthor("Ursula K. Le Guin")
        )
    }

    @Test
    fun normalizeAuthorsRemovesBlankValuesAfterNormalization() {
        assertEquals(
            listOf("neil gaiman", "terry pratchett"),
            normalizeAuthors(listOf("Neil Gaiman", "   ", "Terry Pratchett"))
        )
    }

    @Test
    fun normalizeIsbnRemovesNonIsbnSeparators() {
        assertEquals("9781400033416", normalizeIsbn("978-1-4000-3341-6"))
    }

    @Test
    fun normalizeIsbnKeepsXCheckDigitUppercased() {
        assertEquals("043942089X", normalizeIsbn("0-439-42089-x"))
    }
}
