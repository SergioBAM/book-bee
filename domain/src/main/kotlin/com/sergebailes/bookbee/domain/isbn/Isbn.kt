package com.sergebailes.bookbee.domain.isbn

import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.normalization.normalizeIsbn

data class ValidatedIsbn(
    val value: String,
    val type: IdentifierType,
)

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
