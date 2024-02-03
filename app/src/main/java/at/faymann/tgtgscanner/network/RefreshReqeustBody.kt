package at.faymann.tgtgscanner.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshReqeustBody(
    @SerialName("refresh_token")
    val refreshToken: String
)
