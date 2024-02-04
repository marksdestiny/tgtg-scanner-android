package at.faymann.tgtgscanner.data

import android.content.Context
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
        val worker = PeriodicWorkRequestBuilder<CheckBagsWorker>(Duration.ofMinutes(5)).build()
        workManager.enqueueUniquePeriodicWork(
            CHECK_BAGS_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            worker
        )
    }

    fun cancel() {
        workManager.cancelAllWork()
    }
}