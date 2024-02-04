package at.faymann.tgtgscanner.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class BagsRepository {

    val items = MutableStateFlow<List<Bag>>(listOf())

    private val _lastUpdated = MutableStateFlow<Date?>(null)

    /**
     * The date and time the bags have last been synchronized with the TGTG server.
     */
    val lastUpdated = _lastUpdated.asStateFlow()

    fun updateLastUpdated(date: Date) {
        _lastUpdated.value = date
    }

    /**
     * Enable or disable notifications for a specific bag.
     */
    fun updateItemNotificationEnabled(bagId: Int, enabled: Boolean) {
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

    /**
     * Enable or disable notifications all bags.
     */
    fun updateAllItemNotificationsEnabled(enabled: Boolean) {
        items.update { list ->
            return@update list.map { bag ->
                bag.copy(notificationEnabled = enabled)
            }
        }
    }
}