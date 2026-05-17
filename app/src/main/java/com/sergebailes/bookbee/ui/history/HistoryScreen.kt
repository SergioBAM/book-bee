package com.sergebailes.bookbee.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sergebailes.bookbee.domain.model.ReadStatus
import java.util.UUID

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onQueryChanged: (String) -> Unit,
    onEditClicked: (UUID) -> Unit,
    onEditNotesChanged: (String) -> Unit,
    onEditReadStatusChanged: (ReadStatus) -> Unit,
    onCancelEditClicked: () -> Unit,
    onSaveEditClicked: () -> Unit,
    onRestoreClicked: (UUID) -> Unit,
    onHardDeleteClicked: (UUID) -> Unit,
    onCancelHardDeleteClicked: () -> Unit,
    onConfirmHardDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            top = 8.dp,
            end = 24.dp,
            bottom = 120.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search History") },
                    singleLine = true,
                )
            }
        }

        state.message?.let { message ->
            item {
                HistoryMessageCard(message = message)
            }
        }

        state.restoreConflict?.let { conflict ->
            item {
                HistoryMessageCard(
                    message = "Restore blocked by active Shelf ISBN: ${conflict.title}${conflict.authorLine?.let { " - $it" }.orEmpty()}",
                )
            }
        }

        state.hardDeleteConfirmation?.let { confirmation ->
            item {
                HardDeleteConfirmationCard(
                    confirmation = confirmation,
                    onCancelHardDeleteClicked = onCancelHardDeleteClicked,
                    onConfirmHardDeleteClicked = onConfirmHardDeleteClicked,
                )
            }
        }

        state.editForm?.let { form ->
            item {
                HistoryEditCard(
                    form = form,
                    onEditNotesChanged = onEditNotesChanged,
                    onEditReadStatusChanged = onEditReadStatusChanged,
                    onCancelEditClicked = onCancelEditClicked,
                    onSaveEditClicked = onSaveEditClicked,
                )
            }
        }

        if (state.isLoading) {
            item {
                EmptyHistoryCard(
                    title = "Loading History",
                    body = "Archived ownership records are loading from local storage.",
                )
            }
        } else if (state.items.isEmpty()) {
            item {
                EmptyHistoryCard(
                    title = "No History matches",
                    body = "Archived books stay here after you remove the last Shelf copy.",
                )
            }
        } else {
            items(
                items = state.items,
                key = { it.ownershipId },
            ) { item ->
                HistoryBookCard(
                    item = item,
                    onEditClicked = onEditClicked,
                    onRestoreClicked = onRestoreClicked,
                    onHardDeleteClicked = onHardDeleteClicked,
                )
            }
        }
    }
}

@Composable
private fun HistoryBookCard(
    item: HistoryListItem,
    onEditClicked: (UUID) -> Unit,
    onRestoreClicked: (UUID) -> Unit,
    onHardDeleteClicked: (UUID) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            item.authorLine?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = item.readStatus.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            item.isbn?.let {
                Text(
                    text = "ISBN $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item.notes?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onEditClicked(item.ownershipId) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = { onRestoreClicked(item.ownershipId) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Restore")
                }
            }
            Button(
                onClick = { onHardDeleteClicked(item.ownershipId) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Delete permanently")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryEditCard(
    form: HistoryEditFormState,
    onEditNotesChanged: (String) -> Unit,
    onEditReadStatusChanged: (ReadStatus) -> Unit,
    onCancelEditClicked: () -> Unit,
    onSaveEditClicked: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Edit ${form.title}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = form.notes,
                onValueChange = onEditNotesChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 3,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ReadStatus.entries.forEach { readStatus ->
                    FilterChip(
                        selected = form.readStatus == readStatus,
                        onClick = { onEditReadStatusChanged(readStatus) },
                        label = { Text(readStatus.label) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onCancelEditClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSaveEditClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun HardDeleteConfirmationCard(
    confirmation: HistoryHardDeleteConfirmation,
    onCancelHardDeleteClicked: () -> Unit,
    onConfirmHardDeleteClicked: () -> Unit,
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
                text = "Delete \"${confirmation.title}\" permanently?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "This is only available from History. Any linked Wishlist item for this book will also be removed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onCancelHardDeleteClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Keep")
                }
                Button(
                    onClick = onConfirmHardDeleteClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun HistoryMessageCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyHistoryCard(
    title: String,
    body: String,
) {
    Card {
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

private val ReadStatus.label: String
    get() = when (this) {
        ReadStatus.UNREAD -> "Unread"
        ReadStatus.READING -> "Reading"
        ReadStatus.READ -> "Read"
    }
