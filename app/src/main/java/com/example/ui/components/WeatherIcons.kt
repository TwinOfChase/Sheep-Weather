package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ConditionIcon(
    weatherCode: Int,
    isDay: Boolean = true,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    tint: Color? = null
) {
    val (iconVector, defaultColor) = when (weatherCode) {
        0 -> if (isDay) (Icons.Default.WbSunny to Color(0xFFFFB300)) else (Icons.Default.NightsStay to Color(0xFF90CAF9))
        1 -> if (isDay) (Icons.Default.WbSunny to Color(0xFFFFA000)) else (Icons.Default.NightsStay to Color(0xFF81D4FA))
        2 -> (Icons.Default.CloudQueue to Color(0xFF64B5F6))
        3 -> (Icons.Default.Cloud to Color(0xFF90A4AE))
        45, 48 -> (Icons.Default.WbTwilight to Color(0xFFB0BEC5))
        51, 53, 55 -> (Icons.Default.Grain to Color(0xFF4FC3F7))
        61, 63, 65 -> (Icons.Default.WaterDrop to Color(0xFF29B6F6))
        71, 73, 75, 77, 85, 86 -> (Icons.Default.Grain to Color(0xFFE0F7FA))
        80, 81, 82 -> (Icons.Default.WaterDrop to Color(0xFF0288D1))
        95, 96, 99 -> (Icons.Default.Thunderstorm to Color(0xFFFFCA28))
        else -> (Icons.Default.WbSunny to Color(0xFFFFB300))
    }

    Icon(
        imageVector = iconVector,
        contentDescription = "Weather condition $weatherCode",
        tint = tint ?: defaultColor,
        modifier = modifier.size(size)
    )
}
