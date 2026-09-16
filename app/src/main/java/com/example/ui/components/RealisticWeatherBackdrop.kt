package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MoonInfo

@Composable
fun RealisticWeatherBackdrop(
    isNight: Boolean,
    moonInfo: MoonInfo,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Image in the top section, shifted down so it is not covered by control buttons
        if (isNight) {
            // "In The realistic version the sheep image should be replaced with a picture of the moon reflecting the moon phases."
            Image(
                painter = painterResource(id = R.drawable.realistic_moon),
                contentDescription = "Realistic Moon - ${moonInfo.phaseName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 54.dp)
                    .height(320.dp)
            )

            // Moon phase badge overlay placed below control buttons
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp)
            ) {
                Text(
                    text = "🌙 ${moonInfo.phaseName} • ${moonInfo.illuminationPercent}%",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        } else {
            // "The second style should feature a real life photo of a sheep that again transitions into a glassy blurred backround again for maximum readibility."
            Image(
                painter = painterResource(id = R.drawable.realistic_sheep),
                contentDescription = "Realistic Sheep Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 54.dp)
                    .height(320.dp)
            )
        }

        // Glassy blurred gradient veil transition for maximum readability
        // Blurs and shades into the dark/light card background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isNight) {
                            listOf(
                                Color.Transparent,
                                Color(0xFF070B19).copy(alpha = 0.40f),
                                Color(0xFF070B19).copy(alpha = 0.88f),
                                Color(0xFF070B19)
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                Color(0xFF1B2A47).copy(alpha = 0.35f),
                                Color(0xFF0F172A).copy(alpha = 0.85f),
                                Color(0xFF0F172A)
                            )
                        },
                        startY = 120f,
                        endY = 780f
                    )
                )
        )
    }
}
