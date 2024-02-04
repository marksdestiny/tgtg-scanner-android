package at.faymann.tgtgscanner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.faymann.tgtgscanner.network.TgtgItem

@Composable
fun TgtgScannerApp(
    viewModel: TgtgScannerViewModel = viewModel(
        factory = TgtgScannerViewModel.Factory
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    TgtgScannerScreen(
        uiState,
        onRefresh = viewModel::refreshItems,
        onAutoCheckEnabledChanged = viewModel::setAutoCheckBagsEnabled
    )
}

@Composable
fun TgtgScannerScreen(
    uiState: TgtgScannerUiState,
    onAutoCheckEnabledChanged: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column (
        modifier = Modifier.padding(4.dp)
    ){
        Row {
            Checkbox(
                checked = uiState.isAutoCheckEnabled,
                onCheckedChange = { value -> onAutoCheckEnabledChanged(value) },
            )
            Text(text = "Auto check bags", Modifier.padding(0.dp, 12.dp))
        }
        Button(onClick = { onRefresh() }) {
            Text(text = "Refresh")
        }
        Row {
            Text(text = "Last update: ")
            Text(text = uiState.lastUpdated?.toString() ?: "Never")
        }
        LazyColumn ( modifier = modifier ) {
            items(uiState.items) { item ->
                Card (
                    modifier = Modifier                        .padding(0.dp, 3.dp)
                        .fillMaxWidth()
                ){
                    Text(
                        text = item.displayName,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp, 4.dp, 6.dp, 0.dp)
                    )
                    Text(
                        text = item.itemsAvailable.toString(),
                        modifier = Modifier.padding(8.dp, 0.dp, 6.dp, 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TgtgScannerPreview() {
    val items = listOf(
        TgtgItem("Spar", 3),
        TgtgItem("Billa", 0)
    )
    TgtgScannerScreen(
        TgtgScannerUiState(false, items, null, false),
        onRefresh = {},
        onAutoCheckEnabledChanged = {}
    )
}