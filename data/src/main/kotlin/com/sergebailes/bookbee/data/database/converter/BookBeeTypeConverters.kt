package com.sergebailes.bookbee.data.database.converter

import androidx.room3.TypeConverter
import com.sergebailes.bookbee.data.database.entity.IdentifierType
import com.sergebailes.bookbee.data.database.entity.LookupType
import com.sergebailes.bookbee.data.database.entity.MetadataProvider
import com.sergebailes.bookbee.data.database.entity.OwnershipStatus
import com.sergebailes.bookbee.data.database.entity.ReadStatus
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import java.util.UUID

class BookBeeTypeConverters {
    @TypeConverter
    fun fromUuid(value: UUID?): String? = value?.toString()

    @TypeConverter
    fun toUuid(value: String?): UUID? = value?.let(UUID::fromString)

    @TypeConverter
    fun fromInstant(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let(Instant::parse)

    @TypeConverter
    fun fromIdentifierType(value: IdentifierType?): String? = value?.name

    @TypeConverter
    fun toIdentifierType(value: String?): IdentifierType? = value?.let(IdentifierType::valueOf)

    @TypeConverter
    fun fromOwnershipStatus(value: OwnershipStatus?): String? = value?.name

    @TypeConverter
    fun toOwnershipStatus(value: String?): OwnershipStatus? = value?.let(OwnershipStatus::valueOf)

    @TypeConverter
    fun fromReadStatus(value: ReadStatus?): String? = value?.name

    @TypeConverter
    fun toReadStatus(value: String?): ReadStatus? = value?.let(ReadStatus::valueOf)

    @TypeConverter
    fun fromMetadataProvider(value: MetadataProvider?): String? = value?.name

    @TypeConverter
    fun toMetadataProvider(value: String?): MetadataProvider? = value?.let(MetadataProvider::valueOf)

    @TypeConverter
    fun fromLookupType(value: LookupType?): String? = value?.name

    @TypeConverter
    fun toLookupType(value: String?): LookupType? = value?.let(LookupType::valueOf)

    @TypeConverter
    fun fromStringList(values: List<String>?): String? {
        return values?.joinToString(separator = ",") { value ->
            Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
        }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        if (value.isEmpty()) return emptyList()

        return value.split(",").map { encoded ->
            String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8)
        }
    }
}
