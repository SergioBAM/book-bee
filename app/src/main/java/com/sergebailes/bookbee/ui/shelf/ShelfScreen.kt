package com.sergebailes.bookbee.ui.shelf

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sergebailes.bookbee.domain.model.ReadStatus
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun ShelfScreen(
    state: ShelfUiState,
    onAddBookClicked: () -> Unit,
    onCancelAddBook: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onAuthorChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onIsbnChanged: (String) -> Unit,
    onReadStatusChanged: (ReadStatus) -> Unit,
    onSaveBookClicked: () -> Unit,
    onAddAnotherCopyClicked: (UUID) -> Unit,
    onUndoAddAnotherCopyClicked: () -> Unit,
    onRemoveCopyClicked: (UUID) -> Unit,
    onConfirmArchiveClicked: () -> Unit,
    onCancelArchiveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isShowingAddForm) {
        ManualAddShelfLayout(
            state = state,
            onCancelAddBook = onCancelAddBook,
            onTitleChanged = onTitleChanged,
            onAuthorChanged = onAuthorChanged,
            onNotesChanged = onNotesChanged,
            onIsbnChanged = onIsbnChanged,
            onReadStatusChanged = onReadStatusChanged,
            onSaveBookClicked = onSaveBookClicked,
            onAddAnotherCopyClicked = onAddAnotherCopyClicked,
            modifier = modifier,
        )
    } else {
        ShelfBrowseLayout(
            state = state,
            onAddBookClicked = onAddBookClicked,
            onUndoAddAnotherCopyClicked = onUndoAddAnotherCopyClicked,
            onRemoveCopyClicked = onRemoveCopyClicked,
            onAddAnotherCopyClicked = onAddAnotherCopyClicked,
            onConfirmArchiveClicked = onConfirmArchiveClicked,
            onCancelArchiveClicked = onCancelArchiveClicked,
            modifier = modifier,
        )
    }
}

@Composable
private fun ShelfBrowseLayout(
    state: ShelfUiState,
    onAddBookClicked: () -> Unit,
    onUndoAddAnotherCopyClicked: () -> Unit,
    onRemoveCopyClicked: (UUID) -> Unit,
    onAddAnotherCopyClicked: (UUID) -> Unit,
    onConfirmArchiveClicked: () -> Unit,
    onCancelArchiveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 8.dp,
                end = 24.dp,
                bottom = 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ShelfHeader(
                    title = "Books you currently own",
                )
            }

            state.message?.let { message ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    InlineMessageCard(message = message)
                }
            }

            state.copyFeedback?.let { feedback ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ActionMessageCard(
                        message = feedback.message,
                        actionLabel = feedback.actionLabel,
                        onActionClicked = onUndoAddAnotherCopyClicked,
                    )
                }
            }

            state.archiveConfirmation?.let { confirmation ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ArchiveConfirmationCard(
                        confirmation = confirmation,
                        onConfirmArchiveClicked = onConfirmArchiveClicked,
                        onCancelArchiveClicked = onCancelArchiveClicked,
                    )
                }
            }

            if (state.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ExplanatoryCard(
                        title = "Loading your shelf",
                        body = "Book Bee is preparing your local owned books so Shelf stays useful offline.",
                    )
                }
            } else if (state.books.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ExplanatoryCard(
                        title = "No owned books yet",
                        body = "Shelf is the place for books you currently own. Start with a quick manual add, with ISBN optional and fully checked if you enter one.",
                    )
                }
            } else {
                items(
                    items = state.books,
                    key = { it.id },
                ) { book ->
                    ShelfBookCard(
                        book = book,
                        onRemoveCopyClicked = onRemoveCopyClicked,
                        onAddAnotherCopyClicked = onAddAnotherCopyClicked,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        ShelfFooter(
            onAddBookClicked = onAddBookClicked,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManualAddShelfLayout(
    state: ShelfUiState,
    onCancelAddBook: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onAuthorChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onIsbnChanged: (String) -> Unit,
    onReadStatusChanged: (ReadStatus) -> Unit,
    onSaveBookClicked: () -> Unit,
    onAddAnotherCopyClicked: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .imeNestedScroll(),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 8.dp,
                end = 24.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ShelfHeader(
                    title = "Add a book to Shelf",
                    supportingText = "Enter the basics now. The form scrolls so you can keep working even when the keyboard is open.",
                )
            }

            state.message?.let { message ->
                item {
                    InlineMessageCard(message = message)
                }
            }

            state.duplicateConflict?.let { conflict ->
                item {
                    DuplicateConflictCard(
                        conflict = conflict,
                        onAddAnotherCopyClicked = onAddAnotherCopyClicked,
                    )
                }
            }

            item {
                ManualAddShelfBookForm(
                    state = state,
                    onCancelAddBook = onCancelAddBook,
                    onTitleChanged = onTitleChanged,
                    onAuthorChanged = onAuthorChanged,
                    onNotesChanged = onNotesChanged,
                    onIsbnChanged = onIsbnChanged,
                    onReadStatusChanged = onReadStatusChanged,
                    onSaveBookClicked = onSaveBookClicked,
                )
            }
        }
    }
}

@Composable
private fun ShelfHeader(
    title: String,
    supportingText: String? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
private fun ShelfFooter(
    onAddBookClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = onAddBookClicked,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .defaultMinSize(minHeight = 52.dp),
            ) {
                Text("Add book")
            }
        }
    }
}

