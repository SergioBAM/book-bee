package com.sergebailes.bookbee.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sergebailes.bookbee.BookBeeAppContainer
import com.sergebailes.bookbee.domain.usecase.LibrarySearchBadge
import com.sergebailes.bookbee.domain.usecase.LibrarySearchTarget
import com.sergebailes.bookbee.domain.usecase.SearchActiveLibraryUseCase
import com.sergebailes.bookbee.domain.repository.UserProfileRepository
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibrarySearchListItem(
    val bookId: UUID,
    val title: String,
    val authorLine: String?,
    val isbn: String?,
    val badges: Set<LibrarySearchBadge>,
    val target: LibrarySearchTarget,
)

data class LibrarySearchUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    val results: List<LibrarySearchListItem> = emptyList(),
    val message: String? = null,
)

class LibrarySearchViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val searchActiveLibraryUseCase: SearchActiveLibraryUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(LibrarySearchUiState())
    val uiState: StateFlow<LibrarySearchUiState> = mutableUiState.asStateFlow()

    private var activeUserId: UUID? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching {
                userProfileRepository.getOrCreateDefaultUser()
            }.onSuccess { user ->
                activeUserId = user.id
                mutableUiState.update { it.copy(isLoading = false) }
                restartSearch()
            }.onFailure {
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Search could not be loaded.",
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
            )
        }
        restartSearch()
    }

    private fun restartSearch() {
        val userId = activeUserId ?: return
        val query = mutableUiState.value.query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchActiveLibraryUseCase(userId, query).collect { results ->
                mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        results = if (query.isBlank()) {
                            emptyList()
                        } else {
                            results.map { result ->
                                LibrarySearchListItem(
                                    bookId = result.bookId,
                                    title = result.title,
                                    authorLine = result.authorLine,
                                    isbn = result.isbn,
                                    badges = result.badges,
                                    target = result.target,
                                )
                            }
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
            check(modelClass == LibrarySearchViewModel::class.java) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }

            return LibrarySearchViewModel(
                userProfileRepository = appContainer.userProfileRepository,
                searchActiveLibraryUseCase = appContainer.searchActiveLibraryUseCase,
            ) as T
        }
    }
}
