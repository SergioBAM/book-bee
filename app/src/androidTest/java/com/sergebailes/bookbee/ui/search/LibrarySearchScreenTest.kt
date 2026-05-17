package com.sergebailes.bookbee.ui.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sergebailes.bookbee.domain.usecase.LibrarySearchBadge
import com.sergebailes.bookbee.domain.usecase.LibrarySearchTarget
import com.sergebailes.bookbee.ui.theme.BookBeeTheme
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LibrarySearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun blankQueryShowsQuietEmptyState() {
        setSearchScreen(
            state = LibrarySearchUiState(
                query = "",
                isLoading = false,
            )
        )

        composeRule.onNodeWithText("Search Shelf and Wishlist").assertIsDisplayed()
        composeRule.onNodeWithText("Search active Shelf and Wishlist records.").assertIsDisplayed()
    }

    @Test
    fun matchingQueryShowsActiveResultsAndSelectionTarget() {
        var selectedTarget: LibrarySearchTarget? = null
        setSearchScreen(
            state = LibrarySearchUiState(
                query = "dune",
                isLoading = false,
                results = listOf(
                    searchItem(
                        title = "Dune",
                        target = LibrarySearchTarget.SHELF,
                        badges = setOf(LibrarySearchBadge.ON_SHELF),
                    ),
                    searchItem(
                        title = "Dune Messiah",
                        target = LibrarySearchTarget.WISHLIST,
                        badges = setOf(LibrarySearchBadge.WISHLIST),
                    ),
                ),
            ),
            onResultSelected = { selectedTarget = it },
        )

        composeRule.onNodeWithText("Dune").assertIsDisplayed()
        composeRule.onNodeWithText("On Shelf").assertIsDisplayed()
        composeRule.onNodeWithText("Dune Messiah").assertIsDisplayed()
        composeRule.onNodeWithText("Wishlist").assertIsDisplayed()

        composeRule.onNodeWithText("Dune").performClick()

        assertEquals(LibrarySearchTarget.SHELF, selectedTarget)
    }

    private fun setSearchScreen(
        state: LibrarySearchUiState,
        onResultSelected: (LibrarySearchTarget) -> Unit = {},
    ) {
        composeRule.setContent {
            BookBeeTheme {
                LibrarySearchScreen(
                    state = state,
                    onQueryChanged = {},
                    onResultSelected = onResultSelected,
                )
            }
        }
    }

    private fun searchItem(
        title: String,
        target: LibrarySearchTarget,
        badges: Set<LibrarySearchBadge>,
    ): LibrarySearchListItem = LibrarySearchListItem(
        bookId = UUID.randomUUID(),
        title = title,
        authorLine = "Frank Herbert",
        isbn = "9780441172719",
        badges = badges,
        target = target,
    )
}
