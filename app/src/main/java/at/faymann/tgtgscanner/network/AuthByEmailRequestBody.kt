package at.faymann.tgtgscanner.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthByEmailRequestBody(
    @SerialName("device_type")
    val deviceType: String,
    @SerialName("email")
    val email: String
)
