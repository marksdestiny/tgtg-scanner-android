package at.faymann.tgtgscanner.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class BagsRepository {

    val items = MutableStateFlow<List<Bag>>(listOf())
    val lastUpdate = MutableStateFlow<Date?>(null)

    fun setNotificationEnabled(bagId: Int, enabled: Boolean) {
        items.update { list ->
            val mutableList = list.toMutableList()
            var bagIndex = -1
            list.forEachIndexed { index, bag ->
                if (bag.id == bagId)
                    bagIndex = index
            }
            if (bagIndex == -1) {
                throw IllegalArgumentException("Illegal bag identifier.")
            }
            mutableList[bagIndex] = mutableList[bagIndex].copy(notificationEnabled = enabled)
            return@update mutableList
        }
    }
}