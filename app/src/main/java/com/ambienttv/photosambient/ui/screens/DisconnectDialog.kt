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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.ui.components.ButtonVariant
import com.ambienttv.photosambient.ui.components.GooglePhotosPinwheelIcon
import com.ambienttv.photosambient.ui.components.TvButton
import com.ambienttv.photosambient.ui.components.rememberInitialFocusRequester
import com.ambienttv.photosambient.ui.theme.BackgroundDark
import com.ambienttv.photosambient.ui.theme.DangerRed
import com.ambienttv.photosambient.ui.theme.SurfaceCard
import com.ambienttv.photosambient.ui.theme.TextPrimary
import com.ambienttv.photosambient.ui.theme.TextSecondary

/**
 * Screen G: Disconnect Confirmation Modal
 *
 * Requirements:
 * - Clear confirmation dialog explaining that:
 *   1. Google Photos will be disconnected from this TV.
 *   2. The application's Ambient device association will be removed from Google Photos.
 *   3. Photos and videos will no longer appear on this TV.
 */
@Composable
fun DisconnectDialog(
    deviceName: String = "Living Room TV",
    onConfirmDisconnect: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryFocus = rememberInitialFocusRequester()
    val confirmFocus = remember { FocusRequester() }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .padding(horizontal = 120.dp, vertical = 70.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard)
                .border(1.5.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(40.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GooglePhotosPinwheelIcon(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Disconnect Google Photos",
                        color = DangerRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Disconnect Google Photos from $deviceName?",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "When you disconnect:\n\n• The ambient device registration for \"$deviceName\" will be deleted from your Google Account.\n• Photos and videos from Google Photos will no longer appear on this TV.\n• Local access tokens and cached queue items will be securely removed.\n• You can reconnect anytime by scanning a new setup QR code.",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvButton(
                        text = "Cancel",
                        onClick = onCancel,
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier
                            .focusProperties { right = confirmFocus }
                            .focusRequester(primaryFocus)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    TvButton(
                        text = "Disconnect Google Photos",
                        onClick = onConfirmDisconnect,
                        variant = ButtonVariant.DANGER,
                        modifier = Modifier
                            .focusProperties { left = primaryFocus }
                            .focusRequester(confirmFocus)
                    )
                }
            }
        }
    }
}
