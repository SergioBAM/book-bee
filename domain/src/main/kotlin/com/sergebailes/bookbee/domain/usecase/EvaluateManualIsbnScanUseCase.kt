package com.sergebailes.bookbee.domain.usecase

import com.sergebailes.bookbee.domain.isbn.ValidatedIsbn
import com.sergebailes.bookbee.domain.isbn.parseIsbn
import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import java.util.UUID

sealed interface ManualIsbnScanResult {
    data class Success(
        val isbn: ValidatedIsbn,
        val ownership: ScanOwnershipResult,
        val wishlistBook: WishlistBook?,
    ) : ManualIsbnScanResult

    data class ValidationFailed(
        val isbnError: String,
    ) : ManualIsbnScanResult
}

sealed interface ScanOwnershipResult {
    data class Owned(
        val shelfBook: ShelfBook,
    ) : ScanOwnershipResult

    data class NotOwned(
        val previouslyOwnedBook: ShelfBook?,
    ) : ScanOwnershipResult
}

class EvaluateManualIsbnScanUseCase(
    private val shelfRepository: ShelfRepository,
    private val wishlistRepository: WishlistRepository,
) {
    suspend operator fun invoke(
        userId: UUID,
        rawIsbn: String,
    ): ManualIsbnScanResult {
        val isbn = parseIsbn(rawIsbn)
            ?: return ManualIsbnScanResult.ValidationFailed("Enter a valid ISBN-10 or ISBN-13")

        val activeOwnedBook = shelfRepository.findOwnedBookByExactIsbn(
            userId = userId,
            isbn = isbn,
        )
        val archivedBook = if (activeOwnedBook == null) {
            shelfRepository.findArchivedBookByExactIsbn(
                userId = userId,
                isbn = isbn,
            )
        } else {
            null
        }
        val wishlistBook = wishlistRepository.findWishlistBookByExactIsbn(
            userId = userId,
            isbn = isbn,
        )

        return ManualIsbnScanResult.Success(
            isbn = isbn,
            ownership = activeOwnedBook?.let(ScanOwnershipResult::Owned)
                ?: ScanOwnershipResult.NotOwned(previouslyOwnedBook = archivedBook),
            wishlistBook = wishlistBook,
        )
    }
}
