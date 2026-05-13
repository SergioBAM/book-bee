package com.sergebailes.bookbee.data.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("userId"),
        Index("normalizedTitle"),
    ],
)
data class BookEntity(
    @PrimaryKey
    val id: UUID,
    val userId: UUID,
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val normalizedTitle: String,
    val normalizedAuthors: List<String>,
    val description: String?,
    val publisher: String?,
    val publishedDate: String?,
    val pageCount: Int?,
    val thumbnailUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
