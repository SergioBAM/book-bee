package com.sergebailes.bookbee.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.sergebailes.bookbee.data.database.entity.WishlistItemEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wishlistItem: WishlistItemEntity)

    @Update
    suspend fun update(wishlistItem: WishlistItemEntity)

    @Query("SELECT * FROM wishlist_items WHERE id = :id")
    suspend fun getById(id: UUID): WishlistItemEntity?

    @Query("SELECT * FROM wishlist_items WHERE userId = :userId AND bookId = :bookId LIMIT 1")
    suspend fun getByUserIdAndBookId(
        userId: UUID,
        bookId: UUID,
    ): WishlistItemEntity?

    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getByUserId(userId: UUID): List<WishlistItemEntity>

    @Transaction
    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeRelationsByUserId(userId: UUID): Flow<List<com.sergebailes.bookbee.data.database.relation.WishlistBookRelation>>

    @Transaction
    @Query("SELECT * FROM wishlist_items WHERE id = :id LIMIT 1")
    suspend fun getRelationById(id: UUID): com.sergebailes.bookbee.data.database.relation.WishlistBookRelation?

    @Transaction
    @Query("SELECT * FROM wishlist_items WHERE userId = :userId AND bookId = :bookId LIMIT 1")
    suspend fun getRelationByUserIdAndBookId(
        userId: UUID,
        bookId: UUID,
    ): com.sergebailes.bookbee.data.database.relation.WishlistBookRelation?

    @Query("SELECT COUNT(*) FROM wishlist_items WHERE bookId = :bookId")
    suspend fun countByBookId(bookId: UUID): Int

    @Query("DELETE FROM wishlist_items WHERE bookId = :bookId")
    suspend fun deleteByBookId(bookId: UUID): Int

    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteById(id: UUID)
}
