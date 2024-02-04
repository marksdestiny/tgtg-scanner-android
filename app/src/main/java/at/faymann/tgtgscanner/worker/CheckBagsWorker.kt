package at.faymann.tgtgscanner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.faymann.tgtgscanner.TgtgScannerApplication
import kotlinx.coroutines.delay

class CheckBagsWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val application = (applicationContext as TgtgScannerApplication)
        val userPreferences = application.userPreferencesRepository.userPreferences

        delay(5000)
        makeStockNotification("Test", applicationContext)
        return Result.success()
    }

}