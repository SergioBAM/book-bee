package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.model.OwnershipStatus
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import java.time.Instant
import java.util.UUID

sealed interface AddShelfCopyResult {
    data class Success(
        val title: String,
        val quantity: Int,
    ) : AddShelfCopyResult

    data object ShelfBookNotFound : AddShelfCopyResult
}

class AddShelfCopyUseCase(
    private val shelfRepository: ShelfRepository,
    private val clock: () -> Instant = Instant::now,
) {
    suspend operator fun invoke(
        userId: UUID,
        bookId: UUID,
    ): AddShelfCopyResult {
        val shelfBook = shelfRepository.getBookDetailById(
            userId = userId,
            bookId = bookId,
        ) ?: return AddShelfCopyResult.ShelfBookNotFound

        val now = clock()
        val updatedOwnership = shelfBook.ownership.copy(
            quantity = shelfBook.ownership.quantity + 1,
            updatedAt = now,
        )

        shelfRepository.updateBook(
            book = shelfBook.book.copy(updatedAt = now),
            ownership = updatedOwnership,
            identifiers = shelfBook.identifiers,
        )

        return AddShelfCopyResult.Success(
            title = shelfBook.book.title,
            quantity = updatedOwnership.quantity,
        )
    }
}

sealed interface UndoAddShelfCopyResult {
    data class Success(
        val title: String,
        val quantity: Int,
    ) : UndoAddShelfCopyResult

    data object ShelfBookNotFound : UndoAddShelfCopyResult
    data object CannotUndoSingleCopy : UndoAddShelfCopyResult
}

class UndoAddShelfCopyUseCase(
    private val shelfRepository: ShelfRepository,
    private val clock: () -> Instant = Instant::now,
) {
    suspend operator fun invoke(
        userId: UUID,
        bookId: UUID,
    ): UndoAddShelfCopyResult {
        val shelfBook = shelfRepository.getBookDetailById(
            userId = userId,
            bookId = bookId,
        ) ?: return UndoAddShelfCopyResult.ShelfBookNotFound

        if (shelfBook.ownership.quantity <= 1) {
            return UndoAddShelfCopyResult.CannotUndoSingleCopy
        }

        val now = clock()
        val updatedOwnership = shelfBook.ownership.copy(
            quantity = shelfBook.ownership.quantity - 1,
            updatedAt = now,
        )

        shelfRepository.updateBook(
            book = shelfBook.book.copy(updatedAt = now),
            ownership = updatedOwnership,
            identifiers = shelfBook.identifiers,
        )

        return UndoAddShelfCopyResult.Success(
            title = shelfBook.book.title,
            quantity = updatedOwnership.quantity,
        )
    }
}

sealed interface RemoveShelfCopyResult {
    data class Success(
        val title: String,
        val quantity: Int,
    ) : RemoveShelfCopyResult

    data class ArchiveConfirmationRequired(
        val title: String,
    ) : RemoveShelfCopyResult

    data object ShelfBookNotFound : RemoveShelfCopyResult
}

class RemoveShelfCopyUseCase(
    private val shelfRepository: ShelfRepository,
    private val clock: () -> Instant = Instant::now,
) {
    suspend operator fun invoke(
        userId: UUID,
        bookId: UUID,
    ): RemoveShelfCopyResult {
        val shelfBook = shelfRepository.getBookDetailById(
            userId = userId,
            bookId = bookId,
        ) ?: return RemoveShelfCopyResult.ShelfBookNotFound

        if (shelfBook.ownership.quantity == 1) {
            return RemoveShelfCopyResult.ArchiveConfirmationRequired(
                title = shelfBook.book.title,
            )
        }

        val now = clock()
        val updatedOwnership = shelfBook.ownership.copy(
            quantity = shelfBook.ownership.quantity - 1,
            updatedAt = now,
        )

        shelfRepository.updateBook(
            book = shelfBook.book.copy(updatedAt = now),
            ownership = updatedOwnership,
            identifiers = shelfBook.identifiers,
        )

        return RemoveShelfCopyResult.Success(
            title = shelfBook.book.title,
            quantity = updatedOwnership.quantity,
        )
    }
}

sealed interface ArchiveShelfBookResult {
    data class Success(
        val title: String,
    ) : ArchiveShelfBookResult

    data object ShelfBookNotFound : ArchiveShelfBookResult
}

class ArchiveShelfBookUseCase(
    private val shelfRepository: ShelfRepository,
    private val clock: () -> Instant = Instant::now,
) {
    suspend operator fun invoke(
        userId: UUID,
        bookId: UUID,
    ): ArchiveShelfBookResult {
        val shelfBook = shelfRepository.getBookDetailById(
            userId = userId,
            bookId = bookId,
        ) ?: return ArchiveShelfBookResult.ShelfBookNotFound

        val now = clock()
        shelfRepository.updateBook(
            book = shelfBook.book.copy(updatedAt = now),
            ownership = shelfBook.ownership.copy(
                status = OwnershipStatus.ARCHIVED,
                archivedAt = now,
                updatedAt = now,
            ),
            identifiers = shelfBook.identifiers,
        )

        return ArchiveShelfBookResult.Success(title = shelfBook.book.title)
    }
}
