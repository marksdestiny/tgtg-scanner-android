package at.faymann.tgtgscanner.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthPollingRequestBody(
    @SerialName("device_type")
    val deviceType: String,
    @SerialName("email")
    val email: String,
    @SerialName("request_polling_id")
    val requestPollingId: String
)
