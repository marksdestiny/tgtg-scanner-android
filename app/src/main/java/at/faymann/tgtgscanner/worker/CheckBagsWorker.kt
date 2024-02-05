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
import kotlinx.coroutines.flow.update
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

        if (application.bagsRepository.lastUpdated.value == null) {
            client.refreshToken()
            update()
        }

        while(!isStopped) {
            val secondsBetweenUpdates = application.userPreferencesRepository.userPreferences.first().autoCheckIntervalMinutes * 60
            val secondsSinceLastUpdate = Duration.between(application.bagsRepository.lastUpdated.value, LocalDateTime.now()).seconds
            delay((secondsBetweenUpdates - secondsSinceLastUpdate) * 1000L)

            update()
        }
        return Result.success()
    }

    private suspend fun update() {
        Log.d("CheckBagsWorker", "Checking bags...")
        val items = client.getItems()

        application.bagsRepository.items.update { bags ->
            checkBags(items, bags)
        }
        application.bagsRepository.updateLastUpdated(LocalDateTime.now())
    }

    private fun checkBags(items: List<TgtgItem>, bags: List<Bag>) : List<Bag> {
        val newBags = bags.toMutableList()
        for (item in items) {
            var bagIndex = -1
            bags.forEachIndexed { index, bag ->
                if (bag.id == item.id)
                    bagIndex = index
            }
            if (bagIndex == -1) {
                newBags.add(Bag(
                    id = item.id,
                    name = item.name,
                    itemsAvailable = item.itemsAvailable,
                    notificationEnabled = true,
                    logoPictureUrl = item.logoPictureUrl,
                    coverPictureUrl = item.coverPictureUrl
                ))
                continue
            }

            val bag = bags[bagIndex]
            if (bag.itemsAvailable == 0 && item.itemsAvailable > 0 && bag.notificationEnabled) {
                Log.d("CheckBagsWorker", "Sending notification...")
                showStockNotification(bag, applicationContext)
                Log.d("CheckBagsWorker", "Done.")
            }
            if (bag.itemsAvailable != item.itemsAvailable) {
                newBags[bagIndex] = bag.copy(itemsAvailable = item.itemsAvailable)
            }
        }
        newBags.sortByDescending { it.itemsAvailable }
        return newBags
    }

}