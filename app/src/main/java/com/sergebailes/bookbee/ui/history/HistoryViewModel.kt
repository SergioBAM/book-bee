package com.sergebailes.bookbee.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sergebailes.bookbee.BookBeeAppContainer
import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.UserProfileRepository
import com.sergebailes.bookbee.domain.usecase.HardDeleteArchivedShelfBookResult
import com.sergebailes.bookbee.domain.usecase.HardDeleteArchivedShelfBookUseCase
import com.sergebailes.bookbee.domain.usecase.RestoreArchivedShelfBookResult
import com.sergebailes.bookbee.domain.usecase.RestoreArchivedShelfBookUseCase
import com.sergebailes.bookbee.domain.usecase.SearchHistoryUseCase
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryListItem(
    val ownershipId: UUID,
    val bookId: UUID,
    val title: String,
    val authorLine: String?,
    val isbn: String?,
    val notes: String?,
    val readStatus: ReadStatus,
)

data class HistoryEditFormState(
    val ownershipId: UUID,
    val bookId: UUID,
    val title: String,
    val notes: String,
    val readStatus: ReadStatus,
)

data class HistoryRestoreConflict(
    val title: String,
    val authorLine: String?,
)

data class HistoryHardDeleteConfirmation(
    val ownershipId: UUID,
    val title: String,
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val items: List<HistoryListItem> = emptyList(),
    val editForm: HistoryEditFormState? = null,
    val restoreConflict: HistoryRestoreConflict? = null,
    val hardDeleteConfirmation: HistoryHardDeleteConfirmation? = null,
    val message: String? = null,
)

class HistoryViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val shelfRepository: ShelfRepository,
    private val searchHistoryUseCase: SearchHistoryUseCase,
    private val restoreArchivedShelfBookUseCase: RestoreArchivedShelfBookUseCase,
    private val hardDeleteArchivedShelfBookUseCase: HardDeleteArchivedShelfBookUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = mutableUiState.asStateFlow()

    private var activeUserId: UUID? = null
    private var historyJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching {
                userProfileRepository.getOrCreateDefaultUser()
            }.onSuccess { user ->
                activeUserId = user.id
                restartHistorySearch()
            }.onFailure {
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        message = "History could not be loaded.",
                    )
                }
            }
        }
    }

    fun onQueryChanged(value: String) {
        mutableUiState.update {
            it.copy(
                query = value,
                message = null,
                restoreConflict = null,
            )
        }
        restartHistorySearch()
    }

    fun onEditClicked(ownershipId: UUID) {
        val item = mutableUiState.value.items.firstOrNull { it.ownershipId == ownershipId } ?: return
        mutableUiState.update {
            it.copy(
                editForm = HistoryEditFormState(
                    ownershipId = item.ownershipId,
                    bookId = item.bookId,
                    title = item.title,
                    notes = item.notes.orEmpty(),
                    readStatus = item.readStatus,
                ),
                message = null,
                restoreConflict = null,
                hardDeleteConfirmation = null,
            )
        }
    }

    fun onEditNotesChanged(value: String) {
        mutableUiState.update { state ->
            state.copy(editForm = state.editForm?.copy(notes = value))
        }
    }

    fun onEditReadStatusChanged(value: ReadStatus) {
        mutableUiState.update { state ->
            state.copy(editForm = state.editForm?.copy(readStatus = value))
        }
    }

    fun onCancelEditClicked() {
        mutableUiState.update {
            it.copy(
                editForm = null,
                message = null,
            )
        }
    }

    fun onSaveEditClicked() {
        val userId = activeUserId ?: return
        val form = mutableUiState.value.editForm ?: return

        viewModelScope.launch {
            runCatching {
                val archivedBook = shelfRepository.getArchivedBookDetailById(
                    userId = userId,
                    bookId = form.bookId,
                ) ?: return@runCatching false
                shelfRepository.updateBook(
                    book = archivedBook.book,
                    ownership = archivedBook.ownership.copy(
                        notes = form.notes.trim().takeIf(String::isNotBlank),
                        readStatus = form.readStatus,
                    ),
                    identifiers = archivedBook.identifiers,
                )
                true
            }.onSuccess { saved ->
                mutableUiState.update {
                    it.copy(
                        editForm = null,
                        message = if (saved) "History record updated." else "History record could not be found.",
                    )
                }
            }.onFailure {
                mutableUiState.update { it.copy(message = "History record could not be updated.") }
            }
        }
    }

    fun onRestoreClicked(ownershipId: UUID) {
        viewModelScope.launch {
            runCatching {
                restoreArchivedShelfBookUseCase(ownershipId)
            }.onSuccess { result ->
                when (result) {
                    is RestoreArchivedShelfBookResult.Success -> {
                        mutableUiState.update {
                            it.copy(
                                restoreConflict = null,
                                message = "\"${result.title}\" restored to Shelf.",
                            )
                        }
                    }

                    is RestoreArchivedShelfBookResult.ActiveExactIsbnConflict -> {
                        mutableUiState.update {
                            it.copy(
                                restoreConflict = HistoryRestoreConflict(
                                    title = result.title,
                                    authorLine = result.authorLine,
                                ),
                                message = null,
                            )
                        }
                    }

                    RestoreArchivedShelfBookResult.ArchivedBookNotFound -> {
                        mutableUiState.update { it.copy(message = "History record could not be found.") }
                    }
                }
            }.onFailure {
                mutableUiState.update { it.copy(message = "History record could not be restored.") }
            }
        }
    }

    fun onHardDeleteClicked(ownershipId: UUID) {
        val item = mutableUiState.value.items.firstOrNull { it.ownershipId == ownershipId } ?: return
        mutableUiState.update {
            it.copy(
                hardDeleteConfirmation = HistoryHardDeleteConfirmation(
                    ownershipId = ownershipId,
                    title = item.title,
                ),
                restoreConflict = null,
                message = null,
            )
        }
    }

    fun onCancelHardDeleteClicked() {
        mutableUiState.update {
            it.copy(
                hardDeleteConfirmation = null,
                message = null,
            )
        }
    }

    fun onConfirmHardDeleteClicked() {
        val confirmation = mutableUiState.value.hardDeleteConfirmation ?: return
        viewModelScope.launch {
            runCatching {
                hardDeleteArchivedShelfBookUseCase(confirmation.ownershipId)
            }.onSuccess { result ->
                when (result) {
                    is HardDeleteArchivedShelfBookResult.Success -> {
                        val wishlistText = if (result.deletedWishlistItemCount > 0) {
                            " Linked Wishlist item removed."
                        } else {
                            ""
                        }
                        mutableUiState.update {
                            it.copy(
                                hardDeleteConfirmation = null,
                                message = "History record permanently deleted.$wishlistText",
                            )
                        }
                    }

                    HardDeleteArchivedShelfBookResult.ArchivedBookNotFound -> {
                        mutableUiState.update {
                            it.copy(
                                hardDeleteConfirmation = null,
                                message = "History record could not be found.",
                            )
                        }
                    }
                }
            }.onFailure {
                mutableUiState.update { it.copy(message = "History record could not be deleted.") }
            }
        }
    }

    private fun restartHistorySearch() {
        val userId = activeUserId ?: return
        val query = mutableUiState.value.query
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            searchHistoryUseCase(userId, query).collect { results ->
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        items = results.mapNotNull { result ->
                            val archivedBook = result.archivedBook ?: return@mapNotNull null
                            HistoryListItem(
                                ownershipId = archivedBook.ownership.id,
                                bookId = archivedBook.book.id,
                                title = archivedBook.book.title,
                                authorLine = archivedBook.book.authors
                                    .takeIf(List<String>::isNotEmpty)
                                    ?.joinToString(", "),
                                isbn = archivedBook.identifiers
                                    .firstOrNull { identifier ->
                                        identifier.type == IdentifierType.ISBN_13 ||
                                            identifier.type == IdentifierType.ISBN_10
                                    }
                                    ?.value,
                                notes = archivedBook.ownership.notes,
                                readStatus = archivedBook.ownership.readStatus,
                            )
                        },
                    )
                }
            }
        }
    }

    class Factory(
        private val appContainer: BookBeeAppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            check(modelClass == HistoryViewModel::class.java) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }

            return HistoryViewModel(
                userProfileRepository = appContainer.userProfileRepository,
                shelfRepository = appContainer.shelfRepository,
                searchHistoryUseCase = appContainer.searchHistoryUseCase,
                restoreArchivedShelfBookUseCase = appContainer.restoreArchivedShelfBookUseCase,
                hardDeleteArchivedShelfBookUseCase = appContainer.hardDeleteArchivedShelfBookUseCase,
            ) as T
        }
    }
}
