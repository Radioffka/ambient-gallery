package com.ambienttv.photosambient.data.repository

import android.content.Context
import com.ambienttv.photosambient.data.model.AmbientDevice
import com.ambienttv.photosambient.data.model.AmbientMediaItem
import com.ambienttv.photosambient.data.model.AuthState
import com.ambienttv.photosambient.data.model.DeviceAuthorizationResponse
import com.ambienttv.photosambient.data.model.GoogleAccountInfo
import com.ambienttv.photosambient.data.model.MediaType
import com.ambienttv.photosambient.data.model.PollingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

/**
 * Small, direct implementation of the verified TV OAuth and Ambient API path.
 * The Google Photos account is chosen on Google's consent page, never inside this app.
 */
class GoogleAmbientPhotosRepository(
    context: Context,
    private val clientId: String,
    private val clientSecret: String
) : AmbientPhotosRepository {
    private val store = AmbientSessionStore(context.applicationContext)
    private val tokenMutex = Mutex()
    private var session: AmbientSession? = store.read()
    private var pendingCode: DeviceAuthorizationResponse? = null
    private var requestedDeviceName = "Living Room TV"

    private val _authState = MutableStateFlow<AuthState>(
        session?.let { AuthState.ConfiguringMediaSources(it.toDevice()) } ?: AuthState.Unauthenticated
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentDevice = MutableStateFlow(session?.toDevice())
    override val currentDevice: StateFlow<AmbientDevice?> = _currentDevice.asStateFlow()

    private val _accountInfo = MutableStateFlow<GoogleAccountInfo?>(null)
    override val accountInfo: StateFlow<GoogleAccountInfo?> = _accountInfo.asStateFlow()

    private val _mediaItems = MutableStateFlow<List<AmbientMediaItem>>(emptyList())
    override val mediaItems: StateFlow<List<AmbientMediaItem>> = _mediaItems.asStateFlow()

    private val _accessToken = MutableStateFlow(session?.accessToken)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    override suspend fun updateDeviceName(displayName: String) {
        requestedDeviceName = displayName.trim().ifBlank { "Living Room TV" }
    }

    override suspend fun initiateAuthorization(displayName: String): DeviceAuthorizationResponse {
        require(clientId.isNotBlank() && clientSecret.isNotBlank()) { "Ambient OAuth credentials are missing from this APK." }
        updateDeviceName(displayName)
        val result = request(
            "POST", "https://oauth2.googleapis.com/device/code",
            form = mapOf("client_id" to clientId, "scope" to AmbientPhotosRepository.OAUTH_SCOPE)
        )
        requireSuccess(result, "Device authorization")
        val body = result.body
        val response = DeviceAuthorizationResponse(
            deviceCode = body.getString("device_code"),
            userCode = body.getString("user_code"),
            verificationUrl = body.getString("verification_url"),
            verificationUrlComplete = body.optString("verification_url_complete").ifBlank { null },
            expiresInSeconds = body.getInt("expires_in"),
            intervalSeconds = body.optInt("interval", 5).coerceAtLeast(5)
        )
        pendingCode = response
        _authState.value = AuthState.WaitingForAuthorization(
            userCode = response.userCode,
            verificationUrl = response.verificationUrl,
            qrPayloadUri = response.verificationUrlComplete ?: response.verificationUrl,
            expiresInSeconds = response.expiresInSeconds
        )
        return response
    }

    override suspend fun completeAuthorization() {
        val code = pendingCode ?: error("Start device authorization again.")
        val deadline = System.currentTimeMillis() + code.expiresInSeconds * 1000L
        var waitSeconds = code.intervalSeconds.toLong()
        try {
            while (System.currentTimeMillis() < deadline) {
                delay(waitSeconds * 1000L)
                val result = request(
                    "POST", "https://oauth2.googleapis.com/token",
                    form = mapOf(
                        "client_id" to clientId,
                        "client_secret" to clientSecret,
                        "device_code" to code.deviceCode,
                        "grant_type" to "urn:ietf:params:oauth:grant-type:device_code"
                    )
                )
                if (result.status in 200..299) {
                    val token = result.body
                    val grantedScopes = token.optString("scope").split(' ')
                    check(AmbientPhotosRepository.OAUTH_SCOPE in grantedScopes) {
                        "Google did not grant the Ambient media scope."
                    }
                    val access = token.getString("access_token")
                    val refresh = token.getString("refresh_token")
                    val created = request(
                        "POST",
                        "$API_ROOT/devices?requestId=${UUID.randomUUID()}",
                        bearer = access,
                        json = JSONObject().put("displayName", requestedDeviceName)
                    )
                    requireSuccess(created, "Create Ambient device")
                    val device = parseDevice(created.body)
                    val saved = AmbientSession(
                        accessToken = access,
                        refreshToken = refresh,
                        expiresAtMillis = expiry(token),
                        deviceId = device.deviceId,
                        deviceName = device.displayName,
                        settingsUri = device.settingsUri
                    )
                    try {
                        store.write(saved)
                    } catch (storageError: Exception) {
                        try {
                            request("DELETE", "$API_ROOT/devices/${encode(device.deviceId)}", bearer = access)
                        } catch (_: Exception) {
                            // The device ID is not persisted if secure storage failed.
                        }
                        throw storageError
                    }
                    session = saved
                    _accessToken.value = access
                    _currentDevice.value = device
                    _authState.value = AuthState.ConfiguringMediaSources(device)
                    return
                }
                when (result.body.optString("error")) {
                    "authorization_pending" -> Unit
                    "slow_down" -> waitSeconds += 5
                    "access_denied" -> error("Google account access was denied.")
                    "expired_token" -> error("The Google device code expired. Start again.")
                    else -> throw failure(result, "Google token exchange")
                }
            }
            error("The Google device code expired. Start again.")
        } finally {
            pendingCode = null
        }
    }

    override suspend fun checkMediaSourcesSet(): Boolean {
        val active = session ?: return false
        val result = request(
            "GET", "$API_ROOT/devices/${encode(active.deviceId)}",
            bearer = validAccessToken()
        )
        if (result.status == 404) {
            forgetLocalSession()
            error("This Google Photos device no longer exists. Connect again.")
        }
        requireSuccess(result, "Check media selection")
        val device = parseDevice(result.body)
        _currentDevice.value = device
        if (device.settingsUri != active.settingsUri || device.displayName != active.deviceName) {
            val updated = active.copy(settingsUri = device.settingsUri, deviceName = device.displayName)
            session = updated
            store.write(updated)
        }
        if (device.mediaSourcesSet) {
            val sources = result.body.optJSONArray("mediaSources")
            val names = (0 until (sources?.length() ?: 0)).mapNotNull {
                sources?.optJSONObject(it)?.optString("displayName")?.takeIf(String::isNotBlank)
            }
            val account = GoogleAccountInfo(
                avatarUrl = null,
                configuredSourcesDescription = names.joinToString(", ").ifBlank { "Selected in Google Photos" }
            )
            _accountInfo.value = account
            _authState.value = AuthState.Connected(account, device)
        } else {
            _authState.value = AuthState.ConfiguringMediaSources(device)
        }
        return device.mediaSourcesSet
    }

    override suspend fun refreshMediaItems(): List<AmbientMediaItem> {
        val device = _currentDevice.value ?: error("No Ambient device has been created.")
        val access = validAccessToken()
        val items = LinkedHashMap<String, AmbientMediaItem>()
        var pageToken: String? = null
        // A page is up to 100 items. Bound each refresh to avoid exhausting the 240/day device quota.
        repeat(3) {
            val query = "$API_ROOT/mediaItems?deviceId=${encode(device.deviceId)}&pageSize=100" +
                (pageToken?.let { token -> "&pageToken=${encode(token)}" } ?: "")
            val result = request("GET", query, bearer = access)
            requireSuccess(result, "List Ambient media")
            val page = result.body.optJSONArray("mediaItems")
            for (index in 0 until (page?.length() ?: 0)) {
                val entry = page?.optJSONObject(index) ?: continue
                val media = entry.optJSONObject("mediaFile") ?: continue
                val mime = media.optString("mimeType")
                val type = when {
                    mime.startsWith("image/") -> MediaType.PHOTO
                    mime.startsWith("video/") -> MediaType.VIDEO
                    else -> continue
                }
                val id = entry.optString("id")
                val baseUrl = media.optString("baseUrl")
                if (id.isBlank() || baseUrl.isBlank()) continue
                val dimensions = media.optJSONObject("mediaFileMetadata")
                items[id] = AmbientMediaItem(
                    id = id,
                    mediaType = type,
                    baseUrl = baseUrl,
                    mimeType = mime,
                    width = dimensions?.optInt("width")?.takeIf { it > 0 } ?: 1920,
                    height = dimensions?.optInt("height")?.takeIf { it > 0 } ?: 1080,
                    title = "",
                    albumTitle = "",
                    captureDate = "",
                    locationName = null,
                    useGoogleBaseUrl = true
                )
            }
            pageToken = result.body.optString("nextPageToken").ifBlank { null }
            if (pageToken == null) {
                _mediaItems.value = items.values.toList()
                return _mediaItems.value
            }
        }
        _mediaItems.value = items.values.toList()
        return _mediaItems.value
    }

    override fun getSettingsUri(): String? = _currentDevice.value?.settingsUri

    override suspend fun simulateMediaSourcesConfigured() {
        error("Choose media in the Google Photos app using settingsUri.")
    }

    override suspend fun disconnectGooglePhotos(): Boolean {
        val active = session ?: return true
        return try {
            val access = validAccessToken()
            val deleted = request("DELETE", "$API_ROOT/devices/${encode(active.deviceId)}", bearer = access)
            if (deleted.status !in 200..299 && deleted.status != 404) return false
            // Revocation is best effort after deleting the device; local state is cleared either way.
            try {
                request(
                    "POST", "https://oauth2.googleapis.com/revoke",
                    form = mapOf("token" to active.refreshToken)
                )
            } catch (_: Exception) {
                // Google account settings can also revoke the authorization.
            }
            forgetLocalSession()
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun resetToCleanInstall() {
        check(disconnectGooglePhotos()) { "Could not delete the Google Photos device. Try disconnect again." }
    }

    private fun forgetLocalSession() {
        store.clear()
        session = null
        pendingCode = null
        _accessToken.value = null
        _currentDevice.value = null
        _accountInfo.value = null
        _mediaItems.value = emptyList()
        _authState.value = AuthState.Unauthenticated
    }

    private suspend fun validAccessToken(): String = tokenMutex.withLock {
        val active = session ?: error("Connect Google Photos again.")
        if (System.currentTimeMillis() < active.expiresAtMillis) return@withLock active.accessToken
        val result = request(
            "POST", "https://oauth2.googleapis.com/token",
            form = mapOf(
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "refresh_token" to active.refreshToken,
                "grant_type" to "refresh_token"
            )
        )
        if (result.status !in 200..299) {
            if (result.body.optString("error") == "invalid_grant") {
                forgetLocalSession()
                error("Google authorization expired or was revoked. Connect again.")
            }
            throw failure(result, "Refresh Google authorization")
        }
        val updated = active.copy(
            accessToken = result.body.getString("access_token"),
            refreshToken = result.body.optString("refresh_token").ifBlank { active.refreshToken },
            expiresAtMillis = expiry(result.body)
        )
        store.write(updated)
        session = updated
        _accessToken.value = updated.accessToken
        updated.accessToken
    }

    private fun expiry(token: JSONObject): Long =
        System.currentTimeMillis() + (token.optLong("expires_in", 3600) - 60).coerceAtLeast(60) * 1000L

    private fun parseDevice(body: JSONObject): AmbientDevice = AmbientDevice(
        deviceId = body.getString("id"),
        displayName = body.optString("displayName").ifBlank { requestedDeviceName },
        mediaSourcesSet = body.optBoolean("mediaSourcesSet"),
        settingsUri = body.optString("settingsUri").ifBlank { null },
        pollingConfig = PollingConfig(body.optJSONObject("pollingConfig")?.optString("pollInterval")),
        createTime = body.optString("createTime").ifBlank { null }
    )

    private fun AmbientSession.toDevice() = AmbientDevice(
        deviceId = deviceId,
        displayName = deviceName,
        settingsUri = settingsUri
    )

    private fun requireSuccess(result: HttpResult, operation: String) {
        if (result.status !in 200..299) throw failure(result, operation)
    }

    private fun failure(result: HttpResult, operation: String): IllegalStateException {
        val detail = result.body.optJSONObject("error")
        val reason = detail?.optString("status")
            ?: result.body.optString("error").ifBlank { "UNKNOWN" }
        val message = detail?.optString("message")
            ?: result.body.optString("error_description")
        return IllegalStateException("$operation: HTTP ${result.status} $reason. ${message.orEmpty().take(180)}")
    }

    private data class HttpResult(val status: Int, val body: JSONObject)

    private suspend fun request(
        method: String,
        url: String,
        bearer: String? = null,
        form: Map<String, String>? = null,
        json: JSONObject? = null
    ): HttpResult = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 20_000
            readTimeout = 30_000
            useCaches = false
            setRequestProperty("Accept", "application/json")
            if (bearer != null) setRequestProperty("Authorization", "Bearer $bearer")
        }
        try {
            val payload = when {
                form != null -> form.entries.joinToString("&") { (key, value) -> "${encode(key)}=${encode(value)}" }
                json != null -> json.toString()
                else -> null
            }
            if (payload != null) {
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    if (form != null) "application/x-www-form-urlencoded" else "application/json"
                )
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            HttpResult(status, if (text.isBlank()) JSONObject() else JSONObject(text))
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String) = URLEncoder.encode(value, "UTF-8")

    companion object {
        private const val API_ROOT = "https://photosambient.googleapis.com/v1"
    }
}
