package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MoonInfo
import com.example.ui.util.computeAdaptiveUiSpec

@Composable
fun RealisticWeatherBackdrop(
    isNight: Boolean,
    moonInfo: MoonInfo,
    modifier: Modifier = Modifier
) {
    val baseBgColor = if (isNight) Color(0xFF070B19) else Color(0xFF0F172A)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(baseBgColor)
            .graphicsLayer { clip = false }
    ) {
        val adaptive = computeAdaptiveUiSpec(maxWidth, maxHeight)
        val density = LocalDensity.current

        // Edge-to-edge starting at 0.dp so the backdrop covers behind the menu/location & reload buttons
        // and extends comfortably past the Temperature Hero Card on both phones and 10.4" tablets.
        val heroHeight = if (adaptive.isTabletOrUnfolded) {
            maxOf(540.dp, maxHeight * 0.62f)
        } else {
            maxOf(420.dp, maxHeight * 0.54f)
        }
        val heroHeightPx = with(density) { heroHeight.toPx() }.coerceAtLeast(1f)

        val imageRes = if (isNight) R.drawable.realistic_moon else R.drawable.realistic_sheep
        val imageDesc = if (isNight) "Realistic Moon - ${moonInfo.phaseName}" else "Realistic Sheep Photo"

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = imageDesc,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0f, -0.15f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(heroHeight)
        )

        // Smooth container-relative gradient veil transition for maximum foreground card readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isNight) {
                            listOf(
                                Color(0xFF070B19).copy(alpha = 0.14f),
                                Color(0xFF070B19).copy(alpha = 0.42f),
                                Color(0xFF070B19).copy(alpha = 0.88f),
                                Color(0xFF070B19)
                            )
                        } else {
                            listOf(
                                Color(0xFF0F172A).copy(alpha = 0.10f),
                                Color(0xFF1B2A47).copy(alpha = 0.36f),
                                Color(0xFF0F172A).copy(alpha = 0.85f),
                                Color(0xFF0F172A)
                            )
                        },
                        startY = heroHeightPx * 0.14f,
                        endY = heroHeightPx * 0.98f
                    )
                )
        )
    }
}
