package at.faymann.tgtgscanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class Bag (
    @PrimaryKey
    val id: Int,
    val name: String,
    val itemsAvailable: Int,
    val notificationEnabled: Boolean,
    val logoPictureUrl: String,
    val coverPictureUrl: String,
    val lastCheck: LocalDateTime
)