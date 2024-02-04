package at.faymann.tgtgscanner.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.faymann.tgtgscanner.TgtgScannerApplication
import at.faymann.tgtgscanner.data.Bag
import at.faymann.tgtgscanner.network.TgtgClient
import kotlinx.coroutines.delay
import java.util.Date

class CheckBagsWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private lateinit var application: TgtgScannerApplication
    private lateinit var bags: MutableList<Bag>
    private lateinit var client: TgtgClient
    override suspend fun doWork(): Result {
        Log.d("CheckBagsWorker", "Checking bags...")
        application = (applicationContext as TgtgScannerApplication)
        bags = application.bagsRepository.items.value.toMutableList()
        client = TgtgClient(application.userPreferencesRepository)
        client.refreshToken()
        while(!isStopped) {
            checkBags()
            delay(60000)
        }
        return Result.success()
    }

    private suspend fun checkBags() {
        Log.d("CheckBagsWorker", "Checking bags...")
        val items = client.getItems()

        for (item in items) {
            var bagIndex = -1
            bags.forEachIndexed { index, bag ->
                if (bag.id == item.id)
                    bagIndex = index
            }
            if (bagIndex == -1) {
                bags.add(Bag(item.id, item.name, item.itemsAvailable))
                continue
            }

            val bag = bags[bagIndex]
            if (bag.itemsAvailable == 0 && item.itemsAvailable > 0) {
                Log.d("CheckBagsWorker", "Sending notification...")
                makeStockNotification(bag.name, applicationContext)
                Log.d("CheckBagsWorker", "Done.")
            }
            if (bag.itemsAvailable != item.itemsAvailable) {
                bags[bagIndex] = bag.copy(itemsAvailable = item.itemsAvailable)
            }
        }
        bags.sortByDescending { it.itemsAvailable }
        application.bagsRepository.lastUpdate.value = Date()
        application.bagsRepository.items.value = bags
    }

}