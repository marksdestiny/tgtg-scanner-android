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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TgtgScannerViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManagerTgtgScannerRepository: WorkManagerTgtgScannerRepository,
    private val bagsRepository: BagsRepository
): ViewModel() {

    private val isAutoCheckEnabled = workManagerTgtgScannerRepository.workInfo.map { info ->
        info.state == WorkInfo.State.ENQUEUED || info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.BLOCKED
    }
    private val bags = bagsRepository.getBags()
    private val userPreferences = userPreferencesRepository.userPreferences

    val uiState: StateFlow<TgtgScannerUiState> = combine(isAutoCheckEnabled, bags, userPreferences) { check, bags, preferences ->
            TgtgScannerUiState(check, preferences.autoCheckIntervalMinutes, bags, preferences.lastCheck)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            TgtgScannerUiState()
        )

    fun setAutoCheckInterval(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateAutoCheckInterval(minutes)
            if (isAutoCheckEnabled.first()) {
                // Restart the worker
                workManagerTgtgScannerRepository.cancel()
                workManagerTgtgScannerRepository.check()
            }
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
        viewModelScope.launch {
            bagsRepository.updateItemNotificationEnabled(bagId, enabled)
        }
    }

    fun setAllNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            bagsRepository.updateAllItemNotificationsEnabled(enabled)
        }
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