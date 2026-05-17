package com.sergebailes.bookbee.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ScanScreen(
    state: ScanUiState,
    onIsbnChanged: (String) -> Unit,
    onEvaluateManualIsbnClicked: () -> Unit,
    onCancelResultClicked: () -> Unit,
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
                    text = "Scan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                CameraFallbackCard(state.cameraState)
            }
        }

        state.message?.let { message ->
            item {
                MessageCard(message = message)
            }
        }

        item {
            ManualIsbnCard(
                state = state,
                onIsbnChanged = onIsbnChanged,
                onEvaluateManualIsbnClicked = onEvaluateManualIsbnClicked,
            )
        }

        state.result?.let { result ->
            item {
                ScanResultCard(
                    result = result,
                    onCancelResultClicked = onCancelResultClicked,
                )
            }
        }
    }
}

@Composable
private fun CameraFallbackCard(cameraState: ScanCameraState) {
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
                text = when (cameraState) {
                    ScanCameraState.UNAVAILABLE -> "Camera scanner unavailable"
                    ScanCameraState.PERMISSION_DENIED -> "Camera permission denied"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Manual ISBN entry stays available so ownership checks still work.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ManualIsbnCard(
    state: ScanUiState,
    onIsbnChanged: (String) -> Unit,
    onEvaluateManualIsbnClicked: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Manual ISBN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = state.isbn,
                onValueChange = onIsbnChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("ISBN") },
                singleLine = true,
                isError = state.isbnError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                supportingText = {
                    Text(state.isbnError ?: "ISBN-10 or ISBN-13")
                },
            )
            Button(
                onClick = onEvaluateManualIsbnClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                Text("Check ownership")
            }
        }
    }
}

@Composable
private fun ScanResultCard(
    result: ScanResultUiState,
    onCancelResultClicked: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (result.status) {
                ScanOwnershipUiStatus.OWNED -> MaterialTheme.colorScheme.secondaryContainer
                ScanOwnershipUiStatus.NOT_OWNED -> MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = when (result.status) {
                    ScanOwnershipUiStatus.OWNED -> "Owned"
                    ScanOwnershipUiStatus.NOT_OWNED -> "Not owned"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "ISBN ${result.isbn}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            result.ownedBook?.let {
                ContextLine(label = "Shelf", context = it)
            }
            result.previouslyOwnedBook?.let {
                ContextLine(label = "Previously owned", context = it)
            }
            result.wishlistBook?.let {
                ContextLine(label = "Wishlist", context = it)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onCancelResultClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onCancelResultClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Scan again")
                }
            }
        }
    }
}

@Composable
private fun ContextLine(
    label: String,
    context: ScanBookContext,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = listOfNotNull(context.title, context.authorLine).joinToString(" - "),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MessageCard(message: String) {
    Card {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
