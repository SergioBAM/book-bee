package com.sergebailes.bookbee.data.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverters
import com.sergebailes.bookbee.data.database.converter.BookBeeTypeConverters
import com.sergebailes.bookbee.data.database.dao.BookDao
import com.sergebailes.bookbee.data.database.dao.BookIdentifierDao
import com.sergebailes.bookbee.data.database.dao.MetadataLookupCacheDao
import com.sergebailes.bookbee.data.database.dao.OwnershipDao
import com.sergebailes.bookbee.data.database.dao.UserProfileDao
import com.sergebailes.bookbee.data.database.dao.WishlistItemDao
import com.sergebailes.bookbee.data.database.entity.BookEntity
import com.sergebailes.bookbee.data.database.entity.BookIdentifierEntity
import com.sergebailes.bookbee.data.database.entity.MetadataLookupCacheEntity
import com.sergebailes.bookbee.data.database.entity.OwnershipEntity
import com.sergebailes.bookbee.data.database.entity.UserProfileEntity
import com.sergebailes.bookbee.data.database.entity.WishlistItemEntity

@Database(
    entities = [
        UserProfileEntity::class,
        BookEntity::class,
        BookIdentifierEntity::class,
        OwnershipEntity::class,
        WishlistItemEntity::class,
        MetadataLookupCacheEntity::class,
    ],
    version = BookBeeDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(BookBeeTypeConverters::class)
abstract class BookBeeDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bookDao(): BookDao
    abstract fun bookIdentifierDao(): BookIdentifierDao
    abstract fun ownershipDao(): OwnershipDao
    abstract fun wishlistItemDao(): WishlistItemDao
    abstract fun metadataLookupCacheDao(): MetadataLookupCacheDao

    companion object {
        const val VERSION = 1
        const val DATABASE_NAME = "book-bee.db"
    }
}
