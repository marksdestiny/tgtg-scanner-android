package at.faymann.tgtgscanner.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import at.faymann.tgtgscanner.worker.CheckBagsWorker
import kotlinx.coroutines.flow.mapNotNull

private const val CHECK_BAGS_WORK_NAME = "check_bags_work"

class WorkManagerTgtgScannerRepository(
    context: Context
)  {

    private val workManager = WorkManager.getInstance(context)

    val checkWorkInfo = workManager
        .getWorkInfosForUniqueWorkLiveData(CHECK_BAGS_WORK_NAME)
        .asFlow()
        .mapNotNull {
            if (it.isNotEmpty()) it.first() else null
        }

    fun check() {
        Log.d("WorkManagerTgtgScannerRepository", "Check called.")
        val constrains = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val worker = OneTimeWorkRequestBuilder<CheckBagsWorker>()
            .setConstraints(constrains)
            .build()
        workManager.enqueueUniqueWork(
            CHECK_BAGS_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            worker
        )
    }

    fun cancelCheck() {
        Log.d("WorkManagerTgtgScannerRepository", "Cancel called.")
        workManager.cancelUniqueWork(CHECK_BAGS_WORK_NAME)
    }
}