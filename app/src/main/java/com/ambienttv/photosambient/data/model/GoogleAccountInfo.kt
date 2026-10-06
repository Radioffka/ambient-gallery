package com.ambienttv.photosambient.data.model

/**
 * Encapsulates privacy-safe Google Account details for Android TV presentation.
 *
 * Per Google Photos TV & Public Display UX Guidelines:
 * - Sensitive user email addresses (full or masked) and personal names are NOT displayed on TV screens.
 * - The UI only shows the Google Account profile avatar and the clear status "Connected to Google Photos".
 */
data class GoogleAccountInfo(
    val isConnected: Boolean = true,
    val avatarUrl: String? = null,
    val avatarInitial: String = "G",
    val accountBadge: String = "Connected to Google Photos",
    val configuredSourcesDescription: String = "Family & Recent Highlights (64 items)"
)
