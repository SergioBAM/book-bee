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
import com.sergebailes.bookbee.domain.usecase.AddShelfCopyResult
import com.sergebailes.bookbee.domain.usecase.AddShelfCopyUseCase
import com.sergebailes.bookbee.domain.usecase.ArchiveShelfBookResult
import com.sergebailes.bookbee.domain.usecase.ArchiveShelfBookUseCase
import com.sergebailes.bookbee.domain.usecase.CreateManualShelfBookCommand
import com.sergebailes.bookbee.domain.usecase.CreateManualShelfBookResult
import com.sergebailes.bookbee.domain.usecase.CreateManualShelfBookUseCase
import com.sergebailes.bookbee.domain.usecase.RemoveShelfCopyResult
import com.sergebailes.bookbee.domain.usecase.RemoveShelfCopyUseCase
import com.sergebailes.bookbee.domain.usecase.UndoAddShelfCopyResult
import com.sergebailes.bookbee.domain.usecase.UndoAddShelfCopyUseCase
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

data class ShelfDuplicateConflict(
    val bookId: UUID,
    val title: String,
    val authorLine: String?,
)

data class ShelfCopyFeedback(
    val id: Long,
    val bookId: UUID,
    val message: String,
    val actionLabel: String,
)

data class ShelfArchiveConfirmation(
    val bookId: UUID,
    val title: String,
)

data class ShelfUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isShowingAddForm: Boolean = false,
    val books: List<ShelfBookListItem> = emptyList(),
    val form: ManualShelfBookFormState = ManualShelfBookFormState(),
    val duplicateConflict: ShelfDuplicateConflict? = null,
    val copyFeedback: ShelfCopyFeedback? = null,
    val archiveConfirmation: ShelfArchiveConfirmation? = null,
    val message: String? = null,
)

class ShelfViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val shelfRepository: ShelfRepository,
    private val createManualShelfBookUseCase: CreateManualShelfBookUseCase,
    private val addShelfCopyUseCase: AddShelfCopyUseCase,
    private val undoAddShelfCopyUseCase: UndoAddShelfCopyUseCase,
    private val removeShelfCopyUseCase: RemoveShelfCopyUseCase,
    private val archiveShelfBookUseCase: ArchiveShelfBookUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ShelfUiState())
    val uiState: StateFlow<ShelfUiState> = mutableUiState.asStateFlow()

    private var activeUserId: UUID? = null
    private var nextCopyFeedbackId = 0L

    init {
        loadShelf()
    }

    fun onAddBookClicked() {
        mutableUiState.update {
            it.copy(
                isShowingAddForm = true,
                form = ManualShelfBookFormState(),
                duplicateConflict = null,
                archiveConfirmation = null,
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
                duplicateConflict = null,
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
                                duplicateConflict = null,
                                message = result.message,
                            )
                        }
                    }

                    is CreateManualShelfBookResult.ValidationFailed -> {
                        mutableUiState.update {
                            it.copy(
                                isSaving = false,
                                duplicateConflict = null,
                                form = it.form.copy(
                                    titleError = result.titleError,
                                    isbnError = result.isbnError,
                                ),
                            )
                        }
                    }

                    is CreateManualShelfBookResult.DuplicateActiveOwned -> {
                        mutableUiState.update {
                            it.copy(
                                isSaving = false,
                                duplicateConflict = ShelfDuplicateConflict(
                                    bookId = result.bookId,
                                    title = result.title,
                                    authorLine = result.authorLine,
                                ),
                                message = null,
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

    fun onAddAnotherCopyClicked(bookId: UUID) {
        val userId = activeUserId ?: run {
            mutableUiState.update { it.copy(message = "Shelf is still loading.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                addShelfCopyUseCase(userId = userId, bookId = bookId)
            }.onSuccess { result ->
                when (result) {
                    is AddShelfCopyResult.Success -> {
                        mutableUiState.update {
                            it.copy(
                                isShowingAddForm = false,
                                form = ManualShelfBookFormState(),
                                duplicateConflict = null,
                                archiveConfirmation = null,
                                copyFeedback = ShelfCopyFeedback(
                                    id = nextCopyFeedbackId(),
                                    bookId = bookId,
                                    message = "Added another copy of \"${result.title}\".",
                                    actionLabel = "Undo",
                                ),
                                message = null,
                            )
                        }
                    }

                    AddShelfCopyResult.ShelfBookNotFound -> {
                        mutableUiState.update {
                            it.copy(
                                duplicateConflict = null,
                                message = "Shelf book could not be found.",
                            )
                        }
                    }
                }
            }.onFailure {
                mutableUiState.update { it.copy(message = "Copy count could not be updated.") }
            }
        }
    }

    fun onUndoAddAnotherCopyClicked() {
        val feedback = mutableUiState.value.copyFeedback ?: return
        val userId = activeUserId ?: run {
            mutableUiState.update { it.copy(message = "Shelf is still loading.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                undoAddShelfCopyUseCase(userId = userId, bookId = feedback.bookId)
            }.onSuccess { result ->
                when (result) {
                    is UndoAddShelfCopyResult.Success -> {
                        mutableUiState.update {
                            it.copy(
                                copyFeedback = null,
                                message = "Copy count restored for \"${result.title}\".",
                            )
                        }
                    }

                    UndoAddShelfCopyResult.CannotUndoSingleCopy,
                    UndoAddShelfCopyResult.ShelfBookNotFound -> {
                        mutableUiState.update {
                            it.copy(
                                copyFeedback = null,
                                message = "Copy count could not be restored.",
                            )
                        }
                    }
                }
            }.onFailure {
                mutableUiState.update {
                    it.copy(
                        copyFeedback = null,
                        message = "Copy count could not be restored.",
                    )
                }
            }
        }
    }

    fun onRemoveCopyClicked(bookId: UUID) {
        val userId = activeUserId ?: run {
            mutableUiState.update { it.copy(message = "Shelf is still loading.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                removeShelfCopyUseCase(userId = userId, bookId = bookId)
            }.onSuccess { result ->
                when (result) {
                    is RemoveShelfCopyResult.Success -> {
                        mutableUiState.update {
                            it.copy(
                                archiveConfirmation = null,
                                message = "Removed one copy of \"${result.title}\".",
                            )
                        }
                    }

                    is RemoveShelfCopyResult.ArchiveConfirmationRequired -> {
                        mutableUiState.update {
                            it.copy(
                                archiveConfirmation = ShelfArchiveConfirmation(
                                    bookId = bookId,
                                    title = result.title,
                                ),
                                message = null,
                            )
                        }
                    }

                    RemoveShelfCopyResult.ShelfBookNotFound -> {
                        mutableUiState.update { it.copy(message = "Shelf book could not be found.") }
                    }
                }
            }.onFailure {
                mutableUiState.update { it.copy(message = "Copy count could not be updated.") }
            }
        }
    }

    fun onConfirmArchiveClicked() {
        val confirmation = mutableUiState.value.archiveConfirmation ?: return
        val userId = activeUserId ?: run {
            mutableUiState.update { it.copy(message = "Shelf is still loading.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                archiveShelfBookUseCase(userId = userId, bookId = confirmation.bookId)
            }.onSuccess { result ->
                when (result) {
                    is ArchiveShelfBookResult.Success -> {
                        mutableUiState.update {
                            it.copy(
                                archiveConfirmation = null,
                                copyFeedback = null,
                                message = "\"${result.title}\" archived.",
                            )
                        }
                    }

                    ArchiveShelfBookResult.ShelfBookNotFound -> {
                        mutableUiState.update {
                            it.copy(
                                archiveConfirmation = null,
                                message = "Shelf book could not be found.",
                            )
                        }
                    }
                }
            }.onFailure {
                mutableUiState.update { it.copy(message = "Shelf book could not be archived.") }
            }
        }
    }

    fun onCancelArchiveClicked() {
        mutableUiState.update {
            it.copy(
                archiveConfirmation = null,
                message = null,
            )
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
                            archiveConfirmation = it.archiveConfirmation?.takeIf { confirmation ->
                                books.any { book -> book.book.id == confirmation.bookId }
                            },
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
                duplicateConflict = null,
            )
        }
    }

    private fun nextCopyFeedbackId(): Long {
        nextCopyFeedbackId += 1
        return nextCopyFeedbackId
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
                addShelfCopyUseCase = appContainer.addShelfCopyUseCase,
                undoAddShelfCopyUseCase = appContainer.undoAddShelfCopyUseCase,
                removeShelfCopyUseCase = appContainer.removeShelfCopyUseCase,
                archiveShelfBookUseCase = appContainer.archiveShelfBookUseCase,
            ) as T
        }
    }
}
