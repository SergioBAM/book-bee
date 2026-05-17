package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.repository.HardDeleteArchivedOwnershipResult
import com.sergebailes.bookbee.domain.repository.RestoreArchivedOwnershipResult
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import java.time.Instant
import java.util.UUID

sealed interface RestoreArchivedShelfBookResult {
    data class Success(
        val title: String,
    ) : RestoreArchivedShelfBookResult

    data class ActiveExactIsbnConflict(
        val title: String,
        val authorLine: String?,
    ) : RestoreArchivedShelfBookResult

    data object ArchivedBookNotFound : RestoreArchivedShelfBookResult
}

class RestoreArchivedShelfBookUseCase(
    private val shelfRepository: ShelfRepository,
    private val clock: () -> Instant = Instant::now,
) {
    suspend operator fun invoke(ownershipId: UUID): RestoreArchivedShelfBookResult {
        return when (
            val result = shelfRepository.restoreArchivedOwnership(
                ownershipId = ownershipId,
                restoredAt = clock(),
            )
        ) {
            is RestoreArchivedOwnershipResult.Success ->
                RestoreArchivedShelfBookResult.Success(result.restoredBook.book.title)

            is RestoreArchivedOwnershipResult.ActiveExactIsbnConflict ->
                RestoreArchivedShelfBookResult.ActiveExactIsbnConflict(
                    title = result.activeBook.book.title,
                    authorLine = result.activeBook.authorLine,
                )

            RestoreArchivedOwnershipResult.ArchivedOwnershipNotFound ->
                RestoreArchivedShelfBookResult.ArchivedBookNotFound
        }
    }
}

sealed interface HardDeleteArchivedShelfBookResult {
    data class Success(
        val deletedWishlistItemCount: Int,
        val deletedBookAggregate: Boolean,
    ) : HardDeleteArchivedShelfBookResult

    data object ArchivedBookNotFound : HardDeleteArchivedShelfBookResult
}

class HardDeleteArchivedShelfBookUseCase(
    private val shelfRepository: ShelfRepository,
) {
    suspend operator fun invoke(ownershipId: UUID): HardDeleteArchivedShelfBookResult {
        return when (val result = shelfRepository.hardDeleteArchivedOwnership(ownershipId)) {
            is HardDeleteArchivedOwnershipResult.Success ->
                HardDeleteArchivedShelfBookResult.Success(
                    deletedWishlistItemCount = result.deletedWishlistItemCount,
                    deletedBookAggregate = result.deletedBookAggregate,
                )

            HardDeleteArchivedOwnershipResult.ArchivedOwnershipNotFound ->
                HardDeleteArchivedShelfBookResult.ArchivedBookNotFound
        }
    }
}

private val ShelfBook.authorLine: String?
    get() = book.authors.takeIf(List<String>::isNotEmpty)?.joinToString(", ")
