package com.sergebailes.bookbee.domain.model

import java.time.Instant
import java.util.UUID

data class UserProfile(
    val id: UUID,
    val displayName: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
