package at.faymann.tgtgscanner.ui

import android.util.Log
import android.util.Patterns
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
    private val autoCheckIntervalMinutes = MutableStateFlow("")
    private val isAutoCheckIntervalMinutesInvalid = MutableStateFlow(false)
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
        email, isLoggingIn, isChecking, autoCheckIntervalMinutes, isAutoCheckIntervalMinutesInvalid, bags, userPreferences
    ) { email, isLoggingIn, isChecking, autoCheckIntervalMinutes, isAutoCheckIntervalMinutesInvalid, bags, preferences ->
            if (preferences == null || bags == null) {
                return@combine TgtgScannerUiState.Loading
            } else if (preferences.userId == 0) {
                if (isLoggingIn) {
                    return@combine TgtgScannerUiState.Polling
                } else {
                    return@combine TgtgScannerUiState.Login(email = email)
                }
            } else {
                return@combine TgtgScannerUiState.Items(
                    isAutoCheckEnabled = isChecking,
                    isAutoCheckIntervalInvalid = isAutoCheckIntervalMinutesInvalid,
                    autoCheckIntervalMinutes = autoCheckIntervalMinutes,
                    items = bags,
                    lastCheck = preferences.lastCheck
                )
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            TgtgScannerUiState.Login()
        )

    init {
        viewModelScope.launch {
            val preferences = userPreferencesRepository.userPreferences.first()
            email.value = preferences.userEmail
            autoCheckIntervalMinutes.value = preferences.autoCheckIntervalMinutes.toString()
        }
    }

    fun updateEmail(email: String) {
        this.email.value = email
    }

    fun login() {
        if (!email.value.isValidEmail()) {
            return
        }

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

    fun updateAutoCheckInterval(minutes: String) {
        autoCheckIntervalMinutes.value = minutes
        isAutoCheckIntervalMinutesInvalid.value = false
    }

    fun checkAutoCheckInterval() {
        if (!autoCheckIntervalMinutes.value.isValidPositiveInteger()) {
            isAutoCheckIntervalMinutesInvalid.value = true
            return
        }
        viewModelScope.launch {
            userPreferencesRepository.updateAutoCheckInterval(autoCheckIntervalMinutes.value.toInt())
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

private fun CharSequence?.isValidEmail(): Boolean {
    return !this.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

private fun CharSequence?.isValidPositiveInteger(): Boolean {
    if (this.isNullOrEmpty()) {
        return false
    }
    val value = this.toString().toIntOrNull()
    return value != null && value > 0
}

private inline fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> {
    return kotlinx.coroutines.flow.combine(flow, flow2, flow3, flow4, flow5, flow6, flow7) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
            args[6] as T7,
        )
    }
}