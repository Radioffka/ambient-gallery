package com.ambienttv.photosambient.data.model

enum class MediaType {
    PHOTO,
    VIDEO
}

/**
 * Status of video processing in Google Photos.
 * Video media items can only be streamed or played once processing has completed.
 */
enum class VideoProcessingStatus {
    UNSPECIFIED,
    PROCESSING,
    READY,
    FAILED
}

/**
 * Represents an ambient media item returned by the Google Photos Ambient API
 * endpoint `photosambient.googleapis.com/v1/mediaItems:list`.
 *
 * @property id Unique media item identifier.
 * @property mediaType Type of media (PHOTO or VIDEO).
 * @property baseUrl Direct base URL provided by Google Photos.
 * @property videoProcessingStatus Current video processing status (only applicable when mediaType == VIDEO).
 * @property mimeType MIME type (e.g. "image/jpeg", "video/mp4").
 * @property width Pixel width.
 * @property height Pixel height.
 * @property durationSeconds Duration in seconds if mediaType == VIDEO.
 * @property title Optional title or caption.
 * @property albumTitle Title of the Google Photos album or collection this item belongs to.
 * @property captureDate Formatted capture date string.
 * @property locationName Optional location description.
 */
data class AmbientMediaItem(
    val id: String,
    val mediaType: MediaType,
    val baseUrl: String,
    val videoProcessingStatus: VideoProcessingStatus = VideoProcessingStatus.READY,
    val mimeType: String = "image/jpeg",
    val width: Int = 1920,
    val height: Int = 1080,
    val durationSeconds: Int? = null,
    val title: String = "",
    val albumTitle: String = "Family Highlights",
    val captureDate: String = "August 2026",
    val locationName: String? = null
) {
    /**
     * Constructs the compliant playback URL for the media item.
     *
     * In accordance with Google Photos Ambient API documentation:
     * - Photos use the standard `baseUrl` (with optional dimension parameters).
     * - Videos must only be played when [videoProcessingStatus] is [VideoProcessingStatus.READY],
     *   requesting the transcoded stream via the documented `=dv` parameter.
     */
    fun playbackUrl(): String? {
        return when (mediaType) {
            MediaType.PHOTO -> baseUrl
            MediaType.VIDEO -> {
                if (videoProcessingStatus == VideoProcessingStatus.READY) {
                    "$baseUrl=dv"
                } else {
                    null // Video is still processing or failed in Google Photos
                }
            }
        }
    }
}