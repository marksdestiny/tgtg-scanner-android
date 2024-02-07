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
            val userId = preferences[Keys.USER_ID]?.toInt() ?: 0
            val dataDome = preferences[Keys.DATA_DOME] ?: ""
            val accessToken = preferences[Keys.ACCESS_TOKEN] ?: "e30.eyJzdWIiOiI5NTMzNzgxMiIsImV4cCI6MTcwNzE0MjQ4MCwidCI6IlpGaWdjcFFVUjN5ejNLSDNobWRscWc6MDoxIn0.NCJUB0tn3daLrDc-N49G39mGNoY-soAtxF1LBP8ihs0"
            val accessTokenTtlLong = preferences[Keys.ACCESS_TOKEN_TTL]
            val accessTokenTtl = if(accessTokenTtlLong != null) {
                LocalDateTime.ofEpochSecond(accessTokenTtlLong, 0, ZoneOffset.UTC)
            } else null
            val refreshToken = preferences[Keys.REFRESH_TOKEN] ?: "e30.eyJzdWIiOiI5NTMzNzgxMiIsImV4cCI6MTczODU5MjA4MCwidCI6IlVZeWNqTnpCUmVtOFhLVW5ySVdkaGc6MDowIn0.UvRhY5QmxymhWoM5prRVpmw0c7kvethKWHTaFSR_6YI"
            val autoCheckIntervalMinutes = preferences[Keys.AUTO_CHECK_INTERVAL] ?: 1
            val lastCheckedLong = preferences[Keys.LAST_CHECKED]
            val lastChecked = if(lastCheckedLong != null) {
                LocalDateTime.ofEpochSecond(lastCheckedLong, 0, ZoneOffset.UTC)
            } else null
            UserPreferences(userId, dataDome, accessToken, accessTokenTtl, refreshToken, autoCheckIntervalMinutes, lastChecked)
        }

    suspend fun updateUserId(userId: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.USER_ID] = userId
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