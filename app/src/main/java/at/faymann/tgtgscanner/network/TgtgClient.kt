package at.faymann.tgtgscanner.network

import android.util.Log
import at.faymann.tgtgscanner.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

private const val BASE_URL: String = "https://apptoogoodtogo.com/api/"
//const val BASE_URL: String = "https://httpbin.org/anything/"
private const val ITEM_ENDPOINT: String = "item/v8/"
private const val REFRESH_ENDPOINT: String = "auth/v3/token/refresh"

private const val USER_AGENT: String = "TGTG/24.1.12 Dalvik/2.1.0 (Linux; U; Android 9; Nexus 5 Build/M4B30Z)"

private const val TAG = "TgtgClient"

class TgtgClient (
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val userId = 95337812
    private val client: OkHttpClient

    init {
        client = OkHttpClient()
    }

    suspend fun refreshToken() {
        val accessToken = userPreferencesRepository.userPreferences.map { it.accessToken }.first()
        val refreshToken = userPreferencesRepository.userPreferences.map { it.refreshToken }.first()
        val dataDome = userPreferencesRepository.userPreferences.map { it.dataDome }.first()

        val data = RefreshReqeustBody(refreshToken)
        val json = Json.encodeToString(data)
        val body: RequestBody = json.toRequestBody("application/json".toMediaType())
        val requestBuilder = Request.Builder()
            .url(BASE_URL + REFRESH_ENDPOINT)
            .post(body)
            .header("Accept", "application/json")
            .header("Accept-Language", "en-GB")
            .header("Authorization", "Bearer $accessToken")
            .header("User-Agent", USER_AGENT)
        if (dataDome != "") {
            requestBuilder.header("Cookie", "datadome=$dataDome")
        }
        val request = requestBuilder.build()
        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }
        if (response.body == null) {
            throw Exception("Response body is empty.")
        }
        val responseString = response.body!!.string()
        Log.d(TAG, responseString)

        val refreshResponseBody = Json.decodeFromString<RefreshResponseBody>(responseString)
        userPreferencesRepository.updateRefreshToken(refreshResponseBody.refreshToken)
        userPreferencesRepository.updateAccessToken(refreshResponseBody.accessToken)

        parseDataDome(response)
    }

    private suspend fun parseDataDome(response: Response) {
        val responseCookieString = response.header("Set-Cookie")
            ?: throw Exception("Response cookie is missing.")
        Log.d(TAG, responseCookieString)

        val responseCookie = Cookie.parse(response.request.url, responseCookieString)
            ?: throw Exception("Response cookie not well-formed.")
        userPreferencesRepository.updateDataDome(responseCookie.value)
    }

    suspend fun getItems() : List<TgtgItem> {
        val accessToken = userPreferencesRepository.userPreferences.map { it.accessToken }.first()
        val dataDome = userPreferencesRepository.userPreferences.map { it.dataDome }.first()

        val json = "{\"user_id\": \"$userId\", \"origin\": {\"latitude\": 0.0, \"longitude\": 0.0}, \"radius\": 21, \"page_size\": 100, \"page\": 1, \"discover\": false, \"favorites_only\": true, \"item_categories\": [], \"diet_categories\": [], \"pickup_earliest\": null, \"pickup_latest\": null, \"search_phrase\": null, \"with_stock_only\": false, \"hidden_only\": false, \"we_care_only\": false}"
        val body: RequestBody = json.toRequestBody("application/json".toMediaType())

        val request: Request = Request.Builder()
            .url(BASE_URL + ITEM_ENDPOINT)
            .post(body)
            .header("Accept", "application/json")
            .header("Accept-Language", "en-GB")
            .header("Authorization", "Bearer $accessToken")
            .header("User-Agent", USER_AGENT)
            .header("Cookie", "datadome=$dataDome")
            .build()
        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }
        parseDataDome(response)

        if (response.body == null) {
            throw Exception("Response body is empty.")
        }
        val responseString = response.body!!.string()
        Log.d(TAG, responseString)

        val responseBody = Json { this.ignoreUnknownKeys = true }.decodeFromString<ItemsResponseBody>(responseString)
        val items = responseBody.items.map { item ->
            TgtgItem(item.item.itemId.toInt(), item.displayName, item.itemsAvailable)
        }
        return items
    }
}