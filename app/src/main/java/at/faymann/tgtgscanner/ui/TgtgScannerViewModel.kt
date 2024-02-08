package at.faymann.tgtgscanner.ui

import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TgtgScannerViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManagerTgtgScannerRepository: WorkManagerTgtgScannerRepository,
    private val bagsRepository: BagsRepository
): ViewModel() {

    private val email = MutableStateFlow("")
    private val isLoggingIn = MutableStateFlow(false)
    private val isChecking = workManagerTgtgScannerRepository.checkWorkInfo
        .map { info ->
            info.state == WorkInfo.State.ENQUEUED || info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.BLOCKED
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )
    private val bags = bagsRepository.getBags()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null
        )
    private val userPreferences = userPreferencesRepository.userPreferences
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null
        )

    // Use a StateFlow for all parameters before combining, as the combine operator waits for all
    // flows to emit at least one value before it starts combining them.
    // https://medium.com/@theAndroidDeveloper/the-most-important-aspect-of-the-kotlin-flow-operator-combine-e59b2e38fcb2
    val uiState: StateFlow<TgtgScannerUiState> = combine(
        email, isLoggingIn, isChecking, bags, userPreferences
    ) { email, isLoggingIn, isChecking, bags, preferences ->
            if (preferences == null || bags == null) {
                return@combine TgtgScannerUiState.Loading
            } else if (preferences.userId == 0) {
                if (isLoggingIn) {
                    return@combine TgtgScannerUiState.Polling
                } else {
                    return@combine TgtgScannerUiState.Login(email = email)
                }
            } else {
                return@combine TgtgScannerUiState.Items(isChecking, preferences.autoCheckIntervalMinutes, bags, preferences.lastCheck)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            TgtgScannerUiState.Login()
        )

    fun updateEmail(email: String) {
        this.email.value = email
    }

    fun login() {
        viewModelScope.launch {
            userPreferencesRepository.updateUserEmail(email.value)
            isLoggingIn.value = true
            try {
                val client = TgtgClient(userPreferencesRepository)
                client.login()
            } catch(e: Throwable) {
                Log.e("TgtgScannerViewModel", e.message.toString())
            } finally {
                isLoggingIn.value = false
            }
        }
    }

    fun cancelLogin() {

    }

    fun setAutoCheckInterval(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateAutoCheckInterval(minutes)
            if (isChecking.value) {
                // Restart the worker
                workManagerTgtgScannerRepository.cancelCheck()
                workManagerTgtgScannerRepository.check()
            }
        }
    }

    fun setAutoCheckBagsEnabled(enabled: Boolean) {
        if (enabled) {
            workManagerTgtgScannerRepository.check()
        } else {
            workManagerTgtgScannerRepository.cancelCheck()
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