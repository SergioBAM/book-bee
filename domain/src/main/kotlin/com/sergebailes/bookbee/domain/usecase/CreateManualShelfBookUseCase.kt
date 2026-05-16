package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.isbn.parseIsbn
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.OwnershipStatus
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import java.time.Instant
import java.util.UUID

data class CreateManualShelfBookCommand(
    val userId: UUID,
    val title: String,
    val author: String,
    val notes: String,
    val isbn: String,
    val readStatus: ReadStatus = ReadStatus.UNREAD,
)

sealed interface CreateManualShelfBookResult {
    data object Success : CreateManualShelfBookResult

    data class ValidationFailed(
        val titleError: String? = null,
        val isbnError: String? = null,
    ) : CreateManualShelfBookResult
}

class CreateManualShelfBookUseCase(
    private val shelfRepository: ShelfRepository,
    private val clock: () -> Instant = Instant::now,
    private val idProvider: () -> UUID = UUID::randomUUID,
) {
    suspend operator fun invoke(command: CreateManualShelfBookCommand): CreateManualShelfBookResult {
        val trimmedTitle = command.title.trim()
        if (trimmedTitle.isBlank()) {
            return CreateManualShelfBookResult.ValidationFailed(
                titleError = "Title is required",
            )
        }
        val validatedIsbn = command.isbn.trim()
            .takeIf(String::isNotBlank)
            ?.let(::parseIsbn)
        if (command.isbn.isNotBlank() && validatedIsbn == null) {
            return CreateManualShelfBookResult.ValidationFailed(
                isbnError = "Enter a valid ISBN-10 or ISBN-13",
            )
        }

        val now = clock()
        val bookId = idProvider()
        val book = Book(
            id = bookId,
            userId = command.userId,
            title = trimmedTitle,
            subtitle = null,
            authors = command.author.trim().takeIf(String::isNotBlank)?.let(::listOf) ?: emptyList(),
            description = null,
            publisher = null,
            publishedDate = null,
            pageCount = null,
            thumbnailUrl = null,
            createdAt = now,
            updatedAt = now,
        )
        val ownership = Ownership(
            id = idProvider(),
            userId = command.userId,
            bookId = bookId,
            quantity = 1,
            status = OwnershipStatus.OWNED,
            readStatus = command.readStatus,
            dateAdded = now,
            archivedAt = null,
            notes = command.notes.trim().takeIf(String::isNotBlank),
            createdAt = now,
            updatedAt = now,
        )
        val identifiers = validatedIsbn?.let { isbn ->
            listOf(
                BookIdentifier(
                    id = idProvider(),
                    bookId = bookId,
                    type = isbn.type,
                    value = isbn.value,
                )
            )
        } ?: emptyList()

        shelfRepository.createBook(
            book = book,
            ownership = ownership,
            identifiers = identifiers,
        )

        return CreateManualShelfBookResult.Success
    }
}
