package at.faymann.tgtgscanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkInfo
import at.faymann.tgtgscanner.TgtgScannerApplication
import at.faymann.tgtgscanner.data.BagsRepository
import at.faymann.tgtgscanner.data.UserPreferencesRepository
import at.faymann.tgtgscanner.data.WorkManagerTgtgScannerRepository
import at.faymann.tgtgscanner.network.TgtgClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "TgtgScannerViewModel"

class TgtgScannerViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManagerTgtgScannerRepository: WorkManagerTgtgScannerRepository,
    private val bagsRepository: BagsRepository
): ViewModel() {

    private val client: TgtgClient = TgtgClient(userPreferencesRepository)

    private val isAutoCheckEnabled = workManagerTgtgScannerRepository.workInfo.map { info ->
        info.state == WorkInfo.State.ENQUEUED || info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.BLOCKED
    }
    private val lastUpdated = bagsRepository.lastUpdate
    private val bags = bagsRepository.items
    private val userPreferences = userPreferencesRepository.userPreferences

    val uiState: StateFlow<TgtgScannerUiState> = combine(isAutoCheckEnabled, lastUpdated, bags, userPreferences) { check, update, bags, preferences ->
            TgtgScannerUiState(check, preferences.autoCheckIntervalMinutes, bags, update)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            TgtgScannerUiState()
        )

    fun setAutoCheckInterval(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateAutoCheckInterval(minutes)
        }
    }

    fun setAutoCheckBagsEnabled(enabled: Boolean) {
        if (enabled) {
            workManagerTgtgScannerRepository.check()
        } else {
            workManagerTgtgScannerRepository.cancel()
        }
    }

    fun setNotificationEnabled(bagId: Int, enabled: Boolean) {
        bagsRepository.setNotificationEnabled(bagId, enabled)
    }

    fun setAllNotificationsEnabled(enabled: Boolean) {
        bagsRepository.setAllNotificationsEnabled(enabled)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TgtgScannerApplication)
                TgtgScannerViewModel(
                    application.userPreferencesRepository,
                    application.workManagerTgtgScannerRepository,
                    application.bagsRepository)
            }
        }
    }
}