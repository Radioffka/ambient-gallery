package com.ambienttv.photosambient.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) {
                // Remote interaction triggers transient HUD
                controller.triggerTransientOverlay()
            }
    ) {
        val item = currentItem
        if (item != null) {
            Crossfade(
                targetState = item,
                animationSpec = tween(durationMillis = 800),
                label = "mediaCrossfade"
            ) { targetItem ->
                when (targetItem.mediaType) {
                    MediaType.PHOTO -> {
                        PhotoPlayerView(item = targetItem, accessToken = accessToken)
                    }
                    MediaType.VIDEO -> {
                        VideoPlayerView(
                            item = targetItem,
                            isPlaying = isPlaying,
                            accessToken = accessToken,
                            onVideoCompleted = { controller.onVideoCompleted() }
                        )
                    }
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

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerView(
    item: AmbientMediaItem,
    isPlaying: Boolean,
    accessToken: String?,
    onVideoCompleted: () -> Unit
) {
    val mediaUrl = item.playbackUrl() ?: item.baseUrl
    val context = LocalContext.current
    val exoPlayer = remember(item.id, accessToken) {
        val dataSource = DefaultHttpDataSource.Factory()
        if (item.useGoogleBaseUrl && !accessToken.isNullOrBlank()) {
            dataSource.setDefaultRequestProperties(mapOf("Authorization" to "Bearer $accessToken"))
        }
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource))
            .build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(mediaUrl)))
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
            playWhenReady = isPlaying
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onVideoCompleted()
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    onVideoCompleted()
                }
            })
        }
    }

    DisposableEffect(item.id, accessToken) {
        onDispose {
            exoPlayer.release()
        }
    }

    DisposableEffect(isPlaying) {
        exoPlayer.playWhenReady = isPlaying
        onDispose { }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

    }
}
