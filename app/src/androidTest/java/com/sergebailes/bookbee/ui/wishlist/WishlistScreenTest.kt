package com.sergebailes.bookbee.ui.wishlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import com.sergebailes.bookbee.domain.model.ReadStatus
import com.sergebailes.bookbee.ui.theme.BookBeeTheme
import java.util.UUID
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class WishlistScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_explainsHowToRemoveWithLeftSwipe() {
        setWishlistScreen(
            WishlistUiState(
                isLoading = false,
                items = emptyList(),
            )
        )

        composeRule.onNodeWithText("No wishlist items yet").assertIsDisplayed()
        composeRule.onNodeWithText(
            text = "swipe left",
            substring = true,
            ignoreCase = true,
        ).assertIsDisplayed()
    }

    @Test
    fun browseRows_showEditAndAddToShelf_withoutVisibleRemove_includingOnShelfRow() {
        val firstId = UUID.fromString("00000000-0000-0000-0000-000000000801")
        val secondId = UUID.fromString("00000000-0000-0000-0000-000000000802")
        setWishlistScreen(
            WishlistUiState(
                isLoading = false,
                items = listOf(
                    wishlistItem(
                        id = firstId,
                        title = "The Lathe of Heaven",
                        isOnShelf = false,
                    ),
                    wishlistItem(
                        id = secondId,
                        title = "Parable of the Sower",
                        isOnShelf = true,
                    ),
                ),
            )
        )

        composeRule.onNodeWithText("The Lathe of Heaven").assertIsDisplayed()
        composeRule.onNodeWithText("Parable of the Sower").assertIsDisplayed()
        composeRule.onNodeWithText("Wishlist").assertIsDisplayed()
        composeRule.onNodeWithText("On Shelf").assertIsDisplayed()
        composeRule.onAllNodesWithText("Edit", useUnmergedTree = true).assertCountEquals(2)
        composeRule.onAllNodesWithText("Add to Shelf", useUnmergedTree = true).assertCountEquals(2)
        val removeNodes = composeRule
            .onAllNodesWithText("Remove", useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertTrue(removeNodes.none { it.config.contains(SemanticsActions.OnClick) })
    }

    @Test
    fun browseRow_exposesRemoveAsCustomAccessibilityAction() {
        val itemId = UUID.fromString("00000000-0000-0000-0000-000000000803")
        setWishlistScreen(
            WishlistUiState(
                isLoading = false,
                items = listOf(
                    wishlistItem(
                        id = itemId,
                        title = "A Wizard of Earthsea",
                        isOnShelf = false,
                    )
                ),
            )
        )

        composeRule.onNodeWithTag(
            testTag = "wishlist-item-$itemId",
            useUnmergedTree = true,
        ).assert(hasCustomAccessibilityActionLabel("remove"))
    }

    @Test
    fun browseLayout_dismissesRemovalFeedbackWhenLeavingComposition() {
        var state by mutableStateOf(
            WishlistUiState(
                isLoading = false,
                items = listOf(
                    wishlistItem(
                        title = "Kindred",
                        isOnShelf = false,
                    )
                ),
                removalFeedback = WishlistRemovalFeedback(
                    id = 42L,
                    message = "Wishlist item removed.",
                    actionLabel = "Undo",
                ),
            )
        )
        var dismissedId: Long? = null

        composeRule.setContent {
            BookBeeTheme {
                WishlistScreen(
                    state = state,
                    onAddWishlistItemClicked = {},
                    onEditWishlistItemClicked = { _: UUID -> },
                    onDeleteWishlistItemClicked = { _: UUID -> },
                    onUndoWishlistRemovalClicked = {},
                    onWishlistRemovalFeedbackDismissed = { id -> dismissedId = id },
                    onMoveToShelfClicked = { _: UUID -> },
                    onCancelForm = {},
                    onTitleChanged = { _: String -> },
                    onAuthorChanged = { _: String -> },
                    onIsbnChanged = { _: String -> },
                    onNotesChanged = { _: String -> },
                    onSaveWishlistItemClicked = {},
                    onDismissOwnedOverlapConfirmation = {},
                    onConfirmOwnedOverlapClicked = {},
                    onCancelShelfHandoff = {},
                    onShelfNotesChanged = { _: String -> },
                    onShelfReadStatusChanged = { _: ReadStatus -> },
                    onConfirmMoveToShelfClicked = {},
                    onWishlistRowPointerActiveChanged = {},
                )
            }
        }

        composeRule.onNodeWithText("Kindred").assertIsDisplayed()
        composeRule.runOnIdle {
            state = state.copy(isShowingForm = true)
        }
        composeRule.runOnIdle {
            assertEquals(42L, dismissedId)
        }
    }

    private fun setWishlistScreen(state: WishlistUiState) {
        composeRule.setContent {
            BookBeeTheme {
                WishlistScreen(
                    state = state,
                    onAddWishlistItemClicked = {},
                    onEditWishlistItemClicked = { _: UUID -> },
                    onDeleteWishlistItemClicked = { _: UUID -> },
                    onUndoWishlistRemovalClicked = {},
                    onWishlistRemovalFeedbackDismissed = { _: Long -> },
                    onMoveToShelfClicked = { _: UUID -> },
                    onCancelForm = {},
                    onTitleChanged = { _: String -> },
                    onAuthorChanged = { _: String -> },
                    onIsbnChanged = { _: String -> },
                    onNotesChanged = { _: String -> },
                    onSaveWishlistItemClicked = {},
                    onDismissOwnedOverlapConfirmation = {},
                    onConfirmOwnedOverlapClicked = {},
                    onCancelShelfHandoff = {},
                    onShelfNotesChanged = { _: String -> },
                    onShelfReadStatusChanged = { _: ReadStatus -> },
                    onConfirmMoveToShelfClicked = {},
                    onWishlistRowPointerActiveChanged = {},
                )
            }
        }
    }

    private fun wishlistItem(
        id: UUID = UUID.randomUUID(),
        title: String,
        isOnShelf: Boolean,
    ): WishlistListItem = WishlistListItem(
        id = id,
        title = title,
        authorLine = "Spec Author",
        isbn = "9780141187761",
        notes = "Keep an eye out for this copy.",
        isOnShelf = isOnShelf,
    )

    private fun hasCustomAccessibilityActionLabel(labelFragment: String): SemanticsMatcher =
        SemanticsMatcher("has custom accessibility action containing \"$labelFragment\"") { node ->
            node.config.getOrNull(SemanticsActions.CustomActions)
                ?.any { action -> action.label.contains(labelFragment, ignoreCase = true) }
                ?: false
        }
}
