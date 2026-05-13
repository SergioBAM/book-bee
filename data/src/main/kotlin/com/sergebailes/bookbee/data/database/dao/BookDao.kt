package com.sergebailes.bookbee.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.sergebailes.bookbee.data.database.entity.BookEntity
import java.util.UUID

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity)

    @Update
    suspend fun update(book: BookEntity)

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getById(id: UUID): BookEntity?

    @Query("SELECT * FROM books WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getByUserId(userId: UUID): List<BookEntity>
}
