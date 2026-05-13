package com.sergebailes.bookbee.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.sergebailes.bookbee.data.database.entity.BookIdentifierEntity
import com.sergebailes.bookbee.data.database.entity.IdentifierType
import java.util.UUID

@Dao
interface BookIdentifierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(identifier: BookIdentifierEntity)

    @Update
    suspend fun update(identifier: BookIdentifierEntity)

    @Query("SELECT * FROM book_identifiers WHERE bookId = :bookId ORDER BY type ASC, value ASC")
    suspend fun getByBookId(bookId: UUID): List<BookIdentifierEntity>

    @Query(
        "SELECT * FROM book_identifiers " +
            "WHERE type = :type AND value = :value ORDER BY id ASC"
    )
    suspend fun findByTypeAndValue(
        type: IdentifierType,
        value: String,
    ): List<BookIdentifierEntity>
}
