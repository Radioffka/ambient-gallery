package com.ambienttv.photosambient.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmbientMediaItemTest {
    @Test
    fun googlePhotoAndVideoUseDocumentedBaseUrlSuffixes() {
        val base = "https://lh3.googleusercontent.com/p/example"
        val photo = AmbientMediaItem("photo", MediaType.PHOTO, base, useGoogleBaseUrl = true)
        val video = AmbientMediaItem("video", MediaType.VIDEO, base, useGoogleBaseUrl = true)

        assertEquals("$base=w1920-h1080", photo.playbackUrl())
        assertEquals("$base=dv", video.playbackUrl())
    }

    @Test
    fun demoUrlRemainsUntouchedAndUnreadyVideoHasNoPlaybackUrl() {
        val url = "https://example.test/video.mp4?source=demo"
        assertEquals(url, AmbientMediaItem("demo", MediaType.VIDEO, url).playbackUrl())
        assertNull(AmbientMediaItem(
            "pending", MediaType.VIDEO, url,
            videoProcessingStatus = VideoProcessingStatus.PROCESSING
        ).playbackUrl())
    }
}
