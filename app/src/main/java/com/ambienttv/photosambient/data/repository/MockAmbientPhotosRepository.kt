package com.ambienttv.photosambient.data.repository

import com.ambienttv.photosambient.data.model.AmbientDevice
import com.ambienttv.photosambient.data.model.AmbientMediaItem
import com.ambienttv.photosambient.data.model.AmbientOAuthState
import com.ambienttv.photosambient.data.model.AuthState
import com.ambienttv.photosambient.data.model.DeviceAuthorizationResponse
import com.ambienttv.photosambient.data.model.GoogleAccountInfo
import com.ambienttv.photosambient.data.model.MediaType
import com.ambienttv.photosambient.data.model.PollingConfig
import com.ambienttv.photosambient.data.model.VideoProcessingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Mock implementation of [AmbientPhotosRepository] for UX demonstration and review.
 *
 * Simulates the documented Google Photos Ambient API integration flow:
 * 1. OAuth 2.0 device code flow with UUID v4 requestId in `state`
 * 2. Scope: `https://www.googleapis.com/auth/photosambient.mediaitems`
 * 3. Ambient device creation returning output-only `settingsUri` and API device ID
 * 4. Polling for `mediaSourcesSet` according to `pollingConfig.pollInterval` (with demoPollInterval for fast review)
 * 5. Displaying mixed photo and video media queue with `videoProcessingStatus = READY`
 * 6. Deletion of device upon disconnect
 */
class MockAmbientPhotosRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : AmbientPhotosRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentDevice = MutableStateFlow<AmbientDevice?>(null)
    override val currentDevice: StateFlow<AmbientDevice?> = _currentDevice.asStateFlow()

    private val _accountInfo = MutableStateFlow<GoogleAccountInfo?>(null)
    override val accountInfo: StateFlow<GoogleAccountInfo?> = _accountInfo.asStateFlow()

    private val _mediaItems = MutableStateFlow<List<AmbientMediaItem>>(emptyList())
    override val mediaItems: StateFlow<List<AmbientMediaItem>> = _mediaItems.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private var activeDeviceName: String = "Living Room TV"
    private var activeOAuthState: AmbientOAuthState = AmbientOAuthState.create("Living Room TV")

    // Internal mock properties clearly labeled for demonstration mode
    val mockVerificationUrl: String = "https://www.google.com/device"
    val mockUserCode: String = "WDZX-9428"
    val mockSettingsUri: String = "mock://photosambient.googleapis.com/settings/device/demo_device_82914"

    // Shortened polling interval used solely for demonstration walkthrough speed
    val demoPollIntervalSeconds: Long = 5L

    // Curated mock media queue (Photo + Video mix with READY video status)
    private val sampleMediaQueue = listOf(
        AmbientMediaItem(
            id = "media_item_01",
            mediaType = MediaType.PHOTO,
            baseUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1920&q=80",
            mimeType = "image/jpeg",
            width = 1920,
            height = 1080,
            title = "Yosemite Valley Mist",
            albumTitle = "Family Vacation 2026",
            captureDate = "July 14, 2026",
            locationName = "Yosemite National Park, CA"
        ),
        AmbientMediaItem(
            id = "media_item_02",
            mediaType = MediaType.VIDEO,
            baseUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            videoProcessingStatus = VideoProcessingStatus.READY,
            mimeType = "video/mp4",
            width = 1920,
            height = 1080,
            durationSeconds = 15,
            title = "Sunset Waves along the Coast",
            albumTitle = "Recent Highlights",
            captureDate = "August 2, 2026",
            locationName = "Big Sur, California"
        ),
        AmbientMediaItem(
            id = "media_item_03",
            mediaType = MediaType.PHOTO,
            baseUrl = "https://images.unsplash.com/photo-1511884642898-4c92249e20b6?auto=format&fit=crop&w=1920&q=80",
            mimeType = "image/jpeg",
            width = 1920,
            height = 1080,
            title = "Alpine Lake at Sunrise",
            albumTitle = "Outdoor Adventures",
            captureDate = "June 28, 2026",
            locationName = "Banff National Park, Canada"
        ),
        AmbientMediaItem(
            id = "media_item_04",
            mediaType = MediaType.PHOTO,
            baseUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1920&q=80",
            mimeType = "image/jpeg",
            width = 1920,
            height = 1080,
            title = "Morning Fog in the Redwoods",
            albumTitle = "Family Highlights",
            captureDate = "May 19, 2026",
            locationName = "Muir Woods, California"
        ),
        AmbientMediaItem(
            id = "media_item_05",
            mediaType = MediaType.VIDEO,
            baseUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            videoProcessingStatus = VideoProcessingStatus.READY,
            mimeType = "video/mp4",
            width = 1920,
            height = 1080,
            durationSeconds = 15,
            title = "Waterfall Flow in Redwood Grove",
            albumTitle = "Recent Highlights",
            captureDate = "May 20, 2026",
            locationName = "Northern California"
        ),
        AmbientMediaItem(
            id = "media_item_06",
            mediaType = MediaType.PHOTO,
            baseUrl = "https://images.unsplash.com/photo-1426604966848-d7adac402bff?auto=format&fit=crop&w=1920&q=80",
            mimeType = "image/jpeg",
            width = 1920,
            height = 1080,
            title = "Autumn in the Cascades",
            albumTitle = "Nature & Travels",
            captureDate = "October 4, 2025",
            locationName = "Mount Rainier, WA"
        )
    )

    override suspend fun updateDeviceName(displayName: String) {
        activeDeviceName = displayName.ifBlank { "Living Room TV" }
        activeOAuthState = AmbientOAuthState.create(activeDeviceName)
        _currentDevice.value = _currentDevice.value?.copy(displayName = activeDeviceName)
    }

    override suspend fun initiateAuthorization(displayName: String): DeviceAuthorizationResponse {
        activeDeviceName = displayName.ifBlank { "Living Room TV" }
        activeOAuthState = AmbientOAuthState.create(activeDeviceName)

        val response = DeviceAuthorizationResponse(
            deviceCode = "mock_device_code_${System.currentTimeMillis() % 100000}",
            userCode = mockUserCode,
            verificationUrl = mockVerificationUrl,
            verificationUrlComplete = "$mockVerificationUrl?user_code=$mockUserCode",
            expiresInSeconds = 900,
            intervalSeconds = demoPollIntervalSeconds.toInt()
        )

        _authState.value = AuthState.WaitingForAuthorization(
            userCode = response.userCode,
            verificationUrl = "www.google.com/device",
            qrPayloadUri = response.verificationUrlComplete ?: response.verificationUrl,
            expiresInSeconds = response.expiresInSeconds
        )
        return response
    }

    override suspend fun completeAuthorization() {
        val deviceId = "demo_ambient_device_${System.currentTimeMillis() % 100000}"

        val newDevice = AmbientDevice(
            deviceId = deviceId,
            displayName = activeDeviceName,
            mediaSourcesSet = false,
            settingsUri = mockSettingsUri,
            pollingConfig = PollingConfig(pollInterval = "300s")
        )
        _currentDevice.value = newDevice
        _authState.value = AuthState.ConfiguringMediaSources(newDevice)
    }

    override suspend fun checkMediaSourcesSet(): Boolean {
        return _currentDevice.value?.mediaSourcesSet ?: false
    }

    override suspend fun simulateMediaSourcesConfigured() {
        val device = _currentDevice.value ?: AmbientDevice(
            deviceId = "demo_ambient_device_82914",
            displayName = activeDeviceName,
            mediaSourcesSet = false,
            settingsUri = mockSettingsUri,
            pollingConfig = PollingConfig(pollInterval = "300s")
        )
        val updatedDevice = device.copy(mediaSourcesSet = true)

        // Privacy-safe account details (realistic avatar, zero email/name exposure)
        val account = GoogleAccountInfo(
            isConnected = true,
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=160&q=80",
            avatarInitial = "G",
            accountBadge = "Connected to Google Photos",
            configuredSourcesDescription = "Family & Recent Highlights (64 items)"
        )

        _currentDevice.value = updatedDevice
        _accountInfo.value = account
        _mediaItems.value = sampleMediaQueue
        _authState.value = AuthState.Connected(account, updatedDevice)
    }

    override suspend fun refreshMediaItems(): List<AmbientMediaItem> {
        _mediaItems.value = sampleMediaQueue
        return sampleMediaQueue
    }

    override fun getSettingsUri(): String? {
        return _currentDevice.value?.settingsUri ?: mockSettingsUri
    }

    override suspend fun disconnectGooglePhotos(): Boolean {
        _currentDevice.value = null
        _accountInfo.value = null
        _mediaItems.value = emptyList()
        _authState.value = AuthState.Unauthenticated
        return true
    }

    override suspend fun resetToCleanInstall() {
        _currentDevice.value = null
        _accountInfo.value = null
        _mediaItems.value = emptyList()
        activeDeviceName = "Living Room TV"
        activeOAuthState = AmbientOAuthState.create("Living Room TV")
        _authState.value = AuthState.Unauthenticated
    }
}
