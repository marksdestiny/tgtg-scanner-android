package at.faymann.tgtgscanner.data

data class Bag (
    val id: Int,
    val name: String,
    val itemsAvailable: Int,
    val notificationEnabled: Boolean
)