package at.faymann.tgtgscanner.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.faymann.tgtgscanner.TgtgScannerApplication
import at.faymann.tgtgscanner.data.UserPreferencesRepository
import at.faymann.tgtgscanner.data.WorkManagerTgtgScannerRepository
import at.faymann.tgtgscanner.network.TgtgClient
import at.faymann.tgtgscanner.network.TgtgItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

const val TAG = "TgtgScannerViewModel"

data class TgtgScannerUiState(
    val isAutoCheckEnabled: Boolean = false,
    val items: List<TgtgItem> = listOf(),
    val lastUpdated: Date? = null,
    val isRunning: Boolean = false
)

class TgtgScannerViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManagerTgtgScannerRepository: WorkManagerTgtgScannerRepository
): ViewModel() {

    private val client: TgtgClient = TgtgClient(userPreferencesRepository)

    private val _uiState = MutableStateFlow(TgtgScannerUiState())
    val uiState: StateFlow<TgtgScannerUiState>
        get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                client.refreshToken()
                val items = client.getItems()
                _uiState.update { state ->
                    state.copy(
                        items = items,
                        lastUpdated = Date()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

    fun setAutoCheckBagsEnabled(enabled: Boolean) {
        Log.d("TgtgScannerViewModel", enabled.toString())
        _uiState.update { state ->
            state.copy(isAutoCheckEnabled = enabled)
        }
        if (enabled) {
            workManagerTgtgScannerRepository.check()
        } else {
            workManagerTgtgScannerRepository.cancel()
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            try {
                val items = client.getItems()
                _uiState.update { state ->
                    state.copy(
                        items = items,
                        lastUpdated = Date()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TgtgScannerApplication)
                TgtgScannerViewModel(application.userPreferencesRepository, application.workManagerTgtgScannerRepository)
            }
        }
    }
}