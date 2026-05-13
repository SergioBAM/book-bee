package com.sergebailes.bookbee.data.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "book_identifiers",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("bookId"),
        Index(value = ["bookId", "type", "value"], unique = true),
        Index(value = ["type", "value"]),
    ],
)
data class BookIdentifierEntity(
    @PrimaryKey
    val id: UUID,
    val bookId: UUID,
    val type: IdentifierType,
    val value: String,
)
