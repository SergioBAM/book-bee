package com.sergebailes.bookbee.domain.model

import org.junit.Test
import java.time.Instant
import java.util.UUID

class OwnershipTest {
    @Test(expected = IllegalArgumentException::class)
    fun quantityMustBeGreaterThanZero() {
        Ownership(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            bookId = UUID.randomUUID(),
            quantity = 0,
            status = OwnershipStatus.OWNED,
            readStatus = ReadStatus.UNREAD,
            dateAdded = Instant.parse("2026-01-01T00:00:00Z"),
            archivedAt = null,
            notes = null,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
    }
}
