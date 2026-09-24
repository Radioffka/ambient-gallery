package com.ambienttv.photosambient.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.ui.components.GooglePhotosButton
import com.ambienttv.photosambient.ui.components.GooglePhotosPinwheelIcon
import com.ambienttv.photosambient.ui.components.rememberInitialFocusRequester
import com.ambienttv.photosambient.ui.theme.BackgroundDark
import com.ambienttv.photosambient.ui.theme.GoogleBlue
import com.ambienttv.photosambient.ui.theme.SurfaceCard
import com.ambienttv.photosambient.ui.theme.TextPrimary
import com.ambienttv.photosambient.ui.theme.TextSecondary

/**
 * Screen A: Welcome Screen
 *
 * Requirements:
 * - Clear, concise value proposition: "Display your selected photos and videos from Google Photos on this TV."
 * - Compliant Google Photos Connect Action Button
 * - Does not use "Google Photos" as the app's own product name
 */
@Composable
fun WelcomeScreen(
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryFocus = rememberInitialFocusRequester()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 72.dp, vertical = 56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Hero Content
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .padding(end = 48.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Product Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E2430))
                        .border(1.dp, Color(0x334285F4), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Ambient Gallery",
                        color = GoogleBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Display your selected photos and videos from Google Photos on this TV.",
                    color = TextPrimary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Turn your television into a dynamic ambient gallery. Choose your favorite albums and recent highlights in Google Photos, and enjoy seamless photo and video playback when your TV is idle.",
                    color = TextSecondary,
                    fontSize = 18.sp,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Compliant Google Photos Button
                GooglePhotosButton(
                    text = "Connect to Google Photos",
                    onClick = onConnectClick,
                    modifier = Modifier.focusRequester(primaryFocus)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Requires a Google Account with Google Photos. You control which media appears.",
                    color = Color(0xFF7A8699),
                    fontSize = 13.sp
                )
            }

            // Right Hero Visual (Simulated TV Frame with Photo & Video Ambient preview)
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(380.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
                    .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Preview Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GooglePhotosPinwheelIcon(modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Photos Ambient Mode",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoogleBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PHOTOS + VIDEOS",
                                color = GoogleBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Feature highlights inside card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF13171F))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        FeatureRow("📸", "Curated Photo Albums", "Display family portraits, trips, and memories")
                        FeatureRow("🎬", "Seamless Video Playback", "Watch short video clips alongside photos")
                        FeatureRow("🔒", "Privacy First", "Media stays in Google Photos; direct ambient stream")
                    }

                    // Preview Bottom status
                    Text(
                        text = "Press [SELECT] on remote to start setup",
                        color = Color(0xFF6B788E),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
        }
    }
}
