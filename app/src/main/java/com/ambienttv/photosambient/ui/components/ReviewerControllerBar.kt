package com.ambienttv.photosambient.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.ui.theme.FocusBorder
import com.ambienttv.photosambient.ui.theme.GoogleBlue
import com.ambienttv.photosambient.ui.theme.TextPrimary
import com.ambienttv.photosambient.ui.theme.TextSecondary

/**
 * A reviewer navigation bar for Google Photos Partner Program evaluation.
 * Allows quick transitions between all states without blocking normal user experience.
 */
@Composable
fun ReviewerControllerBar(
    currentScreenName: String,
    onNavigateTo: (String) -> Unit,
    onResetDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xE612151B))
                .border(1.dp, Color(0x334285F4), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GoogleBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google Photos Partner Review Demo Controller  [State: $currentScreenName]",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (isExpanded) "▲ Hide Jump Bar" else "▼ Show Jump Bar",
                    color = GoogleBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReviewerNavPill("1. Welcome", "welcome", currentScreenName, onNavigateTo)
                    ReviewerNavPill("2. Permissions", "permissions", currentScreenName, onNavigateTo)
                    ReviewerNavPill("3. Device Name", "device_name", currentScreenName, onNavigateTo)
                    ReviewerNavPill("4. OAuth QR", "oauth_qr", currentScreenName, onNavigateTo)
                    ReviewerNavPill("5. Wait Setup", "waiting_setup", currentScreenName, onNavigateTo)
                    ReviewerNavPill("6. Settings", "settings", currentScreenName, onNavigateTo)
                    ReviewerNavPill("7. Photo Slideshow", "photo_ambient", currentScreenName, onNavigateTo)
                    ReviewerNavPill("8. Video Playback", "video_ambient", currentScreenName, onNavigateTo)
                    ReviewerNavPill("9. Disconnect", "disconnect_dialog", currentScreenName, onNavigateTo)
                    ReviewerNavPill("↺ Reset Clean", "reset", currentScreenName) { onResetDemo() }
                }
            }
        }
    }
}

@Composable
private fun ReviewerNavPill(
    label: String,
    route: String,
    currentScreenName: String,
    onClick: (String) -> Unit
) {
    val isSelected = currentScreenName.equals(route, ignoreCase = true)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) GoogleBlue else if (isFocused) Color(0xFF2E384D) else Color(0xFF1E232E))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) FocusBorder else Color(0x22FFFFFF),
                shape = RoundedCornerShape(8.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick(route)
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}