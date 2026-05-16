package com.sergebailes.bookbee.domain.model

import org.junit.Test
import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals

class WishlistItemTest {
    @Test
    fun notesRemainOptionalAndNoPriorityFieldIsRequired() {
        val item = WishlistItem(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            bookId = UUID.randomUUID(),
            notes = null,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

        assertEquals(null, item.notes)
    }
}
