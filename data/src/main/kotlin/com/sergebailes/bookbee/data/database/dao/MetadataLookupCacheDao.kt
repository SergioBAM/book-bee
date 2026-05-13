package com.sergebailes.bookbee.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.sergebailes.bookbee.data.database.entity.LookupType
import com.sergebailes.bookbee.data.database.entity.MetadataLookupCacheEntity
import com.sergebailes.bookbee.data.database.entity.MetadataProvider

@Dao
interface MetadataLookupCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cacheEntry: MetadataLookupCacheEntity)

    @Update
    suspend fun update(cacheEntry: MetadataLookupCacheEntity)

    @Query(
        "SELECT * FROM metadata_lookup_cache " +
            "WHERE provider = :provider AND lookupType = :lookupType AND lookupValue = :lookupValue"
    )
    suspend fun getByLookup(
        provider: MetadataProvider,
        lookupType: LookupType,
        lookupValue: String,
    ): MetadataLookupCacheEntity?
}
