package com.sergebailes.bookbee.domain.isbn

import com.sergebailes.bookbee.domain.model.BookIdentifier
import java.util.UUID

fun buildIsbnIdentifiers(
    bookId: UUID,
    isbn: ValidatedIsbn?,
    existingIdentifiers: List<BookIdentifier>,
    idProvider: () -> UUID,
): List<BookIdentifier> {
    val identifiersForBook = existingIdentifiers.filter { it.bookId == bookId }
    if (isbn == null) {
        return identifiersForBook
    }

    val existingKeys = identifiersForBook.map { it.type to it.value }.toSet()
    val missingIdentifiers = isbn.exactIdentityForms()
        .filterNot { it.type to it.value in existingKeys }
        .map { isbnForm ->
            BookIdentifier(
                id = idProvider(),
                bookId = bookId,
                type = isbnForm.type,
                value = isbnForm.value,
            )
        }

    return identifiersForBook + missingIdentifiers
}
