package com.sergebailes.bookbee.domain.model

import java.time.Instant
import java.util.UUID

data class Ownership(
    val id: UUID,
    val userId: UUID,
    val bookId: UUID,
    val quantity: Int,
    val status: OwnershipStatus,
    val readStatus: ReadStatus,
    val dateAdded: Instant,
    val archivedAt: Instant?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(quantity > 0) { "quantity must be greater than zero" }
    }
}
