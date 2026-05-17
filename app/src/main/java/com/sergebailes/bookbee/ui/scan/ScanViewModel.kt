package com.sergebailes.bookbee.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sergebailes.bookbee.BookBeeAppContainer
import com.sergebailes.bookbee.domain.repository.UserProfileRepository
import com.sergebailes.bookbee.domain.usecase.EvaluateManualIsbnScanUseCase
import com.sergebailes.bookbee.domain.usecase.ManualIsbnScanResult
import com.sergebailes.bookbee.domain.usecase.ScanOwnershipResult
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanCameraState {
    UNAVAILABLE,
    PERMISSION_DENIED,
}

enum class ScanOwnershipUiStatus {
    OWNED,
    NOT_OWNED,
}

data class ScanBookContext(
    val title: String,
    val authorLine: String?,
)

data class ScanResultUiState(
    val isbn: String,
    val status: ScanOwnershipUiStatus,
    val ownedBook: ScanBookContext? = null,
    val previouslyOwnedBook: ScanBookContext? = null,
    val wishlistBook: ScanBookContext? = null,
)

data class ScanUiState(
    val isLoading: Boolean = true,
    val cameraState: ScanCameraState = ScanCameraState.UNAVAILABLE,
    val isbn: String = "",
    val isbnError: String? = null,
    val result: ScanResultUiState? = null,
    val message: String? = null,
)

class ScanViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val evaluateManualIsbnScanUseCase: EvaluateManualIsbnScanUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = mutableUiState.asStateFlow()

    private var activeUserId: UUID? = null

    init {
        viewModelScope.launch {
            runCatching {
                userProfileRepository.getOrCreateDefaultUser()
            }.onSuccess { user ->
                activeUserId = user.id
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        cameraState = ScanCameraState.UNAVAILABLE,
                    )
                }
            }.onFailure {
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Scan could not be loaded.",
                    )
                }
            }
        }
    }

    fun onCameraPermissionDenied() {
        mutableUiState.update {
            it.copy(cameraState = ScanCameraState.PERMISSION_DENIED)
        }
    }

    fun onIsbnChanged(value: String) {
        mutableUiState.update {
            it.copy(
                isbn = value,
                isbnError = null,
                message = null,
            )
        }
    }

    fun onEvaluateManualIsbnClicked() {
        val userId = activeUserId ?: run {
            mutableUiState.update { it.copy(message = "Scan is still loading.") }
            return
        }
        val rawIsbn = mutableUiState.value.isbn

        viewModelScope.launch {
            runCatching {
                evaluateManualIsbnScanUseCase(userId = userId, rawIsbn = rawIsbn)
            }.onSuccess { result ->
                when (result) {
                    is ManualIsbnScanResult.Success -> {
                        mutableUiState.update {
                            it.copy(
                                isbnError = null,
                                result = result.toUiState(),
                                message = null,
                            )
                        }
                    }

                    is ManualIsbnScanResult.ValidationFailed -> {
                        mutableUiState.update {
                            it.copy(
                                isbnError = result.isbnError,
                                result = null,
                            )
                        }
                    }
                }
            }.onFailure {
                mutableUiState.update {
                    it.copy(message = "ISBN could not be checked.")
                }
            }
        }
    }

    fun onCancelResultClicked() {
        mutableUiState.update {
            it.copy(
                result = null,
                isbn = "",
                isbnError = null,
                message = null,
            )
        }
    }

    private fun ManualIsbnScanResult.Success.toUiState(): ScanResultUiState {
        return when (val ownershipResult = ownership) {
            is ScanOwnershipResult.Owned -> ScanResultUiState(
                isbn = isbn.value,
                status = ScanOwnershipUiStatus.OWNED,
                ownedBook = ScanBookContext(
                    title = ownershipResult.shelfBook.book.title,
                    authorLine = ownershipResult.shelfBook.book.authorLine,
                ),
                wishlistBook = wishlistBook?.let {
                    ScanBookContext(
                        title = it.book.title,
                        authorLine = it.book.authorLine,
                    )
                },
            )

            is ScanOwnershipResult.NotOwned -> ScanResultUiState(
                isbn = isbn.value,
                status = ScanOwnershipUiStatus.NOT_OWNED,
                previouslyOwnedBook = ownershipResult.previouslyOwnedBook?.let {
                    ScanBookContext(
                        title = it.book.title,
                        authorLine = it.book.authorLine,
                    )
                },
                wishlistBook = wishlistBook?.let {
                    ScanBookContext(
                        title = it.book.title,
                        authorLine = it.book.authorLine,
                    )
                },
            )
        }
    }

    class Factory(
        private val appContainer: BookBeeAppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            check(modelClass == ScanViewModel::class.java) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }

            return ScanViewModel(
                userProfileRepository = appContainer.userProfileRepository,
                evaluateManualIsbnScanUseCase = appContainer.evaluateManualIsbnScanUseCase,
            ) as T
        }
    }
}

private val com.sergebailes.bookbee.domain.model.Book.authorLine: String?
    get() = authors.takeIf(List<String>::isNotEmpty)?.joinToString(", ")
