package at.faymann.tgtgscanner

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import at.faymann.tgtgscanner.data.UserPreferencesRepository
import at.faymann.tgtgscanner.data.WorkManagerTgtgScannerRepository

private const val USER_PREFERENCES_NAME = "settings"
private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class TgtgScannerApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var workManagerTgtgScannerRepository: WorkManagerTgtgScannerRepository
    override fun onCreate() {
        super.onCreate()
        workManagerTgtgScannerRepository = WorkManagerTgtgScannerRepository(this)
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }
}