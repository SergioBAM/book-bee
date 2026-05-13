package com.sergebailes.bookbee.data.repository.mapper

import com.sergebailes.bookbee.data.database.entity.BookEntity
import com.sergebailes.bookbee.data.database.entity.BookIdentifierEntity
import com.sergebailes.bookbee.data.database.entity.IdentifierType
import com.sergebailes.bookbee.data.database.entity.OwnershipEntity
import com.sergebailes.bookbee.data.database.entity.OwnershipStatus
import com.sergebailes.bookbee.data.database.entity.ReadStatus
import com.sergebailes.bookbee.data.database.relation.BookWithIdentifiersRelation
import com.sergebailes.bookbee.data.database.relation.ShelfBookRelation
import com.sergebailes.bookbee.domain.model.OwnershipStatus as DomainOwnershipStatus
import com.sergebailes.bookbee.domain.model.ReadStatus as DomainReadStatus
import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class ShelfMappersTest {
    @Test
    fun `maps a Room shelf relation into the domain aggregate`() {
        val userId = UUID.randomUUID()
        val bookId = UUID.randomUUID()
        val ownershipId = UUID.randomUUID()
        val identifierId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-01-01T00:00:00Z")
        val relation = ShelfBookRelation(
            ownership = OwnershipEntity(
                id = ownershipId,
                userId = userId,
                bookId = bookId,
                quantity = 2,
                status = OwnershipStatus.OWNED,
                readStatus = ReadStatus.READING,
                dateAdded = createdAt,
                archivedAt = null,
                notes = "Signed copy",
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
            book = BookWithIdentifiersRelation(
                book = BookEntity(
                    id = bookId,
                    userId = userId,
                    title = "Dune",
                    subtitle = null,
                    authors = listOf("Frank Herbert"),
                    normalizedTitle = "dune",
                    normalizedAuthors = listOf("frank herbert"),
                    description = null,
                    publisher = null,
                    publishedDate = null,
                    pageCount = null,
                    thumbnailUrl = null,
                    createdAt = createdAt,
                    updatedAt = createdAt,
                ),
                identifiers = listOf(
                    BookIdentifierEntity(
                        id = identifierId,
                        bookId = bookId,
                        type = IdentifierType.ISBN_13,
                        value = "9780441013593",
                    )
                ),
            ),
        )

        val shelfBook = relation.toDomainModel()

        assertEquals(bookId, shelfBook.book.id)
        assertEquals(ownershipId, shelfBook.ownership.id)
        assertEquals(DomainOwnershipStatus.OWNED, shelfBook.ownership.status)
        assertEquals(DomainReadStatus.READING, shelfBook.ownership.readStatus)
        assertEquals("9780441013593", shelfBook.identifiers.single().value)
    }
}
