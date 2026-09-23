package com.ambienttv.photosambient.data.repository

import com.ambienttv.photosambient.data.model.AmbientDevice
import com.ambienttv.photosambient.data.model.AmbientMediaItem
import com.ambienttv.photosambient.data.model.AuthState
import com.ambienttv.photosambient.data.model.DeviceAuthorizationResponse
import com.ambienttv.photosambient.data.model.GoogleAccountInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface abstraction for Google Photos Ambient API interactions.
 * Decouples the UI and Slideshow presentation from whether real credentials
 * or mock demonstration services are being utilized.
 *
 * Official Scope: `https://www.googleapis.com/auth/photosambient.mediaitems`
 */
interface AmbientPhotosRepository {
    val authState: StateFlow<AuthState>
    val currentDevice: StateFlow<AmbientDevice?>
    val accountInfo: StateFlow<GoogleAccountInfo?>
    val mediaItems: StateFlow<List<AmbientMediaItem>>

    /**
     * Initiates the OAuth 2.0 Limited Input Device authorization flow.
     * Returns the [DeviceAuthorizationResponse] containing dynamic verification URI and user code.
     */
    suspend fun initiateAuthorization(displayName: String): DeviceAuthorizationResponse

    /**
     * Completes token exchange and registers the ambient device via `devices.create`.
     */
    suspend fun completeAuthorization()

    /**
     * Polls the Ambient API `devices.get` endpoint until `mediaSourcesSet == true`.
     */
    suspend fun checkMediaSourcesSet(): Boolean

    /**
     * Manually triggers simulated user completion of media source configuration on phone.
     */
    suspend fun simulateMediaSourcesConfigured()

    /**
     * Fetches ambient media items (both photos and videos) from `mediaItems.list`.
     */
    suspend fun refreshMediaItems(): List<AmbientMediaItem>

    /**
     * Returns the output-only `settingsUri` provided by the Ambient API response.
     */
    fun getSettingsUri(): String?

    /**
     * Disconnects the Google Photos ambient integration, deleting the AmbientDevice
     * association (`devices.delete`) and purging local credentials/cached queue.
     */
    suspend fun disconnectGooglePhotos(): Boolean

    /**
     * Sets the device display name.
     */
    suspend fun updateDeviceName(displayName: String)

    /**
     * Resets the repository to clean-install unauthenticated state for demo repeatability.
     */
    suspend fun resetToCleanInstall()

    companion object {
        const val OAUTH_SCOPE = "https://www.googleapis.com/auth/photosambient.mediaitems"
    }
}