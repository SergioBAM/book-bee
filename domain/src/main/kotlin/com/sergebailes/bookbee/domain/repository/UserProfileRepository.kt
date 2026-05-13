package com.sergebailes.bookbee.domain.repository

import com.sergebailes.bookbee.domain.model.UserProfile

interface UserProfileRepository {
    suspend fun getOrCreateDefaultUser(): UserProfile
}
