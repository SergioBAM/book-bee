package com.sergebailes.bookbee.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sergebailes.bookbee.BookBeeAppContainer
import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.UserProfileRepository
import com.sergebailes.bookbee.domain.repository.WishlistRepository
import com.sergebailes.bookbee.domain.usecase.DeleteWishlistItemUseCase
import com.sergebailes.bookbee.domain.usecase.MoveWishlistItemToShelfCommand
import com.sergebailes.bookbee.domain.usecase.MoveWishlistItemToShelfResult
import com.sergebailes.bookbee.domain.usecase.MoveWishlistItemToShelfUseCase
import com.sergebailes.bookbee.domain.usecase.SaveWishlistItemCommand
import com.sergebailes.bookbee.domain.usecase.SaveWishlistItemResult
import com.sergebailes.bookbee.domain.usecase.SaveWishlistItemUseCase
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WishlistFormState(
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val notes: String = "",
    val titleError: String? = null,
    val isbnError: String? = null,
)

data class WishlistListItem(
    val id: UUID,
    val title: String,
    val authorLine: String?,
    val isbn: String?,
    val notes: String?,
    val isOnShelf: Boolean,
)

data class WishlistShelfHandoffState(
    val wishlistItemId: UUID,
    val title: String,
    val authorLine: String?,
    val isbn: String?,
    val notes: String,
    val readStatus: ReadStatus = ReadStatus.UNREAD,
)

data class OwnedOverlapConfirmationState(
    val title: String,
    val authorLine: String?,
)

data class WishlistUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isShowingForm: Boolean = false,
    val editingWishlistItemId: UUID? = null,
    val items: List<WishlistListItem> = emptyList(),
    val form: WishlistFormState = WishlistFormState(),
    val shelfHandoff: WishlistShelfHandoffState? = null,
    val pendingOwnedOverlapConfirmation: OwnedOverlapConfirmationState? = null,
    val message: String? = null,
)

class WishlistViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val shelfRepository: ShelfRepository,
    private val wishlistRepository: WishlistRepository,
    private val saveWishlistItemUseCase: SaveWishlistItemUseCase,
    private val deleteWishlistItemUseCase: DeleteWishlistItemUseCase,
    private val moveWishlistItemToShelfUseCase: MoveWishlistItemToShelfUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = mutableUiState.asStateFlow()

    private var activeUserId: UUID? = null

    init {
        loadWishlist()
    }

    fun onAddWishlistItemClicked() {
        mutableUiState.update {
            it.copy(
                isShowingForm = true,
                editingWishlistItemId = null,
                shelfHandoff = null,
                pendingOwnedOverlapConfirmation = null,
                form = WishlistFormState(),
                message = null,
            )
        }
    }

    fun onEditWishlistItemClicked(wishlistItemId: UUID) {
        val item = mutableUiState.value.items.firstOrNull { it.id == wishlistItemId } ?: return
        mutableUiState.update {
            it.copy(
                isShowingForm = true,
                editingWishlistItemId = item.id,
                shelfHandoff = null,
                pendingOwnedOverlapConfirmation = null,
                form = WishlistFormState(
                    title = item.title,
                    author = item.authorLine.orEmpty(),
                    isbn = item.isbn.orEmpty(),
                    notes = item.notes.orEmpty(),
                ),
                message = null,
            )
        }
    }

    fun onDeleteWishlistItemClicked(wishlistItemId: UUID) {
        viewModelScope.launch {
            runCatching {
                deleteWishlistItemUseCase(wishlistItemId)
            }.onSuccess { message ->
                mutableUiState.update {
                    it.copy(message = message ?: "Wishlist item could not be removed.")
                }
            }.onFailure {
                mutableUiState.update { it.copy(message = "Wishlist item could not be removed.") }
            }
        }
    }

    fun onMoveToShelfClicked(wishlistItemId: UUID) {
        val item = mutableUiState.value.items.firstOrNull { it.id == wishlistItemId } ?: return
        if (item.isOnShelf) {
            mutableUiState.update {
                it.copy(message = "\"${item.title}\" is already on Shelf.")
            }
            return
        }

        mutableUiState.update {
            it.copy(
                isShowingForm = false,
                editingWishlistItemId = null,
                pendingOwnedOverlapConfirmation = null,
                shelfHandoff = WishlistShelfHandoffState(
                    wishlistItemId = item.id,
                    title = item.title,
                    authorLine = item.authorLine,
                    isbn = item.isbn,
                    notes = item.notes.orEmpty(),
                ),
                message = null,
            )
        }
    }

    fun onCancelForm() {
        mutableUiState.update {
            it.copy(
                isShowingForm = false,
                isSaving = false,
                editingWishlistItemId = null,
                pendingOwnedOverlapConfirmation = null,
                form = WishlistFormState(),
                message = null,
            )
        }
    }

    fun onCancelShelfHandoff() {
        mutableUiState.update {
            it.copy(
                shelfHandoff = null,
                isSaving = false,
                message = null,
            )
        }
    }

    fun onTitleChanged(value: String) {
        updateForm { copy(title = value, titleError = null) }
    }

    fun onAuthorChanged(value: String) {
        updateForm { copy(author = value) }
    }

    fun onIsbnChanged(value: String) {
        updateForm { copy(isbn = value, isbnError = null) }
    }

    fun onNotesChanged(value: String) {
        updateForm { copy(notes = value) }
    }

    fun onShelfNotesChanged(value: String) {
        mutableUiState.update { state ->
            state.copy(
                shelfHandoff = state.shelfHandoff?.copy(notes = value),
            )
        }
    }

    fun onShelfReadStatusChanged(value: ReadStatus) {
        mutableUiState.update { state ->
            state.copy(
                shelfHandoff = state.shelfHandoff?.copy(readStatus = value),
            )
        }
    }

    fun onSaveWishlistItemClicked() {
        saveWishlistItem(allowOwnedOverlap = false)
    }

    fun onConfirmOwnedOverlapClicked() {
        saveWishlistItem(allowOwnedOverlap = true)
    }

    fun onDismissOwnedOverlapConfirmation() {
        mutableUiState.update {
            it.copy(
                isSaving = false,
                pendingOwnedOverlapConfirmation = null,
            )
        }
    }

    fun onConfirmMoveToShelfClicked() {
        val userId = activeUserId ?: run {
            mutableUiState.update { it.copy(message = "Wishlist is still loading.") }
            return
        }
        val shelfHandoff = mutableUiState.value.shelfHandoff ?: return

        viewModelScope.launch {
            mutableUiState.update { it.copy(isSaving = true, message = null) }

            runCatching {
                moveWishlistItemToShelfUseCase(
                    MoveWishlistItemToShelfCommand(
                        userId = userId,
                        wishlistItemId = shelfHandoff.wishlistItemId,
                        notes = shelfHandoff.notes,
                        readStatus = shelfHandoff.readStatus,
                    )
                )
            }.onSuccess { result ->
                when (result) {
                    is MoveWishlistItemToShelfResult.Success -> {
                        mutableUiState.update {
                            it.copy(
                                isSaving = false,
                                shelfHandoff = null,
                                message = result.message,
                            )
                        }
                    }

                    MoveWishlistItemToShelfResult.WishlistItemNotFound -> {
                        mutableUiState.update {
                            it.copy(
                                isSaving = false,
                                shelfHandoff = null,
                                message = "Wishlist item could not be found.",
                            )
                        }
                    }
                }
            }.onFailure {
                mutableUiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Book could not be moved to Shelf.",
                    )
                }
            }
        }
    }

    private fun saveWishlistItem(allowOwnedOverlap: Boolean) {
        val userId = activeUserId ?: run {
            mutableUiState.update { it.copy(message = "Wishlist is still loading.") }
            return
        }
        val currentState = mutableUiState.value
        val currentForm = currentState.form

        viewModelScope.launch {
            mutableUiState.update { it.copy(isSaving = true, message = null) }

            runCatching {
                saveWishlistItemUseCase(
                    SaveWishlistItemCommand(
                        userId = userId,
                        wishlistItemId = currentState.editingWishlistItemId,
                        title = currentForm.title,
                        author = currentForm.author,
                        isbn = currentForm.isbn,
                        notes = currentForm.notes,
                        allowOwnedOverlap = allowOwnedOverlap,
                    )
                )
            }.onSuccess { result ->
                when (result) {
                    is SaveWishlistItemResult.Success -> {
                        val savedTitle = currentForm.title.trim()
                        val successMessage = if (currentState.editingWishlistItemId == null) {
                            "\"$savedTitle\" added to Wishlist."
                        } else {
                            "\"$savedTitle\" updated in Wishlist."
                        }
                        mutableUiState.update {
                            it.copy(
                                isSaving = false,
                                isShowingForm = false,
                                editingWishlistItemId = null,
                                pendingOwnedOverlapConfirmation = null,
                                form = WishlistFormState(),
                                message = successMessage,
                            )
                        }
                    }

                    is SaveWishlistItemResult.ValidationFailed -> {
                        mutableUiState.update {
                            it.copy(
                                isSaving = false,
                                form = it.form.copy(
                                    titleError = result.titleError,
                                    isbnError = result.isbnError,
                                ),
                            )
                        }
                    }

                    is SaveWishlistItemResult.RequiresOwnedOverlapConfirmation -> {
                        mutableUiState.update {
                            it.copy(
                                isSaving = false,
                                pendingOwnedOverlapConfirmation = OwnedOverlapConfirmationState(
                                    title = result.title,
                                    authorLine = result.authorLine,
                                ),
                            )
                        }
                    }

                    SaveWishlistItemResult.WishlistItemNotFound -> {
                        mutableUiState.update {
                            it.copy(
                                isSaving = false,
                                isShowingForm = false,
                                editingWishlistItemId = null,
                                message = "Wishlist item could not be found.",
                            )
                        }
                    }
                }
            }.onFailure {
                mutableUiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Wishlist item could not be saved.",
                    )
                }
            }
        }
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            runCatching {
                val user = userProfileRepository.getOrCreateDefaultUser()
                activeUserId = user.id
                combine(
                    wishlistRepository.observeWishlistBooks(user.id),
                    shelfRepository.observeOwnedBooks(user.id),
                ) { wishlistBooks, ownedBooks ->
                    val ownedBookIds = ownedBooks.map { it.book.id }.toSet()
                    wishlistBooks.map { it.toListItem(ownedBookIds.contains(it.book.id)) }
                }.collect { items ->
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                        )
                    }
                }
            }.onFailure {
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Wishlist could not be loaded.",
                    )
                }
            }
        }
    }

    private fun updateForm(transform: WishlistFormState.() -> WishlistFormState) {
        mutableUiState.update { state ->
            state.copy(form = state.form.transform())
        }
    }

    private fun WishlistBook.toListItem(isOnShelf: Boolean): WishlistListItem {
        return WishlistListItem(
            id = item.id,
            title = book.title,
            authorLine = book.authors.takeIf(List<String>::isNotEmpty)?.joinToString(", "),
            isbn = identifiers
                .firstOrNull { it.type == IdentifierType.ISBN_10 || it.type == IdentifierType.ISBN_13 }
                ?.value,
            notes = item.notes,
            isOnShelf = isOnShelf,
        )
    }

    class Factory(
        private val appContainer: BookBeeAppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            check(modelClass == WishlistViewModel::class.java) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }

            return WishlistViewModel(
                userProfileRepository = appContainer.userProfileRepository,
                shelfRepository = appContainer.shelfRepository,
                wishlistRepository = appContainer.wishlistRepository,
                saveWishlistItemUseCase = appContainer.saveWishlistItemUseCase,
                deleteWishlistItemUseCase = appContainer.deleteWishlistItemUseCase,
                moveWishlistItemToShelfUseCase = appContainer.moveWishlistItemToShelfUseCase,
            ) as T
        }
    }
}
