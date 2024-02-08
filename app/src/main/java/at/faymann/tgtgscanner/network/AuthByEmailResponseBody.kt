package at.faymann.tgtgscanner.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthByEmailResponseBody (
    @SerialName("state")
    val state: String,
    @SerialName("polling_id")
    val pollingId: String
)