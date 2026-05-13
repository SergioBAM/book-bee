package com.sergebailes.bookbee.data.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "metadata_lookup_cache",
    indices = [
        Index(value = ["provider", "lookupType", "lookupValue"], unique = true),
    ],
)
data class MetadataLookupCacheEntity(
    @PrimaryKey
    val id: UUID,
    val provider: MetadataProvider,
    val lookupType: LookupType,
    val lookupValue: String,
    val responseJson: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
