package com.ambienttv.photosambient.slideshow

import com.ambienttv.photosambient.data.model.AmbientMediaItem
import com.ambienttv.photosambient.data.model.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Observable controller for the ambient Photo and Video slideshow.
 * Manages queue state, slide transitions, playback toggle, and transient HUD timer.
 */
class AmbientSlideshowController(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _mediaQueue = MutableStateFlow<List<AmbientMediaItem>>(emptyList())
    val mediaQueue: StateFlow<List<AmbientMediaItem>> = _mediaQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _currentMediaItem = MutableStateFlow<AmbientMediaItem?>(null)
    val currentMediaItem: StateFlow<AmbientMediaItem?> = _currentMediaItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _showOverlay = MutableStateFlow(false)
    val showOverlay: StateFlow<Boolean> = _showOverlay.asStateFlow()

    private var slideshowJob: Job? = null
    private var overlayDismissJob: Job? = null

    fun release() {
        scope.cancel()
    }

    fun setQueue(items: List<AmbientMediaItem>) {
        _mediaQueue.value = items
        _currentIndex.value = 0
        _currentMediaItem.value = items.firstOrNull()
        restartTimer()
    }

    fun nextItem() {
        val list = _mediaQueue.value
        if (list.isNotEmpty()) {
            val nextIdx = (_currentIndex.value + 1) % list.size
            _currentIndex.value = nextIdx
            _currentMediaItem.value = list[nextIdx]
            restartTimer()
        }
    }

    fun previousItem() {
        val list = _mediaQueue.value
        if (list.isNotEmpty()) {
            val prevIdx = if (_currentIndex.value - 1 < 0) list.size - 1 else _currentIndex.value - 1
            _currentIndex.value = prevIdx
            _currentMediaItem.value = list[prevIdx]
            restartTimer()
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
        triggerTransientOverlay()
        if (_isPlaying.value) {
            restartTimer()
        } else {
            slideshowJob?.cancel()
        }
    }

    fun triggerTransientOverlay() {
        _showOverlay.value = true
        overlayDismissJob?.cancel()
        overlayDismissJob = scope.launch {
            delay(3500)
            _showOverlay.value = false
        }
    }

    fun onVideoCompleted() {
        // Called when ExoPlayer / Media3 video reaches playback completion
        if (_isPlaying.value) {
            nextItem()
        }
    }

    fun onVideoFailed(id: String) {
        val failedIndex = _mediaQueue.value.indexOfFirst { it.id == id }
        if (failedIndex < 0) return
        val currentId = _currentMediaItem.value?.id
        val remaining = _mediaQueue.value.filterNot { it.id == id }
        val nextIndex = if (currentId == id) failedIndex % remaining.size.coerceAtLeast(1)
            else remaining.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        _mediaQueue.value = remaining
        _currentIndex.value = nextIndex
        _currentMediaItem.value = remaining.getOrNull(nextIndex)
        restartTimer()
    }

    private fun restartTimer() {
        slideshowJob?.cancel()
        val item = _currentMediaItem.value ?: return
        if (!_isPlaying.value) return

        slideshowJob = scope.launch {
            if (item.mediaType == MediaType.PHOTO) {
                // Photo display duration: 8 seconds
                delay(8000)
                nextItem()
            }
            // For VIDEO, wait for playback completion or fallback timeout
        }
    }
}
