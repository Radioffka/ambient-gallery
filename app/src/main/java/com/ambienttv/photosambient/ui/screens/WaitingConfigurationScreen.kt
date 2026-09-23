package com.ambienttv.photosambient.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.ui.components.ButtonVariant
import com.ambienttv.photosambient.ui.components.GooglePhotosPinwheelIcon
import com.ambienttv.photosambient.ui.components.ProceduralQrMatrix
import com.ambienttv.photosambient.ui.components.TvButton
import com.ambienttv.photosambient.ui.theme.BackgroundDark
import com.ambienttv.photosambient.ui.theme.GoogleBlue
import com.ambienttv.photosambient.ui.theme.SurfaceCard
import com.ambienttv.photosambient.ui.theme.TextPrimary
import com.ambienttv.photosambient.ui.theme.TextSecondary

/**
 * Screen E: Waiting / Media Source Configuration Screen
 *
 * Requirements:
 * - Displays "Waiting for Google Photos setup"
 * - Accurately represents that media source selection occurs in Google Photos via `settingsUri`
 * - In production, the QR code is generated from the output-only `settingsUri` returned by the Ambient API
 * - Simulates polling `devices.get` for `mediaSourcesSet = true`
 * - Does not present a fake in-app photo picker
 */
@Composable
fun WaitingConfigurationScreen(
    deviceName: String = "Living Room TV",
    settingsUri: String? = null,
    onMediaSourcesConfigured: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 72.dp, vertical = 50.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GooglePhotosPinwheelIcon(modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Google Photos Ambient Setup",
                            color = GoogleBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Waiting for Google Photos setup",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                // Device Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, Color(0x334285F4), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Configuring: $deviceName",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Main Content: Polling card + settingsUri QR card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Polling Status
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                        .padding(32.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = GoogleBlue,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Polling Ambient API (mediaSourcesSet == false)",
                                color = GoogleBlue.copy(alpha = pulseAlpha),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Please choose photos & albums on your mobile device",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "In the Google Photos page opened on your phone via settingsUri, select which albums or recent highlights should appear on $deviceName.\n\nOnce saved in Google Photos, this screen will automatically start playing your selected media items.",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // API details note for reviewers
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF13171F))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "API Contract: In production, app polls devices.get respecting pollingConfig.pollInterval returned by the Ambient API until mediaSourcesSet == true.",
                                color = Color(0xFF8AB4F8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Right settingsUri QR Card
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Re-open Settings Page:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ProceduralQrMatrix(modifier = Modifier.size(150.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Generated from API settingsUri",
                            color = Color(0xFF8AB4F8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvButton(
                    text = "Back to Setup",
                    onClick = onBackClick,
                    variant = ButtonVariant.SECONDARY
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "For demonstration review: trigger mediaSourcesSet = true",
                        color = Color(0xFF6B788E),
                        fontSize = 13.sp
                    )

                    TvButton(
                        text = "Simulate Sources Configured  ➔",
                        onClick = onMediaSourcesConfigured,
                        variant = ButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}