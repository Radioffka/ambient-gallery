package com.ambienttv.photosambient.data.repository

import com.ambienttv.photosambient.data.model.AmbientDevice
import com.ambienttv.photosambient.data.model.AmbientMediaItem
import com.ambienttv.photosambient.data.model.AmbientOAuthState
import com.ambienttv.photosambient.data.model.AuthState
import com.ambienttv.photosambient.data.model.DeviceAuthorizationResponse
import com.ambienttv.photosambient.data.model.GoogleAccountInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Production skeleton implementation of [AmbientPhotosRepository] that will interact
 * with official Google endpoints when production access and credentials are provided.
 *
 * Production Flow:
 * 1. OAuth 2.0 TV device code request (`https://oauth2.googleapis.com/device/code`)
 *    with scope `https://www.googleapis.com/auth/photosambient.mediaitems`
 *    and streamlined `state` parameter containing [AmbientOAuthState.toStateParam]
 *    (UUID v4 requestId + optional displayName).
 * 2. Dynamically parse the returned `verification_url` (or `verification_url_complete`) and `user_code`.
 * 3. Poll for tokens (`https://oauth2.googleapis.com/token`) respecting returned `interval`.
 * 4. Call `POST https://photosambient.googleapis.com/v1/devices` to register AmbientDevice.
 *    Response provides Google-assigned `deviceId`, `settingsUri`, and `pollingConfig.pollInterval`.
 * 5. Display the output-only `settingsUri` as a QR code for mobile media source selection.
 * 6. Poll `GET https://photosambient.googleapis.com/v1/devices/{deviceId}` respecting `pollingConfig.pollInterval`
 *    until `mediaSourcesSet == true`.
 * 7. Call `GET https://photosambient.googleapis.com/v1/mediaItems` to retrieve selected photos & videos.
 *    Videos are streamed via `baseUrl=dv` once `videoProcessingStatus == READY`.
 * 8. On user disconnect: call `DELETE https://photosambient.googleapis.com/v1/devices/{deviceId}`
 *    and revoke OAuth access tokens.
 */
class GoogleAmbientPhotosRepository : AmbientPhotosRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentDevice = MutableStateFlow<AmbientDevice?>(null)
    override val currentDevice: StateFlow<AmbientDevice?> = _currentDevice.asStateFlow()

    private val _accountInfo = MutableStateFlow<GoogleAccountInfo?>(null)
    override val accountInfo: StateFlow<GoogleAccountInfo?> = _accountInfo.asStateFlow()

    private val _mediaItems = MutableStateFlow<List<AmbientMediaItem>>(emptyList())
    override val mediaItems: StateFlow<List<AmbientMediaItem>> = _mediaItems.asStateFlow()

    override suspend fun initiateAuthorization(displayName: String): DeviceAuthorizationResponse {
        val oAuthState = AmbientOAuthState.create(displayName)
        // Production implementation: POST https://oauth2.googleapis.com/device/code
        // with client_id, scope = OAUTH_SCOPE, and state = oAuthState.toStateParam()
        throw UnsupportedOperationException("Production credentials pending Google Photos Partner Program review approval.")
    }

    override suspend fun completeAuthorization() {
        // Exchange device code for OAuth tokens and call POST /v1/devices
        throw UnsupportedOperationException("Production credentials pending Google Photos Partner Program review approval.")
    }

    override suspend fun checkMediaSourcesSet(): Boolean {
        // GET /v1/devices/{deviceId}
        return false
    }

    override suspend fun simulateMediaSourcesConfigured() {
        // Handled via real API polling in production
    }

    override suspend fun refreshMediaItems(): List<AmbientMediaItem> {
        // GET /v1/mediaItems:list
        return emptyList()
    }

    override fun getSettingsUri(): String? {
        return _currentDevice.value?.settingsUri
    }

    override suspend fun disconnectGooglePhotos(): Boolean {
        // DELETE /v1/devices/{deviceId}
        return true
    }

    override suspend fun updateDeviceName(displayName: String) {
        // PATCH /v1/devices/{deviceId}
    }

    override suspend fun resetToCleanInstall() {
        _authState.value = AuthState.Unauthenticated
    }
}