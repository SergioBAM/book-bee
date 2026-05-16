package com.sergebailes.bookbee.ui.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.sergebailes.bookbee.domain.model.ReadStatus

@Composable
fun WishlistScreen(
    state: WishlistUiState,
    onAddWishlistItemClicked: () -> Unit,
    onEditWishlistItemClicked: (java.util.UUID) -> Unit,
    onDeleteWishlistItemClicked: (java.util.UUID) -> Unit,
    onMoveToShelfClicked: (java.util.UUID) -> Unit,
    onCancelForm: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onAuthorChanged: (String) -> Unit,
    onIsbnChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveWishlistItemClicked: () -> Unit,
    onDismissOwnedOverlapConfirmation: () -> Unit,
    onConfirmOwnedOverlapClicked: () -> Unit,
    onCancelShelfHandoff: () -> Unit,
    onShelfNotesChanged: (String) -> Unit,
    onShelfReadStatusChanged: (ReadStatus) -> Unit,
    onConfirmMoveToShelfClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isShowingForm -> WishlistFormLayout(
            state = state,
            onCancelForm = onCancelForm,
            onTitleChanged = onTitleChanged,
            onAuthorChanged = onAuthorChanged,
            onIsbnChanged = onIsbnChanged,
            onNotesChanged = onNotesChanged,
            onSaveWishlistItemClicked = onSaveWishlistItemClicked,
            modifier = modifier,
        )

        state.shelfHandoff != null -> WishlistShelfHandoffLayout(
            state = state,
            onCancelShelfHandoff = onCancelShelfHandoff,
            onShelfNotesChanged = onShelfNotesChanged,
            onShelfReadStatusChanged = onShelfReadStatusChanged,
            onConfirmMoveToShelfClicked = onConfirmMoveToShelfClicked,
            modifier = modifier,
        )

        else -> WishlistBrowseLayout(
            state = state,
            onAddWishlistItemClicked = onAddWishlistItemClicked,
            onEditWishlistItemClicked = onEditWishlistItemClicked,
            onDeleteWishlistItemClicked = onDeleteWishlistItemClicked,
            onMoveToShelfClicked = onMoveToShelfClicked,
            modifier = modifier,
        )
    }

    state.pendingOwnedOverlapConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = onDismissOwnedOverlapConfirmation,
            title = { Text("Add to Wishlist anyway?") },
            text = {
                Text(
                    text = buildString {
                        append("\"${confirmation.title}\" is already on Shelf.")
                        confirmation.authorLine?.let { authorLine ->
                            append(" ")
                            append(authorLine)
                            append(".")
                        }
                        append(" Save this wishlist item only if you still want another copy or a gift copy.")
                    }
                )
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissOwnedOverlapConfirmation) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(onClick = onConfirmOwnedOverlapClicked) {
                    Text("Save anyway")
                }
            },
        )
    }
}

