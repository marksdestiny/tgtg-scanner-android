package at.faymann.tgtgscanner.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TgtgItem (
    @SerialName("display_name")
    val displayName: String,

    @SerialName("items_available")
    val itemsAvailable: Int
)