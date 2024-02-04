package at.faymann.tgtgscanner.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.faymann.tgtgscanner.TgtgScannerApplication
import kotlinx.coroutines.delay
import java.util.Date

class CheckBagsWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        Log.d("CheckBagsWorker", "Checking bags...")
        val application = (applicationContext as TgtgScannerApplication)
        val userPreferences = application.userPreferencesRepository.userPreferences
        val bags = application.bagsRepository.items

        delay(5000)
        Log.d("CheckBagsWorker", "Sending notification...")
        makeStockNotification(Date().toString(), applicationContext)
        Log.d("CheckBagsWorker", "Done.")
        return Result.success()
    }

}