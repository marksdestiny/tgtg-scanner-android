package at.faymann.tgtgscanner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.faymann.tgtgscanner.data.Bag

@Composable
fun TgtgScannerApp(
    viewModel: TgtgScannerViewModel = viewModel(
        factory = TgtgScannerViewModel.Factory
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    TgtgScannerScreen(
        uiState,
        onAutoCheckEnabledChanged = viewModel::setAutoCheckBagsEnabled,
        onAutoCheckIntervalChanged = viewModel::setAutoCheckInterval,
        onNotificationEnabledChanged = viewModel::setNotificationEnabled
    )
}

@Composable
fun TgtgScannerScreen(
    uiState: TgtgScannerUiState,
    onAutoCheckEnabledChanged: (Boolean) -> Unit,
    onAutoCheckIntervalChanged: (Int) -> Unit,
    onNotificationEnabledChanged: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column (
        modifier = Modifier.padding(4.dp)
    ){
        LazyColumn ( modifier = modifier ) {
            item {
                TgtgScannerHeader(uiState, onAutoCheckEnabledChanged, onAutoCheckIntervalChanged)
            }
            items(uiState.items) { item ->
                TgtgScannerItem(item, onNotificationEnabledChanged)
            }
        }
    }
}

@Composable
private fun TgtgScannerItem(
    item: Bag,
    onNotificationEnabledChanged: (Int, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(0.dp, 3.dp)
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = item.notificationEnabled,
                onCheckedChange = { onNotificationEnabledChanged(item.id, it) }
            )
            Text(buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(item.name)
                }
                append(" (${item.itemsAvailable})")
            })
        }
    }
}

@Composable
private fun TgtgScannerHeader(
    uiState: TgtgScannerUiState,
    onAutoCheckEnabledChanged: (Boolean) -> Unit,
    onAutoCheckIntervalChanged: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = uiState.isAutoCheckEnabled,
            onCheckedChange = { value -> onAutoCheckEnabledChanged(value) },
        )
        Text(text = "Auto check bags every ")
        TextField(
            value = uiState.autoCheckIntervalMinutes.toString(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            onValueChange = { value ->
                val minutes = value.trim().toIntOrNull()
                if (minutes == null || minutes < 1) {
                    onAutoCheckIntervalChanged(1)
                } else {
                    onAutoCheckIntervalChanged(minutes)
                }
            },
            modifier = Modifier.width(70.dp)
        )
        Text(text = " minutes")
    }
    Row {
        Text(text = "Last update: ")
        Text(text = uiState.lastUpdated?.toString() ?: "Never")
    }
}

@Preview(showBackground = true)
@Composable
fun TgtgScannerPreview() {
    val items = listOf(
        Bag(1,"SPAR - Conrad v. Hötzendorf-Str. (Ostbahnhof) (Backwarensackerl)", 3, true),
        Bag(2,"Billa", 0, true)
    )
    TgtgScannerScreen(
        TgtgScannerUiState(
            items = items
        ),
        onAutoCheckIntervalChanged = {},
        onAutoCheckEnabledChanged = {},
        onNotificationEnabledChanged = { _, _ ->  }
    )
}