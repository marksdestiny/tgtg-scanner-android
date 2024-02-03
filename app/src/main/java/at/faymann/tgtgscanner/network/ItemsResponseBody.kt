package at.faymann.tgtgscanner.network

import kotlinx.serialization.Serializable

@Serializable
data class ItemsResponseBody (
    val items: List<TgtgItem>
)