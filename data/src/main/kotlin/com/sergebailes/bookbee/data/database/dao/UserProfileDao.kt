package com.sergebailes.bookbee.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.sergebailes.bookbee.data.database.entity.UserProfileEntity
import java.util.UUID

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userProfile: UserProfileEntity)

    @Update
    suspend fun update(userProfile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getById(id: UUID): UserProfileEntity?

    @Query("SELECT * FROM user_profiles ORDER BY createdAt ASC")
    suspend fun getAll(): List<UserProfileEntity>
}
