package com.sergebailes.bookbee.ui.shelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sergebailes.bookbee.BookBeeAppContainer
import com.sergebailes.bookbee.domain.model.IdentifierType
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.repository.ShelfRepository
import com.sergebailes.bookbee.domain.repository.UserProfileRepository
import com.sergebailes.bookbee.domain.usecase.CreateManualShelfBookCommand
import com.sergebailes.bookbee.domain.usecase.CreateManualShelfBookResult
import com.sergebailes.bookbee.domain.usecase.CreateManualShelfBookUseCase
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManualShelfBookFormState(
    val title: String = "",
    val author: String = "",
    val notes: String = "",
    val isbn: String = "",
    val readStatus: ReadStatus = ReadStatus.UNREAD,
    val titleError: String? = null,
    val isbnError: String? = null,
)

data class ShelfBookListItem(
    val id: UUID,
    val title: String,
    val authorLine: String?,
    val isbn: String?,
    val readStatus: ReadStatus,
    val quantity: Int,
    val notes: String?,
)

data class ShelfUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isShowingAddForm: Boolean = false,
    val books: List<ShelfBookListItem> = emptyList(),
    val form: ManualShelfBookFormState = ManualShelfBookFormState(),
    val message: String? = null,
)

class ShelfViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val shelfRepository: ShelfRepository,
    private val createManualShelfBookUseCase: CreateManualShelfBookUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ShelfUiState())
    val uiState: StateFlow<ShelfUiState> = mutableUiState.asStateFlow()

    private var activeUserId: UUID? = null

    init {
        loadShelf()
    }

    fun onAddBookClicked() {
        mutableUiState.update {
            it.copy(
                isShowingAddForm = true,
                form = ManualShelfBookFormState(),
                message = null,
            )
        }
    }

    fun onCancelAddBook() {
        mutableUiState.update {
            it.copy(
                isShowingAddForm = false,
                isSaving = false,
                form = ManualShelfBookFormState(),
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

    fun onNotesChanged(value: String) {
        updateForm { copy(notes = value) }
    }

    fun onIsbnChanged(value: String) {
        updateForm { copy(isbn = value, isbnError = null) }
    }

    fun onReadStatusChanged(value: ReadStatus) {
        updateForm { copy(readStatus = value) }
    }

    fun onSaveBookClicked() {
        val userId = activeUserId ?: run {
            mutableUiState.update { it.copy(message = "Shelf is still loading.") }
            return
        }
        val currentForm = mutableUiState.value.form

        viewModelScope.launch {
            mutableUiState.update { it.copy(isSaving = true, message = null) }

            runCatching {
                createManualShelfBookUseCase(
                    CreateManualShelfBookCommand(
                        userId = userId,
                        title = currentForm.title,
                        author = currentForm.author,
                        notes = currentForm.notes,
                        isbn = currentForm.isbn,
                        readStatus = currentForm.readStatus,
                    )
                )
            }.onSuccess { result ->
                when (result) {
                    is CreateManualShelfBookResult.Success -> {
                        mutableUiState.update {
                            it.copy(
                                isSaving = false,
                                isShowingAddForm = false,
                                form = ManualShelfBookFormState(),
                                message = result.message,
                            )
                        }
                    }

                    is CreateManualShelfBookResult.ValidationFailed -> {
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
                }
            }.onFailure {
                mutableUiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Book could not be saved.",
                    )
                }
            }
        }
    }

    private fun loadShelf() {
        viewModelScope.launch {
            runCatching {
                val user = userProfileRepository.getOrCreateDefaultUser()
                activeUserId = user.id
                shelfRepository.observeOwnedBooks(user.id).collect { books ->
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            books = books.map { book -> book.toListItem() },
                            message = null,
                        )
                    }
                }
            }.onFailure {
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Shelf could not be loaded.",
                    )
                }
            }
        }
    }

    private fun updateForm(transform: ManualShelfBookFormState.() -> ManualShelfBookFormState) {
        mutableUiState.update { state ->
            state.copy(
                form = state.form.transform(),
            )
        }
    }

    private fun ShelfBook.toListItem(): ShelfBookListItem {
        return ShelfBookListItem(
            id = book.id,
            title = book.title,
            authorLine = book.authors.takeIf(List<String>::isNotEmpty)?.joinToString(", "),
            isbn = identifiers
                .firstOrNull { it.type == IdentifierType.ISBN_10 || it.type == IdentifierType.ISBN_13 }
                ?.value,
            readStatus = ownership.readStatus,
            quantity = ownership.quantity,
            notes = ownership.notes,
        )
    }

    class Factory(
        private val appContainer: BookBeeAppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            check(modelClass == ShelfViewModel::class.java) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }

            return ShelfViewModel(
                userProfileRepository = appContainer.userProfileRepository,
                shelfRepository = appContainer.shelfRepository,
                createManualShelfBookUseCase = appContainer.createManualShelfBookUseCase,
            ) as T
        }
    }
}
