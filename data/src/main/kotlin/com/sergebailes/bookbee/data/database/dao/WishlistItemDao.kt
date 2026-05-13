package com.sergebailes.bookbee.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.sergebailes.bookbee.data.database.entity.WishlistItemEntity
import java.util.UUID

@Dao
interface WishlistItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wishlistItem: WishlistItemEntity)

    @Update
    suspend fun update(wishlistItem: WishlistItemEntity)

    @Query("SELECT * FROM wishlist_items WHERE id = :id")
    suspend fun getById(id: UUID): WishlistItemEntity?

    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getByUserId(userId: UUID): List<WishlistItemEntity>
}
