package at.faymann.tgtgscanner.network

import android.util.Log
import at.faymann.tgtgscanner.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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
import java.time.Duration
import java.time.LocalDateTime

private const val BASE_URL: String = "https://apptoogoodtogo.com/api/"
//const val BASE_URL: String = "https://httpbin.org/anything/"
private const val AUTH_BY_EMAIL_ENDPOINT = "auth/v3/authByEmail"
private const val AUTH_POLLING_ENDPOINT = "auth/v3/authByRequestPollingId"
private const val ITEM_ENDPOINT: String = "item/v8/"
private const val REFRESH_ENDPOINT: String = "auth/v3/token/refresh"
private const val MAX_POLLING_TRIES = 24    // 24 * POLLING_WAIT_TIME = 2 minutes
private const val POLLING_WAIT_TIME = 5     // in seconds
private const val USER_AGENT: String = "TGTG/24.1.12 Dalvik/2.1.0 (Linux; U; Android 9; Nexus 5 Build/M4B30Z)"
private const val DEVICE_TYPE = "ANDROID"

private const val TAG = "TgtgClient"

class TgtgClient (
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val client: OkHttpClient = OkHttpClient()
    private val jsonDeserializer = Json { this.ignoreUnknownKeys = true }

    private suspend fun refreshTokenIfNeeded() {
        val accessTokenTtl = userPreferencesRepository.userPreferences.first().accessTokenTtl
        if (accessTokenTtl == null || accessTokenTtl < LocalDateTime.now() - Duration.ofHours(1)) {
            refreshToken()
        }
    }

    private suspend fun refreshToken() {
        val userPreferences = userPreferencesRepository.userPreferences.first()
        val accessToken = userPreferences.accessToken
        val refreshToken = userPreferences.refreshToken
        val dataDome = userPreferences.dataDome

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
        val response = client.newCall(request).execute()

        // Make sure we save the tokens even if the coroutine is cancelled
        withContext(NonCancellable) {
            if (response.body == null) {
                throw Exception("Response body is empty.")
            }
            val responseString = response.body!!.string()
            Log.d(TAG, responseString)

            val refreshResponseBody = Json.decodeFromString<RefreshResponseBody>(responseString)
            userPreferencesRepository.updateRefreshToken(refreshResponseBody.refreshToken)

            val accessTokenTtl = LocalDateTime.now() + Duration.ofSeconds(refreshResponseBody.accessTokenTtlSeconds.toLong())
            userPreferencesRepository.updateAccessToken(refreshResponseBody.accessToken, accessTokenTtl)

            parseDataDome(response)
        }
    }

    private suspend fun parseDataDome(response: Response) {
        val responseCookies = Cookie.parseAll(response.request.url, response.headers)
        val dataDomeCookie = responseCookies.firstOrNull {
            it.name == "datadome"
        }
        if (dataDomeCookie == null) {
            Log.w(TAG, "Missing datadome cookie.")
            return
        }
        withContext(NonCancellable) {
            userPreferencesRepository.updateDataDome(dataDomeCookie.value)
        }
    }

    suspend fun login() {
        val userPreferences = userPreferencesRepository.userPreferences.first()
        if (userPreferences.accessToken.isNotEmpty() && userPreferences.refreshToken.isNotEmpty() && userPreferences.userId != 0) {
            // Already logged in, reset the access token to perform a fresh login
            return
        }

        Log.d(TAG,"Logging in ${userPreferences.userEmail}...")
        val data = AuthByEmailRequestBody(
            deviceType = DEVICE_TYPE,
            email = userPreferences.userEmail
        )
        val json = Json.encodeToString(data)
        val body = json.toRequestBody("application/json".toMediaType())
        val request: Request = Request.Builder()
            .url(BASE_URL + AUTH_BY_EMAIL_ENDPOINT)
            .post(body)
            .header("Accept", "application/json")
            .header("Accept-Language", "en-GB")
            .header("User-Agent", USER_AGENT)
            .build()
        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }
        parseDataDome(response)

        if (response.code != 200) {
            throw Exception("Unexpected HTTP status code ${response.code}.")
        }
        val responseString = response.body!!.string()
        Log.d(TAG, responseString)

        val responseData = jsonDeserializer.decodeFromString<AuthByEmailResponseBody>(responseString)
        if (responseData.state == "TERMS") {
            throw Exception("The email ${userPreferences.userEmail} is not linked to a TGTG account. Please signup with this email first.")
        }
        if (responseData.state != "WAIT") {
            throw Exception("Login error. Status code: ${response.code}. Content: $responseString")
        }

        poll(responseData.pollingId)
    }

    private suspend fun poll(pollingId: String) {
        val userPreferences = userPreferencesRepository.userPreferences.first()
        for (index in 1..MAX_POLLING_TRIES) {
            Log.d(TAG, "Polling...")
            val requestData = AuthPollingRequestBody(
                email = userPreferences.userEmail,
                deviceType = DEVICE_TYPE,
                requestPollingId = pollingId,
            )
            val json = Json.encodeToString(requestData)
            val body = json.toRequestBody("application/json".toMediaType())
            val request: Request = Request.Builder()
                .url(BASE_URL + AUTH_POLLING_ENDPOINT)
                .post(body)
                .header("Accept", "application/json")
                .header("Accept-Language", "en-GB")
                .header("User-Agent", USER_AGENT)
                .header("Cookie", "datadome=${userPreferences.dataDome}")
                .build()
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            parseDataDome(response)

            if (response.code == 202) {
                delay(POLLING_WAIT_TIME * 1000L)
                continue
            }
            if (response.code != 200) {
                throw Exception("Unexpected HTTP status code ${response.code}.")
            }

            val responseString = response.body!!.string()
            Log.d(TAG, responseString)

            withContext(NonCancellable) {
                val responseData = jsonDeserializer.decodeFromString<AuthPollingResponseBody>(responseString)
                val accessTokenTtl = LocalDateTime.now() + Duration.ofSeconds(responseData.accessTokenTtlSeconds.toLong())
                userPreferencesRepository.updateUserData(
                    responseData.startupData.user.userId,
                    responseData.accessToken,
                    accessTokenTtl,
                    responseData.refreshToken
                )
                Log.d(TAG,"Login successful.")
            }
            return

        }
        throw Exception("Max polling retries reached. Try again.")
    }

    suspend fun getItems() : List<TgtgItem> {
        refreshTokenIfNeeded()

        val userPreferences = userPreferencesRepository.userPreferences.first()
        val accessToken = userPreferences.accessToken
        val dataDome = userPreferences.dataDome

        val userId = userPreferences.userId
        // val userId = 95337812

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
        val response = client.newCall(request).execute()

        // Make sure we save the data-dome cookie event if the coroutine is cancelled
        withContext(NonCancellable) {
            parseDataDome(response)
        }

        if (response.body == null) {
            throw Exception("Response body is empty.")
        }
        val responseString = response.body!!.string()
        Log.d(TAG, responseString)

        val responseBody = jsonDeserializer.decodeFromString<ItemsResponseBody>(responseString)
        val items = responseBody.items.map { item ->
            TgtgItem(
                id = item.item.itemId.toInt(),
                name = item.displayName,
                itemsAvailable = item.itemsAvailable,
                coverPictureUrl = item.item.coverPicture.currentUrl,
                logoPictureUrl = item.item.logoPicture.currentUrl
            )
        }
        return items
    }
}