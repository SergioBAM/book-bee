package com.sergebailes.bookbee.data.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "ownership",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("userId"),
        Index("bookId"),
        Index("status"),
        Index(value = ["userId", "bookId"]),
    ],
)
data class OwnershipEntity(
    @PrimaryKey
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
)
