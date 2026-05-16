package com.sergebailes.bookbee

import com.sergebailes.bookbee.data.database.BookBeeDatabase
import com.sergebailes.bookbee.data.repository.RoomShelfRepository
import com.sergebailes.bookbee.data.repository.RoomUserProfileRepository
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.UserProfileRepository
import com.sergebailes.bookbee.domain.usecase.CreateManualShelfBookUseCase

class BookBeeAppContainer(
    database: BookBeeDatabase,
) {
    val userProfileRepository: UserProfileRepository = RoomUserProfileRepository(
        userProfileDao = database.userProfileDao(),
    )
    val shelfRepository: ShelfRepository = RoomShelfRepository(database)
    val createManualShelfBookUseCase = CreateManualShelfBookUseCase(shelfRepository)
}