@Composable
private fun InlineMessageCard(
    message: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ActionMessageCard(
    message: String,
    actionLabel: String,
    onActionClicked: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(onClick = onActionClicked) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun DuplicateConflictCard(
    conflict: ShelfDuplicateConflict,
    onAddAnotherCopyClicked: (UUID) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Already on Shelf",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = listOfNotNull(conflict.title, conflict.authorLine).joinToString(" - "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Button(
                onClick = { onAddAnotherCopyClicked(conflict.bookId) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add another copy")
            }
        }
    }
}

@Composable
private fun ArchiveConfirmationCard(
    confirmation: ShelfArchiveConfirmation,
    onConfirmArchiveClicked: () -> Unit,
    onCancelArchiveClicked: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Archive \"${confirmation.title}\"?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Removing the last copy archives this Shelf record instead of saving quantity zero.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onCancelArchiveClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Keep")
                }
                Button(
                    onClick = onConfirmArchiveClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Archive")
                }
            }
        }
    }
}

@Composable
private fun ExplanatoryCard(
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ManualAddShelfBookForm(
    state: ShelfUiState,
    onCancelAddBook: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onAuthorChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onIsbnChanged: (String) -> Unit,
    onReadStatusChanged: (ReadStatus) -> Unit,
    onSaveBookClicked: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Add to Shelf",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = state.form.title,
                onValueChange = onTitleChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus(),
                label = { Text("Author") },
                singleLine = true,
                supportingText = {
                    Text("Optional free text")
                },
            )
            OutlinedTextField(
                value = state.form.isbn,
                onValueChange = onIsbnChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus(),
                label = { Text("Notes") },
                minLines = 3,
                supportingText = {
                    Text("Optional shelf notes")
                },
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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
                            selected = state.form.readStatus == readStatus,
                            onClick = { onReadStatusChanged(readStatus) },
                            label = { Text(readStatus.label) },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onCancelAddBook,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSaveBookClicked,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSaving,
                ) {
                    Text(if (state.isSaving) "Saving..." else "Save to Shelf")
                }
            }
        }
    }
}

@Composable
private fun ShelfBookCard(
    book: ShelfBookListItem,
    onRemoveCopyClicked: (UUID) -> Unit,
    onAddAnotherCopyClicked: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            book.authorLine?.let { authorLine ->
                Text(
                    text = authorLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatPill(label = "Qty ${book.quantity}")
                StatPill(label = book.readStatus.label)
            }
            book.isbn?.let { isbn ->
                Text(
                    text = "ISBN $isbn",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            book.notes?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onRemoveCopyClicked(book.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Remove copy")
                }
                Button(
                    onClick = { onAddAnotherCopyClicked(book.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Add copy")
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(999.dp),
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

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.bringIntoViewOnFocus(): Modifier = composed {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    bringIntoViewRequester(requester)
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                scope.launch {
                    requester.bringIntoView()
                }
            }
        }
}
