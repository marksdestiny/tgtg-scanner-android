package at.faymann.tgtgscanner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BagDao {
    @Insert
    suspend fun insert(bag: Bag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bags: List<Bag>)

    @Update
    suspend fun update(bag: Bag)

    @Query("UPDATE bag SET notificationEnabled = :enabled")
    suspend fun updateNotificationsEnabled(enabled: Boolean)

    @Query("UPDATE bag SET notificationEnabled = :enabled WHERE id = :id")
    suspend fun updateNotificationsEnabled(id: Int, enabled: Boolean)

    @Delete
    suspend fun delete(bag: Bag)

    @Query("DELETE FROM bag")
    suspend fun deleteAll()

    @Query("SELECT * FROM bag ORDER BY itemsAvailable DESC")
    fun getAllBags() : Flow<List<Bag>>

    @Query("SELECT * FROM bag WHERE id = :id")
    fun getBag(id: Int) : Flow<Bag>
}