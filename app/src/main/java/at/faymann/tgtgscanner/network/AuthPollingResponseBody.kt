package at.faymann.tgtgscanner.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthPollingResponseBody (
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("access_token_ttl_seconds")
    val accessTokenTtlSeconds: Int,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("startup_data")
    val startupData: StartupData
)

@Serializable
data class StartupData(
    @SerialName("user")
    val user: User
)

@Serializable
data class User(
    @SerialName("user_id")
    val userId: Int
)