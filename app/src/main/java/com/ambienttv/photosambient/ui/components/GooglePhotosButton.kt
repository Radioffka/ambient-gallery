package com.ambienttv.photosambient.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.R
import com.ambienttv.photosambient.ui.theme.FocusBorder
import com.ambienttv.photosambient.ui.theme.FocusGlow

/**
 * Google Photos Action Button compliant with Google Photos API UX Guidelines:
 * - Uses the official full-color Google Photos vector icon asset (R.drawable.ic_google_photos)
 * - Minimum icon size: 24x24 dp (rendered at 26x26 dp)
 * - Exact text color: #3C4043 on #FFFFFF background
 * - Exact text copy: "Connect to Google Photos"
 * - TV D-pad focus state with smooth scale and border highlight
 */
@Composable
fun GooglePhotosButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Connect to Google Photos",
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.05f else 1.0f, label = "buttonScale")

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isFocused) 16.dp else 4.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = FocusGlow
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFFFFFFF))
            .border(
                width = if (isFocused) 3.5.dp else 1.dp,
                color = if (isFocused) FocusBorder else Color(0xFFDADCE0),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null) {
                onClick()
            }
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_photos),
                contentDescription = "Google Photos",
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                color = Color(0xFF3C4043), // Required official Google font color
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.25.sp
            )
        }
    }
}

/**
 * Official Google Photos full-color icon composable using vector asset
 */
@Composable
fun GooglePhotosPinwheelIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_google_photos),
        contentDescription = "Google Photos",
        modifier = modifier
    )
}
