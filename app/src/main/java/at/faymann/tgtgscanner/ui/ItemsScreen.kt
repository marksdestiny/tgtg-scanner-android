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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.faymann.tgtgscanner.data.Bag
import coil.compose.AsyncImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ItemsScreen(
    uiState: TgtgScannerUiState.Items,
    onAutoCheckEnabledChanged: (Boolean) -> Unit,
    onAutoCheckIntervalChanged: (String) -> Unit,
    onAutoCheckIntervalDone: () -> Unit,
    onNotificationEnabledChanged: (Int, Boolean) -> Unit,
    onAllNotificationsEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.padding(4.dp)
    ) {
        LazyColumn(modifier = modifier) {
            item {
                ItemsScreenHeader(
                    uiState = uiState,
                    onAutoCheckEnabledChanged = onAutoCheckEnabledChanged,
                    onAutoCheckIntervalChanged = onAutoCheckIntervalChanged,
                    onAutoCheckIntervalDone = onAutoCheckIntervalDone,
                    onAllNotificationsEnabledChanged = onAllNotificationsEnabledChanged
                )
            }
            items(uiState.items) { item ->
                ItemsScreenCard(item, onNotificationEnabledChanged)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemsScreenCard(
    item: Bag,
    onNotificationEnabledChanged: (Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    Card(
        onClick = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://share.toogoodtogo.com/item/${item.id}")
            )
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
                    Box(
                        Modifier
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
private fun ItemsScreenHeader(
    uiState: TgtgScannerUiState.Items,
    onAutoCheckEnabledChanged: (Boolean) -> Unit,
    onAutoCheckIntervalChanged: (String) -> Unit,
    onAutoCheckIntervalDone: () -> Unit,
    onAllNotificationsEnabledChanged: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = uiState.isAutoCheckEnabled,
            onCheckedChange = { value -> onAutoCheckEnabledChanged(value) },
        )
        Text(text = "Auto check bags every ")
        TextField(
            value = uiState.autoCheckIntervalMinutes,
            singleLine = true,
            onValueChange = { value ->
                onAutoCheckIntervalChanged(value)
            },
            keyboardActions = KeyboardActions (
                onDone = {
                    onAutoCheckIntervalDone()
                    focusManager.clearFocus()
                }
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            isError = uiState.isAutoCheckIntervalInvalid,
            modifier = Modifier.width(70.dp)
        )
        Text(text = " minutes")
    }
    Row {
        Text(text = "Last update: ")
        Text(
            text = uiState.lastCheck?.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"))
                ?: "Never"
        )
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
fun ItemsScreenPreview() {
    val items = listOf(
        Bag(
            id = 1,
            name = "SPAR - Conrad v. Hötzendorf-Str. (Ostbahnhof) (Backwarensackerl)",
            itemsAvailable = 3,
            notificationEnabled = true,
            coverPictureUrl = "https://images.tgtg.ninja/itembulkimport/cover/8f47d323-2260-4324-a776-6562f6565328.jpg",
            logoPictureUrl = "https://images.tgtg.ninja/storebulkimport/logo/26721/098003b3-8765-4fcd-9afa-f92763e172ad.png",
            lastCheck = LocalDateTime.now()
        ),
        Bag(
            id = 2,
            name = "BILLA - Theodor Koerner-Str.122 (BILLA Backwarenkisterl vom Vortag)",
            itemsAvailable = 0,
            notificationEnabled = true,
            coverPictureUrl = "https://images.tgtg.ninja/itembulkimport/cover/88541/4de36459-19ba-4dc8-b820-d3cbd0d91864.jpg",
            logoPictureUrl = "https://images.tgtg.ninja/storebulkimport/logo/87530/e786c77f-ea75-4333-9fcc-0a87ed1f3420.png",
            lastCheck = LocalDateTime.now()
        )
    )
    ItemsScreen(
        TgtgScannerUiState.Items(
            items = items
        ),
        onAutoCheckIntervalChanged = {},
        onAutoCheckEnabledChanged = {},
        onAutoCheckIntervalDone = {},
        onNotificationEnabledChanged = { _, _ ->  },
        onAllNotificationsEnabledChanged = {}
    )
}