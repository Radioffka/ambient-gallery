package com.ambienttv.photosambient.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.data.model.AmbientMediaItem
import com.ambienttv.photosambient.data.model.MediaType
import com.ambienttv.photosambient.ui.theme.GoogleBlue
import com.ambienttv.photosambient.ui.theme.TextPrimary
import com.ambienttv.photosambient.ui.theme.TextSecondary

/**
 * Transient on-demand media info overlay for Ambient mode.
 * Conforms strictly to Google Photos Ambient UX Requirements:
 * - NOT permanently visible during playback
 * - Fades in upon user D-pad remote interaction
 * - Displays official attribution: "From Google Photos"
 * - Displays album name, capture date, location, and playback status
 */
@Composable
fun TransientMediaOverlay(
    visible: Boolean,
    mediaItem: AmbientMediaItem?,
    isPlaying: Boolean,
    onOpenSettings: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryFocus = remember { FocusRequester() }
    LaunchedEffect(visible, mediaItem?.id) {
        if (visible && mediaItem != null) primaryFocus.requestFocus()
    }
    AnimatedVisibility(
        visible = visible && mediaItem != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 }),
        modifier = modifier.fillMaxSize()
    ) {
        if (mediaItem == null) return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x66000000),
                            Color(0xCC080A0E)
                        ),
                        startY = 400f
                    )
                )
                .padding(horizontal = 56.dp, vertical = 40.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Media metadata & Google Photos attribution
                Column(modifier = Modifier.weight(1f)) {
                    // Google Photos attribution pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x801E222B))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        GooglePhotosPinwheelIcon(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "From Google Photos",
                            color = Color(0xFFE8EAED),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (mediaItem.mediaType == MediaType.VIDEO) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoogleBlue)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "VIDEO",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (mediaItem.title.isNotBlank()) mediaItem.title else mediaItem.albumTitle,
                        color = TextPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mediaItem.albumTitle,
                            color = Color(0xFF8AB4F8),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "  •  ${mediaItem.captureDate}",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                        mediaItem.locationName?.let { loc ->
                            Text(
                                text = "  •  $loc",
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Quick D-pad control prompts
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TvButton(
                        text = if (isPlaying) "Pause" else "Play",
                        onClick = onTogglePlayPause,
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier.focusRequester(primaryFocus)
                    )
                    TvButton(
                        text = "Next",
                        onClick = onNext,
                        variant = ButtonVariant.SECONDARY
                    )
                    TvButton(
                        text = "Settings",
                        onClick = onOpenSettings,
                        variant = ButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}
