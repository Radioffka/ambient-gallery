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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ambienttv.photosambient.data.model.AmbientDevice
import com.ambienttv.photosambient.data.model.GoogleAccountInfo
import com.ambienttv.photosambient.ui.components.ButtonVariant
import com.ambienttv.photosambient.ui.components.GooglePhotosPinwheelIcon
import com.ambienttv.photosambient.ui.components.TvButton
import com.ambienttv.photosambient.ui.components.rememberInitialFocusRequester
import com.ambienttv.photosambient.ui.theme.BackgroundDark
import com.ambienttv.photosambient.ui.theme.GoogleGreen
import com.ambienttv.photosambient.ui.theme.SurfaceCard
import com.ambienttv.photosambient.ui.theme.TextPrimary
import com.ambienttv.photosambient.ui.theme.TextSecondary

/**
 * Screen F: Connected State & Settings Screen
 *
 * Requirements:
 * - Connected status clearly visible
 * - Privacy-safe account identification (Realistic Avatar only, NO email or personal names on TV)
 * - Device name and configured sources summary
 * - Clear action: "Change Google Photos selection" (re-opens settingsUri)
 * - Clear action: "Start Ambient Slideshow"
 * - Clear action: "Disconnect Google Photos" (easy to locate, not hidden)
 */
@Composable
fun SettingsScreen(
    accountInfo: GoogleAccountInfo,
    device: AmbientDevice,
    onStartSlideshow: () -> Unit,
    onChangeSelection: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryFocus = rememberInitialFocusRequester()
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
            // Top Header: Connected Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GooglePhotosPinwheelIcon(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Connected to Google Photos",
                            color = GoogleGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ambient Settings & Account",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                // Primary Start Slideshow CTA
                TvButton(
                    text = "▶  Start Ambient Slideshow",
                    onClick = onStartSlideshow,
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.focusRequester(primaryFocus)
                )
            }

            // Main Settings Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // Privacy-Safe Account Card (Realistic Avatar, Zero Email / Name exposure)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "GOOGLE PHOTOS ACCOUNT",
                            color = Color(0xFF8AB4F8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Privacy-safe realistic avatar + status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E283A))
                                    .border(2.dp, Color(0xFF4285F4), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!accountInfo.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(accountInfo.avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Google Account Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = accountInfo.avatarInitial.ifBlank { "G" },
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = accountInfo.accountBadge,
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Active Ambient Integration",
                                    color = GoogleGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "🔒 Public Display Privacy: User email addresses and personal names are omitted to protect privacy on shared TV screens.",
                            color = Color(0xFF7D8B9E),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Device & Selected Sources Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "DEVICE & MEDIA SOURCES",
                            color = Color(0xFF8AB4F8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoRow("Device Name", device.displayName)
                        Spacer(modifier = Modifier.height(10.dp))
                        InfoRow("Configured Sources", accountInfo.configuredSourcesDescription)
                        Spacer(modifier = Modifier.height(10.dp))
                        InfoRow("Ambient Status", "mediaSourcesSet == true")

                        Spacer(modifier = Modifier.height(18.dp))

                        TvButton(
                            text = "Change Google Photos selection",
                            onClick = onChangeSelection,
                            variant = ButtonVariant.SECONDARY
                        )
                    }
                }
            }

            // Bottom Actions Bar (With explicit Disconnect option)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Explicit Disconnect Action Button
                TvButton(
                    text = "Disconnect Google Photos",
                    onClick = onDisconnectClick,
                    variant = ButtonVariant.DANGER
                )

                Text(
                    text = "Ambient Gallery • Google Photos Ambient API Integration",
                    color = Color(0xFF5A667A),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
