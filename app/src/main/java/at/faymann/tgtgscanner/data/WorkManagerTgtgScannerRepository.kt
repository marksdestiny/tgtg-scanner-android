package at.faymann.tgtgscanner.data

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import at.faymann.tgtgscanner.worker.CheckBagsWorker
import java.time.Duration

const val CHECK_BAGS_WORK_NAME = "check_bags_work"

class WorkManagerTgtgScannerRepository(
    private val context: Context
)  {

    private val workManager = WorkManager.getInstance(context)

    fun check() {
        Log.d("WorkManagerTgtgScannerRepository", "Check called.")
        val worker = PeriodicWorkRequestBuilder<CheckBagsWorker>(Duration.ofMinutes(1)).build()
        workManager.enqueueUniquePeriodicWork(
            CHECK_BAGS_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            worker
        )
    }

    fun cancel() {
        Log.d("WorkManagerTgtgScannerRepository", "Cancel called.")
        workManager.cancelAllWork()
    }
}