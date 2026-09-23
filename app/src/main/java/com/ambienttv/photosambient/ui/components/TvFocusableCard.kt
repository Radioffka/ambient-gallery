package com.ambienttv.photosambient.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ambienttv.photosambient.ui.theme.DangerRed
import com.ambienttv.photosambient.ui.theme.FocusBorder
import com.ambienttv.photosambient.ui.theme.FocusGlow
import com.ambienttv.photosambient.ui.theme.SurfaceCard
import com.ambienttv.photosambient.ui.theme.SurfaceCardFocused
import com.ambienttv.photosambient.ui.theme.TextPrimary

enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    DANGER,
    TRANSPARENT
}

@Composable
fun TvButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.05f else 1.0f, label = "btnScale")

    val bg = when (variant) {
        ButtonVariant.PRIMARY -> if (isFocused) Color(0xFF4285F4) else Color(0xFF252B37)
        ButtonVariant.SECONDARY -> if (isFocused) Color(0xFF323A4A) else Color(0xFF1E232D)
        ButtonVariant.DANGER -> if (isFocused) Color(0xFFD93025) else Color(0xFF331B1E)
        ButtonVariant.TRANSPARENT -> if (isFocused) Color(0x33FFFFFF) else Color.Transparent
    }

    val textColor = when (variant) {
        ButtonVariant.DANGER -> if (isFocused) Color.White else DangerRed
        else -> TextPrimary
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isFocused) 12.dp else 0.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = FocusGlow
            )
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) FocusBorder else Color(0x22FFFFFF),
                shape = RoundedCornerShape(24.dp)
            )
            .focusable(enabled = enabled, interactionSource = interactionSource)
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null) {
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.let {
                it()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(end = 10.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable (isFocused: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.03f else 1.0f, label = "cardScale")

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isFocused) 16.dp else 2.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = FocusGlow
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (isFocused) SurfaceCardFocused else SurfaceCard)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) FocusBorder else Color(0x1AFFFFFF),
                shape = RoundedCornerShape(cornerRadius)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick()
            }
            .padding(20.dp)
    ) {
        content(isFocused)
    }
}