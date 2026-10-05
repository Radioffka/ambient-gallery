package com.ambienttv.photosambient.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.view.KeyEvent as AndroidKeyEvent
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import okhttp3.Headers
import com.ambienttv.photosambient.data.model.AmbientMediaItem
import com.ambienttv.photosambient.data.model.MediaType
import com.ambienttv.photosambient.slideshow.AmbientSlideshowController
import com.ambienttv.photosambient.ui.components.TransientMediaOverlay
import com.ambienttv.photosambient.ui.theme.GoogleBlue
import kotlinx.coroutines.delay

/**
 * Screens H & I: Fullscreen Ambient Slideshow (Photos & Videos)
 *
 * Requirements:
 * - Media dominates the 1920x1080 screen
 * - NO persistent Google Photos branding during playback
 * - Seamless transition across photos and videos
 * - Media3 / ExoPlayer integration for video playback with aspect ratio preservation
 * - Remote D-pad interaction triggers transient HUD overlay with "From Google Photos" attribution
 */
@OptIn(UnstableApi::class)
@Composable
fun AmbientSlideshowScreen(
    controller: AmbientSlideshowController,
    accessToken: String? = null,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentItem by controller.currentMediaItem.collectAsState()
    val isPlaying by controller.isPlaying.collectAsState()
    val showOverlay by controller.showOverlay.collectAsState()
    val rootFocus = remember { FocusRequester() }
    val context = LocalContext.current
    val dataSource = remember { DefaultHttpDataSource.Factory() }
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource))
            .build().apply { repeatMode = Player.REPEAT_MODE_OFF }
    }
    val activeItem by rememberUpdatedState(currentItem)
    var playbackState by remember { mutableIntStateOf(Player.STATE_IDLE) }
    val videoItem = currentItem?.takeIf { it.mediaType == MediaType.VIDEO }

    DisposableEffect(exoPlayer, controller) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
                val item = activeItem
                if (state == Player.STATE_ENDED && item?.mediaType == MediaType.VIDEO &&
                    exoPlayer.currentMediaItem?.mediaId == item.id) {
                    controller.onVideoCompleted()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val item = activeItem
                if (item?.mediaType == MediaType.VIDEO &&
                    exoPlayer.currentMediaItem?.mediaId == item.id) {
                    controller.onVideoFailed(item.id)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(videoItem?.id, videoItem?.playbackUrl(), accessToken) {
        if (videoItem == null) {
            exoPlayer.playWhenReady = false
            // Keep the last frame behind the outgoing video-to-photo fade.
            delay(800)
            exoPlayer.clearMediaItems()
        } else {
            dataSource.setDefaultRequestProperties(
                if (videoItem.useGoogleBaseUrl && !accessToken.isNullOrBlank())
                    mapOf("Authorization" to "Bearer $accessToken") else emptyMap()
            )
            val mediaUrl = videoItem.playbackUrl() ?: videoItem.baseUrl
            exoPlayer.setMediaItem(MediaItem.Builder().setUri(Uri.parse(mediaUrl)).setMediaId(videoItem.id).build())
            exoPlayer.prepare()
            exoPlayer.playWhenReady = isPlaying
        }
    }

    LaunchedEffect(isPlaying, videoItem?.id) {
        exoPlayer.playWhenReady = isPlaying && videoItem != null
    }

    // An error callback does not cover a player that buffers indefinitely.
    LaunchedEffect(videoItem?.id, playbackState) {
        val item = videoItem ?: return@LaunchedEffect
        if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) return@LaunchedEffect
        kotlinx.coroutines.delay(30_000)
        if (activeItem?.id == item.id && exoPlayer.currentMediaItem?.mediaId == item.id &&
            exoPlayer.playbackState != Player.STATE_READY) {
            controller.onVideoFailed(item.id)
        }
    }

    LaunchedEffect(showOverlay) {
        if (!showOverlay) rootFocus.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(rootFocus)
            .onPreviewKeyEvent { event ->
                if (!showOverlay && event.type == KeyEventType.KeyDown &&
                    event.nativeKeyEvent.keyCode in listOf(
                        AndroidKeyEvent.KEYCODE_DPAD_UP,
                        AndroidKeyEvent.KEYCODE_DPAD_DOWN,
                        AndroidKeyEvent.KEYCODE_DPAD_LEFT,
                        AndroidKeyEvent.KEYCODE_DPAD_RIGHT,
                        AndroidKeyEvent.KEYCODE_DPAD_CENTER,
                        AndroidKeyEvent.KEYCODE_ENTER
                    )
                ) {
                    controller.triggerTransientOverlay()
                    true
                } else false
            }
            .focusable()
    ) {
        val item = currentItem
        if (item != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        setKeepContentOnPlayerReset(true)
                        isFocusable = false
                        isFocusableInTouchMode = false
                        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            Crossfade(
                targetState = item,
                animationSpec = tween(durationMillis = 800),
                label = "mediaCrossfade"
            ) { targetItem ->
                when (targetItem.mediaType) {
                    MediaType.PHOTO -> {
                        PhotoPlayerView(item = targetItem, accessToken = accessToken)
                    }
                    MediaType.VIDEO -> Box(Modifier.fillMaxSize())
                }
            }
        } else {
            // Loading media queue
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoogleBlue, strokeWidth = 3.dp)
            }
        }

        // On-demand Transient Media Overlay (Fades out automatically after 3.5s)
        TransientMediaOverlay(
            visible = showOverlay,
            mediaItem = currentItem,
            isPlaying = isPlaying,
            onOpenSettings = onOpenSettings,
            onTogglePlayPause = { controller.togglePlayPause() },
            onNext = { controller.nextItem() },
            onPrevious = { controller.previousItem() }
        )
    }
}

@Composable
private fun PhotoPlayerView(item: AmbientMediaItem, accessToken: String?) {
    val mediaUrl = item.playbackUrl() ?: item.baseUrl
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).apply {
                data(mediaUrl)
                if (item.useGoogleBaseUrl && !accessToken.isNullOrBlank()) {
                    headers(Headers.Builder().add("Authorization", "Bearer $accessToken").build())
                }
                crossfade(true)
            }.build(),
            contentDescription = item.title.ifBlank { item.albumTitle },
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
