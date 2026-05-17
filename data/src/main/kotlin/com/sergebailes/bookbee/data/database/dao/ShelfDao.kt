package com.sergebailes.bookbee.data.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import com.sergebailes.bookbee.data.database.entity.OwnershipStatus
import com.sergebailes.bookbee.data.database.relation.ShelfBookRelation
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfDao {
    @Transaction
    @Query(
        "SELECT * FROM ownership " +
            "WHERE userId = :userId AND status = :status ORDER BY dateAdded DESC"
    )
    fun observeByUserIdAndStatus(
        userId: UUID,
        status: OwnershipStatus,
    ): Flow<List<ShelfBookRelation>>

    @Transaction
    @Query(
        "SELECT * FROM ownership " +
            "WHERE userId = :userId AND status = :status " +
            "ORDER BY archivedAt DESC, dateAdded DESC"
    )
    fun observeHistoryByUserIdAndStatus(
        userId: UUID,
        status: OwnershipStatus,
    ): Flow<List<ShelfBookRelation>>

    @Transaction
    @Query(
        "SELECT * FROM ownership " +
            "WHERE userId = :userId AND bookId = :bookId AND status = :status " +
            "ORDER BY createdAt DESC LIMIT 1"
    )
    suspend fun getByUserIdAndBookIdAndStatus(
        userId: UUID,
        bookId: UUID,
        status: OwnershipStatus,
    ): ShelfBookRelation?

    @Transaction
    @Query(
        "SELECT * FROM ownership " +
            "WHERE id = :ownershipId AND userId = :userId AND status = :status " +
            "LIMIT 1"
    )
    suspend fun getByOwnershipIdAndUserIdAndStatus(
        ownershipId: UUID,
        userId: UUID,
        status: OwnershipStatus,
    ): ShelfBookRelation?
}
