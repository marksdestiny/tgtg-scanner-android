package at.faymann.tgtgscanner

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import at.faymann.tgtgscanner.data.BagsRepository
import at.faymann.tgtgscanner.data.Database
import at.faymann.tgtgscanner.data.UserPreferencesRepository
import at.faymann.tgtgscanner.data.WorkManagerTgtgScannerRepository

private const val USER_PREFERENCES_NAME = "settings"
private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class TgtgScannerApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var workManagerTgtgScannerRepository: WorkManagerTgtgScannerRepository
    lateinit var bagsRepository: BagsRepository
    override fun onCreate() {
        super.onCreate()
        workManagerTgtgScannerRepository = WorkManagerTgtgScannerRepository(this)
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        bagsRepository = BagsRepository(Database.getDatabase(this).bagDao())
    }
}