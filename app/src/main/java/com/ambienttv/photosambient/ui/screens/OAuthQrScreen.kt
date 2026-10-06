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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.ui.components.ButtonVariant
import com.ambienttv.photosambient.ui.components.GooglePhotosPinwheelIcon
import com.ambienttv.photosambient.ui.components.QrCodeCard
import com.ambienttv.photosambient.ui.components.TvButton
import com.ambienttv.photosambient.ui.components.rememberInitialFocusRequester
import com.ambienttv.photosambient.ui.theme.BackgroundDark
import com.ambienttv.photosambient.ui.theme.GoogleBlue
import com.ambienttv.photosambient.ui.theme.SurfaceCard
import com.ambienttv.photosambient.ui.theme.TextPrimary
import com.ambienttv.photosambient.ui.theme.TextSecondary

/**
 * Screen D: Connect / QR Authentication Screen
 *
 * Requirements:
 * - Implements OAuth 2.0 for TVs and Limited Input Devices
 * - Displays official Google OAuth verification endpoint (e.g. www.google.com/device)
 * - Clear 4-step mobile sign-in instructions
 * - High-contrast QR code with user code
 * - References streamlined Ambient API state parameter (UUID v4 requestId)
 */
@Composable
fun OAuthQrScreen(
    userCode: String,
    verificationUrl: String,
    deviceName: String = "Living Room TV",
    onBackClick: () -> Unit,
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
                            text = "Connect Google Photos",
                            color = GoogleBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan QR code to authorize on mobile",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                // Target TV Name Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, Color(0x334285F4), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Device: $deviceName",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Middle: Instructions + QR Code Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step-by-Step Instructions
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AuthStepItem(
                        stepNumber = "1",
                        title = "Scan QR code with your phone camera",
                        description = "Or visit $verificationUrl on your phone or computer."
                    )
                    AuthStepItem(
                        stepNumber = "2",
                        title = "Sign in to your Google Account",
                        description = "Use the account that contains the photos and videos you want to view."
                    )
                    AuthStepItem(
                        stepNumber = "3",
                        title = "Allow Google Photos Ambient Access",
                        description = "Permits read-only streaming of content selected for this device."
                    )
                    AuthStepItem(
                        stepNumber = "4",
                        title = "Select media sources for $deviceName",
                        description = "Choose albums or recent highlights in Google Photos to appear on TV."
                    )
                }

                // QR Code Display Card
                QrCodeCard(
                    userCode = userCode,
                    verificationUrl = verificationUrl,
                    modifier = Modifier.weight(0.8f)
                )
            }

            // Authorization advances automatically when Google returns a token.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvButton(
                    text = "Back",
                    onClick = onBackClick,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.focusRequester(primaryFocus)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Waiting for mobile authorization...",
                        color = Color(0xFF6B788E),
                        fontSize = 13.sp
                    )

                }
            }
        }
    }
}

@Composable
private fun AuthStepItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E283A))
                .border(1.dp, GoogleBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = GoogleBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
