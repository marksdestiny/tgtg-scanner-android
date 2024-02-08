package at.faymann.tgtgscanner.ui

import at.faymann.tgtgscanner.data.Bag
import java.time.LocalDateTime

sealed interface TgtgScannerUiState {

    /**
     * Indicates that the user settings are being loaded.
     */
    data object Loading : TgtgScannerUiState

    data class Login (
        val email: String = ""
    ) : TgtgScannerUiState

    /**
     * Indicates that the app is currently waiting for the E-Mail confirmation.
     */
    data object Polling : TgtgScannerUiState

    data class Items (
        val isAutoCheckEnabled: Boolean = false,
        val autoCheckIntervalMinutes: Int = 1,
        val items: List<Bag> = listOf(),
        val lastCheck: LocalDateTime? = null
    ) : TgtgScannerUiState
}