package com.sergebailes.bookbee.data.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val id: UUID,
    val displayName: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
