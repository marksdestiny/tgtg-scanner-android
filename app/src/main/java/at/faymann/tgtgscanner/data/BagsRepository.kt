package at.faymann.tgtgscanner.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class BagsRepository {

    val items = MutableStateFlow<List<Bag>>(listOf())

    private val _lastUpdated = MutableStateFlow<Date?>(null)
    val lastUpdated = _lastUpdated.asStateFlow()

    fun updateLastUpdated(date: Date) {
        _lastUpdated.value = date
    }

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

    fun updateAllItemNotificationsEnabled(enabled: Boolean) {
        items.update { list ->
            return@update list.map { bag ->
                bag.copy(notificationEnabled = enabled)
            }
        }
    }
}