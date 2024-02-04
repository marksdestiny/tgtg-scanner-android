package at.faymann.tgtgscanner.ui

import at.faymann.tgtgscanner.data.Bag
import java.util.Date

data class TgtgScannerUiState(
    val isAutoCheckEnabled: Boolean = false,
    val autoCheckIntervalMinutes: Int = 1,
    val items: List<Bag> = listOf(),
    val lastUpdated: Date? = null
)