package at.faymann.tgtgscanner.data

import kotlinx.coroutines.flow.Flow

class BagsRepository(
    private val bagDao: BagDao
) {

    fun getBags(): Flow<List<Bag>> = bagDao.getAllBags()

    suspend fun replace(bags: List<Bag>) {
        bagDao.deleteAll()
        bagDao.insert(bags)
    }

    /**
     * Enable or disable notifications for a specific bag.
     */
    suspend fun updateItemNotificationEnabled(bagId: Int, enabled: Boolean) {
        bagDao.updateNotificationsEnabled(bagId, enabled)
    }

    /**
     * Enable or disable notifications all bags.
     */
    suspend fun updateAllItemNotificationsEnabled(enabled: Boolean) {
        bagDao.updateNotificationsEnabled(enabled)
    }
}