@Composable
private fun WishlistBrowseLayout(
    state: WishlistUiState,
    onAddWishlistItemClicked: () -> Unit,
    onEditWishlistItemClicked: (java.util.UUID) -> Unit,
    onDeleteWishlistItemClicked: (java.util.UUID) -> Unit,
    onMoveToShelfClicked: (java.util.UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 8.dp,
                end = 24.dp,
                bottom = 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                WishlistHeader(
                    title = "Books you want to find later",
                    supportingText = "Keep Wishlist lightweight. Title is required, notes stay with intent, and the same book drops off automatically once it becomes owned.",
                )
            }

            state.message?.let { message ->
                item {
                    WishlistMessageCard(message = message)
                }
            }

            if (state.isLoading) {
                item {
                    WishlistExplanatoryCard(
                        title = "Loading your wishlist",
                        body = "Book Bee is preparing your saved intent so Wishlist stays fast and local.",
                    )
                }
            } else if (state.items.isEmpty()) {
                item {
                    WishlistExplanatoryCard(
                        title = "No wishlist items yet",
                        body = "Save books here when you want to remember them without marking them as owned.",
                    )
                }
            } else {
                items(
                    items = state.items,
                    key = { it.id },
                ) { item ->
                    WishlistBookCard(
                        item = item,
                        onEditClicked = { onEditWishlistItemClicked(item.id) },
                        onDeleteClicked = { onDeleteWishlistItemClicked(item.id) },
                        onMoveToShelfClicked = { onMoveToShelfClicked(item.id) },
                    )
                }
            }
        }

        WishlistFooter(
            onAddWishlistItemClicked = onAddWishlistItemClicked,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun WishlistFormLayout(
    state: WishlistUiState,
    onCancelForm: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onAuthorChanged: (String) -> Unit,
    onIsbnChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveWishlistItemClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(
            start = 24.dp,
            top = 8.dp,
            end = 24.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            WishlistHeader(
                title = if (state.editingWishlistItemId == null) "Add a wishlist item" else "Edit wishlist item",
                supportingText = "Use the lightest valid data now. ISBN is optional, but preserved whenever you have it.",
            )
        }

        state.message?.let { message ->
            item {
                WishlistMessageCard(message = message)
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = if (state.editingWishlistItemId == null) "Add to Wishlist" else "Update Wishlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = state.form.title,
                        onValueChange = onTitleChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title") },
                        singleLine = true,
                        isError = state.form.titleError != null,
                        supportingText = {
                            Text(state.form.titleError ?: "Required")
                        },
                    )
                    OutlinedTextField(
                        value = state.form.author,
                        onValueChange = onAuthorChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Author") },
                        singleLine = true,
                        supportingText = { Text("Optional free text") },
                    )
                    OutlinedTextField(
                        value = state.form.isbn,
                        onValueChange = onIsbnChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("ISBN") },
                        singleLine = true,
                        isError = state.form.isbnError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        supportingText = {
                            Text(state.form.isbnError ?: "Optional. Must validate as ISBN-10 or ISBN-13 if provided.")
                        },
                    )
                    OutlinedTextField(
                        value = state.form.notes,
                        onValueChange = onNotesChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notes") },
                        minLines = 3,
                        supportingText = { Text("Optional wishlist notes") },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = onCancelForm,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = onSaveWishlistItemClicked,
                            modifier = Modifier.weight(1f),
                            enabled = !state.isSaving,
                        ) {
                            Text(if (state.isSaving) "Saving..." else "Save to Wishlist")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WishlistShelfHandoffLayout(
    state: WishlistUiState,
    onCancelShelfHandoff: () -> Unit,
    onShelfNotesChanged: (String) -> Unit,
    onShelfReadStatusChanged: (ReadStatus) -> Unit,
    onConfirmMoveToShelfClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shelfHandoff = state.shelfHandoff ?: return

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(
            start = 24.dp,
            top = 8.dp,
            end = 24.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            WishlistHeader(
                title = "Move into Shelf",
                supportingText = "Wishlist notes are prefilled here so you can keep what matters, trim what does not, and save ownership cleanly.",
            )
        }

        state.message?.let { message ->
            item {
                WishlistMessageCard(message = message)
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = shelfHandoff.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    shelfHandoff.authorLine?.let { authorLine ->
                        Text(
                            text = authorLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    shelfHandoff.isbn?.let { isbn ->
                        Text(
                            text = "ISBN $isbn",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedTextField(
                        value = shelfHandoff.notes,
                        onValueChange = onShelfNotesChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Shelf notes") },
                        minLines = 3,
                        supportingText = { Text("Prefilled from Wishlist. Edit before saving ownership.") },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Read status",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ReadStatus.entries.forEach { readStatus ->
                                FilterChip(
                                    selected = shelfHandoff.readStatus == readStatus,
                                    onClick = { onShelfReadStatusChanged(readStatus) },
                                    label = { Text(readStatus.label) },
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = onCancelShelfHandoff,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = onConfirmMoveToShelfClicked,
                            modifier = Modifier.weight(1f),
                            enabled = !state.isSaving,
                        ) {
                            Text(if (state.isSaving) "Saving..." else "Save to Shelf")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistHeader(
    title: String,
    supportingText: String? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        supportingText?.let { body ->
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WishlistFooter(
    onAddWishlistItemClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = onAddWishlistItemClicked,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .defaultMinSize(minHeight = 52.dp),
            ) {
                Text("Add wishlist item")
            }
        }
    }
}

@Composable
private fun WishlistMessageCard(
    message: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun WishlistExplanatoryCard(
    title: String,
    body: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WishlistBookCard(
    item: WishlistListItem,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onMoveToShelfClicked: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            item.authorLine?.let { authorLine ->
                Text(
                    text = authorLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.isOnShelf) {
                    WishlistPill(label = "On Shelf")
                } else {
                    WishlistPill(label = "Wishlist")
                }
                item.isbn?.let { isbn ->
                    WishlistPill(label = "ISBN")
                }
            }
            item.isbn?.let { isbn ->
                Text(
                    text = "ISBN $isbn",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item.notes?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onEditClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Edit")
                }
                if (!item.isOnShelf) {
                    Button(
                        onClick = onMoveToShelfClicked,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Add to Shelf")
                    }
                }
                OutlinedButton(
                    onClick = onDeleteClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun WishlistPill(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

private val ReadStatus.label: String
    get() = when (this) {
        ReadStatus.UNREAD -> "Unread"
        ReadStatus.READING -> "Reading"
        ReadStatus.READ -> "Read"
    }
