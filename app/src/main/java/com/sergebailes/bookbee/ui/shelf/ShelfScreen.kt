package com.sergebailes.bookbee.ui.shelf

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sergebailes.bookbee.domain.model.ReadStatus

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
                bottom = if (state.isShowingAddForm) 24.dp else 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ShelfHeader()
            }

            state.message?.let { message ->
                item(span = { GridItemSpan(maxLineSpan) }) {
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
            }

            if (state.isShowingAddForm) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ManualAddShelfBookCard(
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
            } else if (state.isLoading) {
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
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (!state.isShowingAddForm) {
            ShelfFooter(
                onAddBookClicked = onAddBookClicked,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun ShelfHeader(
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Books you currently own",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
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
            .navigationBarsPadding()
        //shadowElevation = 8.dp,
        //tonalElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManualAddShelfBookCard(
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
                supportingText = {
                    Text("Optional free text")
                },
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
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
