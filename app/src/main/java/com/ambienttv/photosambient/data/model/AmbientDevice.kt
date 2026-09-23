package com.ambienttv.photosambient.data.model

/**
 * Polling configuration returned by the Google Photos Ambient API.
 *
 * @property pollInterval Duration string returned by the API (e.g. "300s") indicating
 * the recommended interval for polling device status via `devices.get`.
 */
data class PollingConfig(
    val pollInterval: String? = null
)

/**
 * Represents an Ambient Device registered with the Google Photos Ambient API.
 * Corresponds to the `AmbientDevice` resource from `photosambient.googleapis.com/v1/devices`.
 *
 * In production:
 * - `settingsUri` is an output-only URI returned by the Ambient API (`devices.create` or `devices.get`).
 * - `pollingConfig.pollInterval` defines the frequency to check `mediaSourcesSet`.
 *
 * @property deviceId The unique Google Photos identifier for this ambient device.
 * @property displayName User-assigned name for this TV device (e.g. "Living Room TV").
 * @property mediaSourcesSet True if the user has completed album/source selection in Google Photos.
 * @property settingsUri Output-only URI provided by Google Photos for the user to configure media sources.
 * @property pollingConfig Polling settings provided by the Ambient API.
 * @property createTime Timestamp when the device was registered.
 */
data class AmbientDevice(
    val deviceId: String,
    val displayName: String,
    val mediaSourcesSet: Boolean = false,
    val settingsUri: String? = null,
    val pollingConfig: PollingConfig? = null,
    val createTime: String? = null
)