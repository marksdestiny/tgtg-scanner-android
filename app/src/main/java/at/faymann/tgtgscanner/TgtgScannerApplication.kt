package at.faymann.tgtgscanner

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import at.faymann.tgtgscanner.data.UserPreferencesRepository

private const val USER_PREFERENCES_NAME = "settings"
private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class TgtgScannerApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }
}