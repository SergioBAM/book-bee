package com.sergebailes.bookbee.domain.model

import org.junit.Test
import java.time.Instant
import java.util.UUID

class WishlistItemTest {
    @Test(expected = IllegalArgumentException::class)
    fun priorityMustBeGreaterThanZeroWhenProvided() {
        WishlistItem(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            bookId = UUID.randomUUID(),
            notes = null,
            priority = 0,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
    }
}
