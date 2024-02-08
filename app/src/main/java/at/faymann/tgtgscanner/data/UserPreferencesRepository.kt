package at.faymann.tgtgscanner.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset

data class UserPreferences (
    val userId: Int,
    val userEmail: String,
    val dataDome: String,
    val accessToken: String,
    val accessTokenTtl: LocalDateTime?,
    val refreshToken: String,
    val autoCheckIntervalMinutes: Int,
    val lastCheck: LocalDateTime?
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ID = intPreferencesKey("user_id")
        val DATA_DOME = stringPreferencesKey("data_dome")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val ACCESS_TOKEN_TTL = longPreferencesKey("access_token_ttl")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val AUTO_CHECK_INTERVAL = intPreferencesKey("auto_check_interval")
        val LAST_CHECKED = longPreferencesKey("last_updated")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch {exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                userId = preferences[Keys.USER_ID]?.toInt() ?: 0,
                userEmail = preferences[Keys.USER_EMAIL] ?: "",
                dataDome = preferences[Keys.DATA_DOME] ?: "",
                accessToken = preferences[Keys.ACCESS_TOKEN] ?: "",
                accessTokenTtl = preferences[Keys.ACCESS_TOKEN_TTL].toLocalDateTime(),
                refreshToken = preferences[Keys.REFRESH_TOKEN] ?: "",
                autoCheckIntervalMinutes = preferences[Keys.AUTO_CHECK_INTERVAL] ?: 1,
                lastCheck = preferences[Keys.LAST_CHECKED].toLocalDateTime())
        }

    suspend fun updateUserData(userId: Int, accessToken: String, accessTokenTtl: LocalDateTime, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[Keys.USER_ID] = userId
            preferences[Keys.ACCESS_TOKEN] = accessToken
            preferences[Keys.ACCESS_TOKEN_TTL] = accessTokenTtl.toEpochSecond(ZoneOffset.UTC)
            preferences[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun updateUserEmail(userEmail: String) {
        dataStore.edit { preferences ->
            preferences[Keys.USER_EMAIL] = userEmail
        }
    }

    suspend fun updateAutoCheckInterval(minutes: Int) {
        if (minutes < 1) {
            throw IllegalArgumentException("The auto check interval must be at least one minute.")
        }
        dataStore.edit { preferences ->
            preferences[Keys.AUTO_CHECK_INTERVAL] = minutes
        }
    }
    suspend fun updateAccessToken(accessToken: String, accessTokenTtl: LocalDateTime) {
        dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = accessToken
            preferences[Keys.ACCESS_TOKEN_TTL] = accessTokenTtl.toEpochSecond(ZoneOffset.UTC)
        }
    }
    suspend fun updateRefreshToken(refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[Keys.REFRESH_TOKEN] = refreshToken
        }
    }
    suspend fun updateDataDome(dataDome: String) {
        dataStore.edit { preferences ->
            preferences[Keys.DATA_DOME] = dataDome
        }
    }

    suspend fun updateLastChecked(date: LocalDateTime) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_CHECKED] = date.toEpochSecond(ZoneOffset.UTC)
        }
    }
}

private fun Long?.toLocalDateTime(): LocalDateTime? {
    return if(this != null) {
        LocalDateTime.ofEpochSecond(this, 0, ZoneOffset.UTC)
    } else {
        null
    }
}