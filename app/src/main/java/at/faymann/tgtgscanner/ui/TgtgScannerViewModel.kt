package at.faymann.tgtgscanner.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.faymann.tgtgscanner.TgtgScannerApplication
import at.faymann.tgtgscanner.data.UserPreferencesRepository
import at.faymann.tgtgscanner.network.TgtgClient
import at.faymann.tgtgscanner.network.TgtgItem
import kotlinx.coroutines.launch
import java.util.Date

const val TAG = "TgtgScannerViewModel"

data class TgtgScannerUiState(
    val items: List<TgtgItem>,
    val lastUpdated: Date?,
    val isRunning: Boolean
)

class TgtgScannerViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {

    private val client: TgtgClient = TgtgClient(userPreferencesRepository)

    var tgtgScannerUiState: TgtgScannerUiState by mutableStateOf(
        TgtgScannerUiState(
            listOf<TgtgItem>(),
            null,
            false
        ))
        private set

    init {
        viewModelScope.launch {
            try {
                client.refreshToken()
                val items = client.getItems()
                tgtgScannerUiState = TgtgScannerUiState(
                    items = items,
                    lastUpdated = Date(),
                    isRunning = false
                )
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            try {
                val items = client.getItems()
                tgtgScannerUiState = TgtgScannerUiState(
                    items = items,
                    lastUpdated = Date(),
                    isRunning = false
                )
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TgtgScannerApplication)
                TgtgScannerViewModel(application.userPreferencesRepository)
            }
        }
    }
}