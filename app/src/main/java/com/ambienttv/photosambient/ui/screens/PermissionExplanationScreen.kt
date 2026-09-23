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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.ui.components.ButtonVariant
import com.ambienttv.photosambient.ui.components.GooglePhotosPinwheelIcon
import com.ambienttv.photosambient.ui.components.TvButton
import com.ambienttv.photosambient.ui.theme.BackgroundDark
import com.ambienttv.photosambient.ui.theme.GoogleBlue
import com.ambienttv.photosambient.ui.theme.SurfaceCard
import com.ambienttv.photosambient.ui.theme.TextPrimary
import com.ambienttv.photosambient.ui.theme.TextSecondary

/**
 * Screen B: Privacy / Permission Explanation Screen
 *
 * Requirements:
 * - Transparent disclosure of why Google Photos access is needed before requesting scopes.
 * - Confirms media is read-only for ambient presentation and never copied to a 3rd-party cloud.
 * - No unrelated permissions requested.
 */
@Composable
fun PermissionExplanationScreen(
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 72.dp, vertical = 56.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GooglePhotosPinwheelIcon(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Google Photos Integration",
                        color = GoogleBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Why we ask for Google Photos access",
                    color = TextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "To bring your memories to your TV screen, Ambient Gallery uses the official Google Photos Ambient API.",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }

            // 3 Clear Permission Principle Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PermissionCard(
                    icon = "🖼️",
                    title = "Selected Content Only",
                    description = "Only media from specific albums or collections you choose in Google Photos will be accessible to this TV.",
                    modifier = Modifier.weight(1f)
                )
                PermissionCard(
                    icon = "🔒",
                    title = "No 3rd-Party Storage",
                    description = "Your photos and videos remain safely in your Google Photos library. They are streamed directly for ambient display.",
                    modifier = Modifier.weight(1f)
                )
                PermissionCard(
                    icon = "⚙️",
                    title = "Full User Control",
                    description = "You can update which albums are shown or disconnect Google Photos from this TV at any time in Settings.",
                    modifier = Modifier.weight(1f)
                )
            }

            // Actions Bottom Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvButton(
                    text = "Back",
                    onClick = onBackClick,
                    variant = ButtonVariant.SECONDARY
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Step 1 of 3: Permissions Explanation",
                        color = Color(0xFF6B788E),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 24.dp)
                    )
                    TvButton(
                        text = "Continue",
                        onClick = onContinueClick,
                        variant = ButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
            .padding(24.dp)
    ) {
        Column {
            Text(text = icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}