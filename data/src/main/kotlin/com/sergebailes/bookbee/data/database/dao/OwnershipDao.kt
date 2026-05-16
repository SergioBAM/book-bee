package com.sergebailes.bookbee.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.sergebailes.bookbee.data.database.entity.OwnershipEntity
import com.sergebailes.bookbee.data.database.entity.OwnershipStatus
import java.util.UUID

@Dao
interface OwnershipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ownership: OwnershipEntity)

    @Update
    suspend fun update(ownership: OwnershipEntity)

    @Query("SELECT * FROM ownership WHERE id = :id")
    suspend fun getById(id: UUID): OwnershipEntity?

    @Query(
        "SELECT * FROM ownership " +
            "WHERE userId = :userId AND status = :status ORDER BY dateAdded DESC"
    )
    suspend fun getByUserIdAndStatus(
        userId: UUID,
        status: OwnershipStatus,
    ): List<OwnershipEntity>

    @Query(
        "SELECT * FROM ownership " +
            "WHERE userId = :userId AND bookId = :bookId ORDER BY createdAt DESC"
    )
    suspend fun getByUserIdAndBookId(
        userId: UUID,
        bookId: UUID,
    ): List<OwnershipEntity>

    @Query("SELECT COUNT(*) FROM ownership WHERE bookId = :bookId")
    suspend fun countByBookId(bookId: UUID): Int
}
