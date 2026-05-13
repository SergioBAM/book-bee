package com.sergebailes.bookbee.domain.model

import java.time.Instant
import java.util.UUID

data class WishlistItem(
    val id: UUID,
    val userId: UUID,
    val bookId: UUID,
    val notes: String?,
    val priority: Int?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(priority == null || priority > 0) { "priority must be greater than zero when provided" }
    }
}
