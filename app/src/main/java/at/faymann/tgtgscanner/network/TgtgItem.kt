package at.faymann.tgtgscanner.network

data class TgtgItem (
    val id: Int,
    val name: String,
    val itemsAvailable: Int,
    val logoPictureUrl: String,
    val coverPictureUrl: String
)