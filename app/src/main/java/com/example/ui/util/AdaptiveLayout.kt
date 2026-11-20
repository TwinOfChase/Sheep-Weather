package com.example.ui.util

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AdaptiveUiSpec(
    val isTabletOrUnfolded: Boolean,
    val scale: Float,
    val fontScale: Float,
    val horizontalPadding: Dp,
    val verticalSpacing: Dp,
    val cardPadding: Dp,
    val cardCornerRadius: Dp,
    val viewingSpaceHeight: Dp,
    val drawerWidth: Dp
) {
    companion object {
        val PhoneBaseline = AdaptiveUiSpec(
            isTabletOrUnfolded = false,
            scale = 1.0f,
            fontScale = 1.0f,
            horizontalPadding = 16.dp,
            verticalSpacing = 16.dp,
            cardPadding = 18.dp,
            cardCornerRadius = 24.dp,
            viewingSpaceHeight = 170.dp,
            drawerWidth = 320.dp
        )
    }
}

/**
 * Computes responsive layout parameters from actual container bounds.
 * Keeps 6.0" to 6.7" phones (compact width/height) completely unchanged at PhoneBaseline (1.0x),
 * while scaling up seamlessly for unfolded foldables and tablets up to 10.4"+.
 */
fun computeAdaptiveUiSpec(maxWidth: Dp, maxHeight: Dp): AdaptiveUiSpec {
    val minDimension = minOf(maxWidth, maxHeight)
    val isTabletOrUnfolded = minDimension >= 560.dp && maxWidth >= 600.dp

    if (!isTabletOrUnfolded) {
        return AdaptiveUiSpec.PhoneBaseline
    }

    val widthFactor = (maxWidth.value / 600f).coerceIn(1.0f, 1.65f)
    val uiScale = (1.16f + (widthFactor - 1.0f) * 0.24f).coerceIn(1.16f, 1.30f)
    val textScale = (1.14f + (widthFactor - 1.0f) * 0.18f).coerceIn(1.14f, 1.24f)

    return AdaptiveUiSpec(
        isTabletOrUnfolded = true,
        scale = uiScale,
        fontScale = textScale,
        horizontalPadding = (24f * (1f + (widthFactor - 1f) * 0.35f)).coerceIn(24f, 34f).dp,
        verticalSpacing = 20.dp,
        cardPadding = 22.dp,
        cardCornerRadius = 28.dp,
        viewingSpaceHeight = (maxHeight * 0.24f).coerceIn(200.dp, 265.dp),
        drawerWidth = (maxWidth * 0.45f).coerceIn(360.dp, 420.dp)
    )
}
