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
import java.util.Date

class CheckBagsWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private lateinit var application: TgtgScannerApplication
    private lateinit var client: TgtgClient

    override suspend fun doWork(): Result {
        Log.d("CheckBagsWorker", "Checking bags...")
        application = (applicationContext as TgtgScannerApplication)
        client = TgtgClient(application.userPreferencesRepository)
        client.refreshToken()
        while(!isStopped) {
            Log.d("CheckBagsWorker", "Checking bags...")
            val items = client.getItems()

            application.bagsRepository.items.update {bags ->
                checkBags(items, bags)
            }
            application.bagsRepository.updateLastUpdated(Date())

            val delayMinutes = application.userPreferencesRepository.userPreferences.first().autoCheckIntervalMinutes
            delay(delayMinutes * 60000L)
        }
        return Result.success()
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
                newBags.add(Bag(item.id, item.name, item.itemsAvailable, true))
                continue
            }

            val bag = bags[bagIndex]
            if (bag.itemsAvailable == 0 && item.itemsAvailable > 0 && bag.notificationEnabled) {
                Log.d("CheckBagsWorker", "Sending notification...")
                makeStockNotification(bag.name, applicationContext)
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