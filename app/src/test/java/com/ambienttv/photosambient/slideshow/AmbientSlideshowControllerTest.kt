package com.ambienttv.photosambient.slideshow

import com.ambienttv.photosambient.data.model.AmbientMediaItem
import com.ambienttv.photosambient.data.model.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmbientSlideshowControllerTest {
    private fun item(id: String, type: MediaType) = AmbientMediaItem(
        id = id, mediaType = type, baseUrl = "https://example.test/$id"
    )

    @Test
    fun failedVideoIsRemovedAndPlaybackContinuesWithFollowingItem() {
        val controller = AmbientSlideshowController(CoroutineScope(Dispatchers.Unconfined + Job()))
        val first = item("first", MediaType.PHOTO)
        val broken = item("broken", MediaType.VIDEO)
        val following = item("following", MediaType.PHOTO)
        try {
            controller.setQueue(listOf(first, broken, following))
            controller.nextItem()
            controller.onVideoFailed(broken.id)

            assertEquals(listOf(first, following), controller.mediaQueue.value)
            assertEquals(following, controller.currentMediaItem.value)
            assertEquals(1, controller.currentIndex.value)
        } finally {
            controller.release()
        }
    }

    @Test
    fun failingTheOnlyItemLeavesAnEmptyQueueAndReleaseCancelsTimers() {
        val job = Job()
        val controller = AmbientSlideshowController(CoroutineScope(Dispatchers.Unconfined + job))
        val broken = item("broken", MediaType.VIDEO)
        controller.setQueue(listOf(broken))
        controller.onVideoFailed(broken.id)

        assertTrue(controller.mediaQueue.value.isEmpty())
        assertNull(controller.currentMediaItem.value)
        assertEquals(0, controller.currentIndex.value)
        controller.release()
        assertFalse(job.isActive)
    }
}
