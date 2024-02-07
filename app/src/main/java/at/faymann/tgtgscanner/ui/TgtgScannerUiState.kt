package at.faymann.tgtgscanner.ui

import at.faymann.tgtgscanner.data.Bag
import java.time.LocalDateTime

data class TgtgScannerUiState(
    val isAutoCheckEnabled: Boolean = false,
    val autoCheckIntervalMinutes: Int = 1,
    val items: List<Bag> = listOf(),
    val lastCheck: LocalDateTime? = null
)