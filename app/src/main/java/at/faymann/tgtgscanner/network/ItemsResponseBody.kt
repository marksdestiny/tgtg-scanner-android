package at.faymann.tgtgscanner.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemsResponseBody (
    val items: List<ItemsResponseItem>
)

@Serializable
data class ItemsResponseItem (
    @SerialName("item")
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
    val description: String,
    @SerialName("cover_picture")
    val coverPicture: ItemsResponsePicture,
    @SerialName("logo_picture")
    val logoPicture: ItemsResponsePicture
)

@Serializable
data class ItemsResponsePicture (
    @SerialName("picture_id")
    val pictureId: String,
    @SerialName("current_url")
    val currentUrl: String,
    @SerialName("is_automatically_created")
    val isAutomaticallyCreated: Boolean
)
