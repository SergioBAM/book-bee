package com.sergebailes.bookbee.data.repository

import com.sergebailes.bookbee.data.database.dao.UserProfileDao
import com.sergebailes.bookbee.data.repository.mapper.toDataModel
import com.sergebailes.bookbee.data.repository.mapper.toDomainModel
import com.sergebailes.bookbee.domain.model.UserProfile
import com.sergebailes.bookbee.domain.repository.UserProfileRepository
import java.time.Instant
import java.util.UUID

class RoomUserProfileRepository(
    private val userProfileDao: UserProfileDao,
) : UserProfileRepository {
    override suspend fun getOrCreateDefaultUser(): UserProfile {
        val existingUser = userProfileDao.getAll().firstOrNull()
        if (existingUser != null) {
            return existingUser.toDomainModel()
        }

        val now = Instant.now()
        val defaultUser = UserProfile(
            id = UUID.randomUUID(),
            displayName = null,
            createdAt = now,
            updatedAt = now,
        )

        userProfileDao.insert(defaultUser.toDataModel())
        return defaultUser
    }
}
