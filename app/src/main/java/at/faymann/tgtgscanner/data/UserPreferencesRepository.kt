package at.faymann.tgtgscanner.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class UserPreferences (
    val userId: Int,
    val dataDome: String,
    val accessToken: String,
    val refreshToken: String
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val USER_ID = intPreferencesKey("user_id")
        val DATA_DOME = stringPreferencesKey("data_dome")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
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
            val refreshToken = preferences[Keys.REFRESH_TOKEN] ?: "e30.eyJzdWIiOiI5NTMzNzgxMiIsImV4cCI6MTczODU5MjA4MCwidCI6IlVZeWNqTnpCUmVtOFhLVW5ySVdkaGc6MDowIn0.UvRhY5QmxymhWoM5prRVpmw0c7kvethKWHTaFSR_6YI"
            UserPreferences(userId, dataDome, accessToken, refreshToken)
        }

    suspend fun updateUserId(userId: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.USER_ID] = userId
        }
    }
    suspend fun updateAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = accessToken
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
}