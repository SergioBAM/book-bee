package com.sergebailes.bookbee.domain.normalization

import java.util.Locale

private val separatorRegex = Regex("[^\\p{L}\\p{N}]+")
private val whitespaceRegex = Regex("\\s+")
private val isbnCharacterRegex = Regex("[^0-9X]")

fun normalizeTitle(title: String): String = normalizeText(title)

fun normalizeAuthor(author: String): String = normalizeText(author)

fun normalizeAuthors(authors: Iterable<String>): List<String> =
    authors
        .map(::normalizeAuthor)
        .filter(String::isNotBlank)

fun normalizeIsbn(rawIsbn: String): String =
    rawIsbn
        .trim()
        .uppercase(Locale.ROOT)
        .replace(isbnCharacterRegex, "")

private fun normalizeText(value: String): String =
    value
        .lowercase(Locale.ROOT)
        .replace(separatorRegex, " ")
        .replace(whitespaceRegex, " ")
        .trim()
