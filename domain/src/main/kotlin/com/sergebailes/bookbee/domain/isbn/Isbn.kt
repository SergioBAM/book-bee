package com.sergebailes.bookbee.domain.isbn

import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.normalization.normalizeIsbn

data class ValidatedIsbn(
    val value: String,
    val type: IdentifierType,
)

fun ValidatedIsbn.exactIdentityForms(): List<ValidatedIsbn> {
    val pairedIsbn = pairedIsbn()
    return if (pairedIsbn == null) {
        listOf(this)
    } else {
        listOf(this, pairedIsbn)
    }
}

fun ValidatedIsbn.pairedIsbn(): ValidatedIsbn? {
    return when (type) {
        IdentifierType.ISBN_10 -> toIsbn13()
        IdentifierType.ISBN_13 -> toIsbn10()
        IdentifierType.GOOGLE_BOOKS_ID,
        IdentifierType.OPEN_LIBRARY_ID,
        IdentifierType.OTHER -> null
    }
}

fun parseIsbn(rawValue: String): ValidatedIsbn? {
    val normalizedValue = normalizeIsbn(rawValue)

    return when {
        normalizedValue.length == 10 && isValidIsbn10(normalizedValue) ->
            ValidatedIsbn(normalizedValue, IdentifierType.ISBN_10)

        normalizedValue.length == 13 && isValidIsbn13(normalizedValue) ->
            ValidatedIsbn(normalizedValue, IdentifierType.ISBN_13)

        else -> null
    }
}

private fun isValidIsbn10(value: String): Boolean {
    if (!value.dropLast(1).all(Char::isDigit)) {
        return false
    }

    val checksum = value.mapIndexed { index, character ->
        val digit = when {
            character.isDigit() -> character.digitToInt()
            index == value.lastIndex && character == 'X' -> 10
            else -> return false
        }
        (10 - index) * digit
    }.sum()

    return checksum % 11 == 0
}

private fun isValidIsbn13(value: String): Boolean {
    if (!value.all(Char::isDigit)) {
        return false
    }

    val checksum = value.mapIndexed { index, character ->
        val weight = if (index % 2 == 0) 1 else 3
        character.digitToInt() * weight
    }.sum()

    return checksum % 10 == 0
}

private fun ValidatedIsbn.toIsbn13(): ValidatedIsbn? {
    if (type != IdentifierType.ISBN_10) {
        return null
    }

    val core = "978${value.take(9)}"
    val checksum = calculateIsbn13CheckDigit(core)
    return ValidatedIsbn(
        value = "$core$checksum",
        type = IdentifierType.ISBN_13,
    )
}

private fun ValidatedIsbn.toIsbn10(): ValidatedIsbn? {
    if (type != IdentifierType.ISBN_13 || !value.startsWith("978")) {
        return null
    }

    val core = value.substring(startIndex = 3, endIndex = 12)
    val checksum = calculateIsbn10CheckDigit(core)
    return ValidatedIsbn(
        value = "$core$checksum",
        type = IdentifierType.ISBN_10,
    )
}

private fun calculateIsbn13CheckDigit(firstTwelveDigits: String): Int {
    val checksum = firstTwelveDigits.mapIndexed { index, character ->
        val weight = if (index % 2 == 0) 1 else 3
        character.digitToInt() * weight
    }.sum()

    return (10 - checksum % 10) % 10
}

private fun calculateIsbn10CheckDigit(firstNineDigits: String): Char {
    val checksum = firstNineDigits.mapIndexed { index, character ->
        (10 - index) * character.digitToInt()
    }.sum()
    val checkValue = (11 - checksum % 11) % 11

    return if (checkValue == 10) 'X' else checkValue.digitToChar()
}
