package at.faymann.tgtgscanner.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.faymann.tgtgscanner.data.Bag
import coil.compose.AsyncImage

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
        onNotificationEnabledChanged = viewModel::setNotificationEnabled,
        onAllNotificationsEnabledChanged = viewModel::setAllNotificationsEnabled
    )
}

@Composable
fun TgtgScannerScreen(
    uiState: TgtgScannerUiState,
    onAutoCheckEnabledChanged: (Boolean) -> Unit,
    onAutoCheckIntervalChanged: (Int) -> Unit,
    onNotificationEnabledChanged: (Int, Boolean) -> Unit,
    onAllNotificationsEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column (
        modifier = Modifier.padding(4.dp)
    ){
        LazyColumn ( modifier = modifier ) {
            item {
                TgtgScannerHeader(uiState, onAutoCheckEnabledChanged, onAutoCheckIntervalChanged, onAllNotificationsEnabledChanged)
            }
            items(uiState.items) { item ->
                TgtgScannerItem(item, onNotificationEnabledChanged)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TgtgScannerItem(
    item: Bag,
    onNotificationEnabledChanged: (Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    Card(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://share.toogoodtogo.com/item/${item.id}"))
            context.startActivity(intent)
        },
        modifier = Modifier
            .padding(0.dp, 3.dp)
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = item.notificationEnabled,
                onCheckedChange = { onNotificationEnabledChanged(item.id, it) }
            )
            Box(modifier = Modifier.padding(0.dp, 5.dp, 5.dp, 5.dp)) {
                AsyncImage(
                    model = item.logoPictureUrl,
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                )
                if (item.itemsAvailable == 0) {
                    Box(Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6F))
                        .matchParentSize()
                    )
                }
            }
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
    onAutoCheckIntervalChanged: (Int) -> Unit,
    onAllNotificationsEnabledChanged: (Boolean) -> Unit
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
    Row {
        Button(onClick = { onAllNotificationsEnabledChanged(true) }) {
            Text(text = "All")
        }
        Button(onClick = { onAllNotificationsEnabledChanged(false) }) {
            Text(text = "None")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TgtgScannerPreview() {
    val items = listOf(
        Bag(
            id = 1,
            name = "SPAR - Conrad v. Hötzendorf-Str. (Ostbahnhof) (Backwarensackerl)",
            itemsAvailable = 3,
            notificationEnabled = true,
            coverPictureUrl = "https://images.tgtg.ninja/itembulkimport/cover/8f47d323-2260-4324-a776-6562f6565328.jpg",
            logoPictureUrl = "https://images.tgtg.ninja/storebulkimport/logo/26721/098003b3-8765-4fcd-9afa-f92763e172ad.png"),
        Bag(
            id = 2,
            name = "BILLA - Theodor Koerner-Str.122 (BILLA Backwarenkisterl vom Vortag)",
            itemsAvailable = 0,
            notificationEnabled = true,
            coverPictureUrl = "https://images.tgtg.ninja/itembulkimport/cover/88541/4de36459-19ba-4dc8-b820-d3cbd0d91864.jpg",
            logoPictureUrl = "https://images.tgtg.ninja/storebulkimport/logo/87530/e786c77f-ea75-4333-9fcc-0a87ed1f3420.png")
    )
    TgtgScannerScreen(
        TgtgScannerUiState(
            items = items
        ),
        onAutoCheckIntervalChanged = {},
        onAutoCheckEnabledChanged = {},
        onNotificationEnabledChanged = { _, _ ->  },
        onAllNotificationsEnabledChanged = {}
    )
}