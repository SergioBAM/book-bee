package com.sergebailes.bookbee.data.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "wishlist_items",
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
        Index(value = ["userId", "bookId"], unique = true),
    ],
)
data class WishlistItemEntity(
    @PrimaryKey
    val id: UUID,
    val userId: UUID,
    val bookId: UUID,
    val notes: String?,
    val priority: Int?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
