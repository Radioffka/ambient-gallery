package com.ambienttv.photosambient.data.model

/**
 * Represents the response from Google's OAuth 2.0 Device Authorization endpoint
 * (`https://oauth2.googleapis.com/device/code`).
 *
 * In accordance with Google OAuth 2.0 for TVs and Limited Input Devices:
 * - The `verificationUrl` is parsed dynamically from the API response (e.g. `https://www.google.com/device`).
 * - The `userCode` is presented to the user on the TV screen.
 * - The client polls `https://oauth2.googleapis.com/token` every `intervalSeconds`.
 *
 * @property deviceCode The device verification code used for token polling.
 * @property userCode The short code displayed on the TV for the user to enter on their mobile/browser.
 * @property verificationUrl The verification URL returned dynamically by Google's OAuth endpoint.
 * @property verificationUrlComplete Optional URL returned by Google containing the pre-filled user code.
 * @property expiresInSeconds The lifetime in seconds of the `deviceCode` and `userCode`.
 * @property intervalSeconds Minimum interval in seconds between token polling requests.
 */
data class DeviceAuthorizationResponse(
    val deviceCode: String,
    val userCode: String,
    val verificationUrl: String,
    val verificationUrlComplete: String? = null,
    val expiresInSeconds: Int = 900,
    val intervalSeconds: Int = 5
)