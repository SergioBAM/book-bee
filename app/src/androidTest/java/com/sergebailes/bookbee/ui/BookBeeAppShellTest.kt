package com.sergebailes.bookbee.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sergebailes.bookbee.core.BookBeeSections
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.ui.history.HistoryScreen
import com.sergebailes.bookbee.ui.history.HistoryUiState
import com.sergebailes.bookbee.ui.theme.BookBeeTheme
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BookBeeAppShellTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun hamburgerOpensSparseHistoryDrawerWithoutPlaceholders() {
        setShell()

        composeRule.onNodeWithTag("open-navigation-drawer").performClick()

        composeRule.onNodeWithText("Book Bee").assertIsDisplayed()
        composeRule.onNodeWithText("History").assertIsDisplayed()
        composeRule.onAllNodesWithText("Import").assertCountEquals(0)
        composeRule.onAllNodesWithText("Export").assertCountEquals(0)
        composeRule.onNodeWithTag("drawer-history", useUnmergedTree = true).assertIsNotSelected()
    }

    @Test
    fun drawerHistoryActsAsSecondaryDestinationWithBottomTabExitAndBack() {
        var selectedPage by mutableIntStateOf(2)
        var isHistoryActive by mutableStateOf(false)
        setShell(
            selectedPage = { selectedPage },
            isHistoryActive = { isHistoryActive },
            onSectionSelected = { index ->
                selectedPage = index
                isHistoryActive = false
            },
            onHistorySelected = {
                isHistoryActive = true
            },
            onHistoryBack = {
                isHistoryActive = false
            },
        )

        composeRule.onNodeWithTag("open-navigation-drawer").performClick()
        composeRule.onNodeWithTag("drawer-history", useUnmergedTree = true).performClick()

        composeRule.onNodeWithTag("top-bar-title").assertTextEquals("History")
        composeRule.onNodeWithText("Wishlist").assertIsDisplayed()
        composeRule.onNodeWithText("History content").assertIsDisplayed()

        composeRule.onNodeWithTag("open-navigation-drawer").performClick()
        composeRule.onNodeWithTag("drawer-history", useUnmergedTree = true).assertIsSelected()
        composeRule.onNodeWithTag("drawer-history", useUnmergedTree = true).performClick()
        composeRule.onNodeWithText("Shelf").performClick()

        composeRule.onNodeWithTag("top-bar-title").assertTextEquals("Shelf")
        composeRule.onNodeWithText("Shelf content").assertIsDisplayed()

        composeRule.onNodeWithTag("open-navigation-drawer").performClick()
        composeRule.onNodeWithTag("drawer-history", useUnmergedTree = true).performClick()
        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithTag("top-bar-title").assertTextEquals("Shelf")
        composeRule.onNodeWithText("Shelf content").assertIsDisplayed()
    }

    @Test
    fun historyScreenKeepsSearchFieldWithoutRepeatingBodyTitle() {
        composeRule.setContent {
            BookBeeTheme {
                BookBeeAppShell(
                    sections = BookBeeSections.all,
                    selectedPage = 0,
                    isHistoryActive = true,
                    onSectionSelected = {},
                    onHistorySelected = {},
                    onHistoryBack = {},
                ) {
                    HistoryScreen(
                        state = HistoryUiState(isLoading = false),
                        onQueryChanged = {},
                        onEditClicked = { _: UUID -> },
                        onEditNotesChanged = {},
                        onEditReadStatusChanged = { _: ReadStatus -> },
                        onCancelEditClicked = {},
                        onSaveEditClicked = {},
                        onRestoreClicked = { _: UUID -> },
                        onHardDeleteClicked = { _: UUID -> },
                        onCancelHardDeleteClicked = {},
                        onConfirmHardDeleteClicked = {},
                        modifier = Modifier,
                    )
                }
            }
        }

        composeRule.onNodeWithText("Search History").assertIsDisplayed()
        composeRule.onAllNodesWithText("History").assertCountEquals(1)
    }

    private fun setShell(
        selectedPage: () -> Int = { 0 },
        isHistoryActive: () -> Boolean = { false },
        onSectionSelected: (Int) -> Unit = {},
        onHistorySelected: () -> Unit = {},
        onHistoryBack: () -> Unit = {},
    ) {
        composeRule.setContent {
            BookBeeTheme {
                val currentPage = selectedPage()
                val historyActive = isHistoryActive()
                BookBeeAppShell(
                    sections = BookBeeSections.all,
                    selectedPage = currentPage,
                    isHistoryActive = historyActive,
                    onSectionSelected = onSectionSelected,
                    onHistorySelected = onHistorySelected,
                    onHistoryBack = onHistoryBack,
                ) {
                    Text(
                        text = if (historyActive) {
                            "History content"
                        } else {
                            "${BookBeeSections.all[currentPage].title} content"
                        },
                    )
                }
            }
        }
    }
}
