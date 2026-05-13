package com.sergebailes.bookbee.data.repository

import com.sergebailes.bookbee.data.database.dao.UserProfileDao
import com.sergebailes.bookbee.data.database.entity.UserProfileEntity
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomUserProfileRepositoryTest {
    @Test
    fun `returns the existing default user when one is already stored`() {
        val existingUser = UserProfileEntity(
            id = UUID.randomUUID(),
            displayName = "Serge",
            createdAt = java.time.Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = java.time.Instant.parse("2026-01-01T00:00:00Z"),
        )
        val repository = RoomUserProfileRepository(
            userProfileDao = FakeUserProfileDao(mutableListOf(existingUser)),
        )

        val user = kotlinx.coroutines.runBlocking {
            repository.getOrCreateDefaultUser()
        }

        assertEquals(existingUser.id, user.id)
        assertEquals(existingUser.displayName, user.displayName)
    }

    @Test
    fun `creates a default local user when storage is empty`() {
        val dao = FakeUserProfileDao()
        val repository = RoomUserProfileRepository(userProfileDao = dao)

        val user = kotlinx.coroutines.runBlocking {
            repository.getOrCreateDefaultUser()
        }

        assertEquals(1, dao.users.size)
        assertEquals(user.id, dao.users.single().id)
        assertNull(user.displayName)
    }

    private class FakeUserProfileDao(
        val users: MutableList<UserProfileEntity> = mutableListOf(),
    ) : UserProfileDao {
        override suspend fun insert(userProfile: UserProfileEntity) {
            users.removeAll { it.id == userProfile.id }
            users += userProfile
        }

        override suspend fun update(userProfile: UserProfileEntity) {
            insert(userProfile)
        }

        override suspend fun getById(id: UUID): UserProfileEntity? {
            return users.firstOrNull { it.id == id }
        }

        override suspend fun getAll(): List<UserProfileEntity> {
            return users.toList()
        }
    }
}
