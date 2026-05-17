package com.sergebailes.bookbee.ui.search

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sergebailes.bookbee.domain.usecase.LibrarySearchBadge
import com.sergebailes.bookbee.domain.usecase.LibrarySearchTarget

@Composable
fun LibrarySearchScreen(
    state: LibrarySearchUiState,
    onQueryChanged: (String) -> Unit,
    onResultSelected: (LibrarySearchTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            top = 8.dp,
            end = 24.dp,
            bottom = 120.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search Shelf and Wishlist") },
                singleLine = true,
            )
        }
        when {
            state.query.isBlank() -> {
                item {
                    Text(
                        text = "Search active Shelf and Wishlist records.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            state.results.isEmpty() -> {
                item {
                    Text(
                        text = "No active Shelf or Wishlist matches.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                items(
                    count = state.results.size,
                    key = { index -> state.results[index].bookId },
                ) { index ->
                    LibrarySearchResultRow(
                        result = state.results[index],
                        onResultSelected = onResultSelected,
                    )
                }
            }
        }
        state.message?.let { message ->
            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LibrarySearchResultRow(
    result: LibrarySearchListItem,
    onResultSelected: (LibrarySearchTarget) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onResultSelected(result.target) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = result.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = when (result.target) {
                        LibrarySearchTarget.SHELF -> "Shelf"
                        LibrarySearchTarget.WISHLIST -> "Wishlist"
                        LibrarySearchTarget.HISTORY -> "History"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            result.authorLine?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                result.badges.forEach { badge ->
                    AssistChip(
                        onClick = { onResultSelected(result.target) },
                        label = {
                            Text(
                                when (badge) {
                                    LibrarySearchBadge.ON_SHELF -> "On Shelf"
                                    LibrarySearchBadge.WISHLIST -> "Wishlist"
                                }
                            )
                        },
                    )
                }
                result.isbn?.let { isbn ->
                    AssistChip(
                        onClick = { onResultSelected(result.target) },
                        label = { Text("ISBN $isbn") },
                    )
                }
            }
        }
    }
}
