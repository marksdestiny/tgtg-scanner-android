package at.faymann.tgtgscanner.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.faymann.tgtgscanner.TgtgScannerApplication
import at.faymann.tgtgscanner.data.Bag
import at.faymann.tgtgscanner.network.TgtgClient
import at.faymann.tgtgscanner.network.TgtgItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime

class CheckBagsWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private lateinit var application: TgtgScannerApplication
    private lateinit var client: TgtgClient

    override suspend fun doWork(): Result {
        Log.d("CheckBagsWorker", "Checking bags...")

        setForeground(makeCheckForegroundInfo(applicationContext))

        application = (applicationContext as TgtgScannerApplication)
        client = TgtgClient(application.userPreferencesRepository)

        if (application.userPreferencesRepository.userPreferences.first().lastCheck == null) {
            update()
        }

        while(!isStopped) {
            val currentPreferences = application.userPreferencesRepository.userPreferences.first()
            val secondsBetweenUpdates = currentPreferences.autoCheckIntervalMinutes * 60
            val secondsSinceLastUpdate = Duration.between(currentPreferences.lastCheck, LocalDateTime.now()).seconds
            delay((secondsBetweenUpdates - secondsSinceLastUpdate) * 1000L)

            update()
        }
        return Result.success()
    }

    private suspend fun update() {
        Log.d("CheckBagsWorker", "Checking bags...")
        val items = client.getItems()

        val bags = application.bagsRepository.getBags().first()
        val updatedBags = checkBags(items,bags)
        application.bagsRepository.replace(updatedBags)

        application.userPreferencesRepository.updateLastChecked(LocalDateTime.now())
    }

    private fun checkBags(items: List<TgtgItem>, bags: List<Bag>) : List<Bag> {
        val lastCheck = LocalDateTime.now()
        val mutableBags = bags.toMutableList()
        for (item in items) {
            var bagIndex = -1
            mutableBags.forEachIndexed { index, bag ->
                if (bag.id == item.id)
                    bagIndex = index
            }
            if (bagIndex == -1) {
                mutableBags.add(Bag(
                    id = item.id,
                    name = item.name,
                    itemsAvailable = item.itemsAvailable,
                    notificationEnabled = true,
                    logoPictureUrl = item.logoPictureUrl,
                    coverPictureUrl = item.coverPictureUrl,
                    lastCheck = lastCheck
                ))
                continue
            }

            var bag = mutableBags[bagIndex]
            if (bag.itemsAvailable == 0 && item.itemsAvailable > 0 && bag.notificationEnabled) {
                Log.d("CheckBagsWorker", "Sending notification...")
                showStockNotification(bag, applicationContext)
                Log.d("CheckBagsWorker", "Done.")
            }
            bag = if (bag.itemsAvailable != item.itemsAvailable) {
                bag.copy(
                    itemsAvailable = item.itemsAvailable,
                    lastCheck = lastCheck
                )
            } else {
                bag.copy(lastCheck = lastCheck)
            }
            mutableBags[bagIndex] = bag
        }
        return mutableBags
    }

}