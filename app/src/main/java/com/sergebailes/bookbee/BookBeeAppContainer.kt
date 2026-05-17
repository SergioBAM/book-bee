package com.sergebailes.bookbee

import com.sergebailes.bookbee.data.database.BookBeeDatabase
import com.sergebailes.bookbee.data.repository.RoomShelfRepository
import com.sergebailes.bookbee.data.repository.RoomUserProfileRepository
import com.sergebailes.bookbee.data.repository.RoomWishlistRepository
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.UserProfileRepository
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import com.sergebailes.bookbee.domain.usecase.AddShelfCopyUseCase
import com.sergebailes.bookbee.domain.usecase.ArchiveShelfBookUseCase
import com.sergebailes.bookbee.domain.usecase.CreateManualShelfBookUseCase
import com.sergebailes.bookbee.domain.usecase.DeleteWishlistItemUseCase
import com.sergebailes.bookbee.domain.usecase.MoveWishlistItemToShelfUseCase
import com.sergebailes.bookbee.domain.usecase.RemoveShelfCopyUseCase
import com.sergebailes.bookbee.domain.usecase.SaveWishlistItemUseCase
import com.sergebailes.bookbee.domain.usecase.UndoAddShelfCopyUseCase

class BookBeeAppContainer(
    database: BookBeeDatabase,
) {
    val userProfileRepository: UserProfileRepository = RoomUserProfileRepository(
        userProfileDao = database.userProfileDao(),
    )
    val shelfRepository: ShelfRepository = RoomShelfRepository(database)
    val wishlistRepository: WishlistRepository = RoomWishlistRepository(database)
    val createManualShelfBookUseCase = CreateManualShelfBookUseCase(
        shelfRepository = shelfRepository,
        wishlistRepository = wishlistRepository,
    )
    val saveWishlistItemUseCase = SaveWishlistItemUseCase(
        shelfRepository = shelfRepository,
        wishlistRepository = wishlistRepository,
    )
    val deleteWishlistItemUseCase = DeleteWishlistItemUseCase(wishlistRepository)
    val moveWishlistItemToShelfUseCase = MoveWishlistItemToShelfUseCase(
        shelfRepository = shelfRepository,
        wishlistRepository = wishlistRepository,
    )
    val addShelfCopyUseCase = AddShelfCopyUseCase(shelfRepository)
    val undoAddShelfCopyUseCase = UndoAddShelfCopyUseCase(shelfRepository)
    val removeShelfCopyUseCase = RemoveShelfCopyUseCase(shelfRepository)
    val archiveShelfBookUseCase = ArchiveShelfBookUseCase(shelfRepository)
}
