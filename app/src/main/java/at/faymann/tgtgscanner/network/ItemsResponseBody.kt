package at.faymann.tgtgscanner.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemsResponseBody (
    val items: List<ItemsResponseItem>
)

@Serializable
data class ItemsResponseItem (
    val item: ItemsResponseItemItem,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("items_available")
    val itemsAvailable: Int
)

@Serializable
data class ItemsResponseItemItem (
    @SerialName("item_id")
    val itemId: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String
)
