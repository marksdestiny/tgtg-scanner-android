package at.faymann.tgtgscanner.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshResponseBody (
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("access_token_ttl_seconds")
    val accessTokenTtlSeconds: Int,
    @SerialName("refresh_token")
    val refreshToken: String
)