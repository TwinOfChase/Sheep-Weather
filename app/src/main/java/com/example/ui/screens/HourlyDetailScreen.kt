package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyConditionType
import com.example.data.model.HourlyWeather
import com.example.data.model.WeatherInfo
import com.example.ui.components.ConditionIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourlyDetailScreen(
    weatherInfo: WeatherInfo,
    isNight: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCondition by remember { mutableStateOf(HourlyConditionType.TEMPERATURE) }

    val bgGradient = if (isNight) {
        Brush.verticalGradient(listOf(Color(0xFF070E20), Color(0xFF0F1A36), Color(0xFF132044)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFEBF4FA), Color(0xFFE1EDF7), Color(0xFFD6E6F2)))
    }

    val cardBg = if (isNight) Color(0xFF1A2649).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.92f)
    val textColor = if (isNight) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Calculate min/max for the current selected metric across hours to draw proportional bars
    val hourlyList = weatherInfo.hourly
    val values = hourlyList.map { getMetricValue(it, selectedCondition) }
    val minVal = values.minOrNull() ?: 0.0
    val maxVal = values.maxOrNull() ?: 1.0
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgGradient)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hourly Breakdown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor
                        )
                        Text(
                            text = "${weatherInfo.locationName} • Next 24 Hours",
                            fontSize = 12.sp,
                            color = subTextColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Horizontal scrolling condition selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HourlyConditionType.values().forEach { condition ->
                    val isSelected = condition == selectedCondition
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCondition = condition },
                        label = {
                            Text(
                                text = condition.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getConditionChipIcon(condition),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isNight) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            selectedLabelColor = if (isNight) Color(0xFF0C192E) else Color.White,
                            selectedLeadingIconColor = if (isNight) Color(0xFF0C192E) else Color.White,
                            containerColor = if (isNight) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                            labelColor = if (isNight) Color(0xFFE2E8F0) else Color(0xFF334155),
                            iconColor = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color.Transparent else if (isNight) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f),
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("chip_${condition.name}")
                    )
                }
            }

            // Summary metric card
            Surface(
                color = cardBg,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(
                        1.dp,
                        if (isNight) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricStatItem(
                        label = "Minimum",
                        value = "${formatVal(minVal, selectedCondition)} ${selectedCondition.unit}",
                        textColor = textColor,
                        subTextColor = subTextColor
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(if (isNight) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f))
                    )
                    MetricStatItem(
                        label = "Average",
                        value = "${formatVal(values.average(), selectedCondition)} ${selectedCondition.unit}",
                        textColor = textColor,
                        subTextColor = subTextColor
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(if (isNight) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f))
                    )
                    MetricStatItem(
                        label = "Maximum",
                        value = "${formatVal(maxVal, selectedCondition)} ${selectedCondition.unit}",
                        textColor = textColor,
                        subTextColor = subTextColor
                    )
                }
            }

            // Hourly List with values and relative bars
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(hourlyList) { index, hour ->
                    val metricValue = getMetricValue(hour, selectedCondition)
                    val progress = ((metricValue - minVal) / range).toFloat().coerceIn(0.08f, 1.0f)

                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(18.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f),
                                RoundedCornerShape(18.dp)
                            )
                            .testTag("hourly_item_$index")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour & Condition
                            Column(modifier = Modifier.width(68.dp)) {
                                Text(
                                    text = hour.displayHour,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                                Text(
                                    text = if (hour.isDay) "Day" else "Night",
                                    fontSize = 11.sp,
                                    color = subTextColor
                                )
                            }

                            ConditionIcon(
                                weatherCode = hour.weatherCode,
                                isDay = hour.isDay,
                                size = 28.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            // Bar showing relative magnitude
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = hour.conditionDescription,
                                        fontSize = 12.sp,
                                        color = subTextColor,
                                        maxLines = 1
                                    )
                                    // Secondary context (e.g. Real feel if showing temp)
                                    if (selectedCondition != HourlyConditionType.REAL_FEEL) {
                                        Text(
                                            text = "Feels ${hour.realFeel.toInt()}°C",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isNight) Color(0xFFFFB74D) else Color(0xFFD97706)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Visual indicator bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isNight) Color(0xFF243356) else Color(0xFFE2E8F0))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progress)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = getConditionColors(selectedCondition, isNight)
                                                )
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Primary Metric Number Display
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${formatVal(metricValue, selectedCondition)}${selectedCondition.unit}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatItem(
    label: String,
    value: String,
    textColor: Color,
    subTextColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = subTextColor, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

private fun getMetricValue(hourly: HourlyWeather, type: HourlyConditionType): Double {
    return when (type) {
        HourlyConditionType.TEMPERATURE -> hourly.temperature
        HourlyConditionType.REAL_FEEL -> hourly.realFeel
        HourlyConditionType.PRECIPITATION -> hourly.precipitationProbability.toDouble()
        HourlyConditionType.WIND_SPEED -> hourly.windSpeed
        HourlyConditionType.HUMIDITY -> hourly.humidity.toDouble()
        HourlyConditionType.UV_INDEX -> hourly.uvIndex
        HourlyConditionType.CLOUD_COVER -> hourly.cloudCover.toDouble()
    }
}

private fun formatVal(value: Double, type: HourlyConditionType): String {
    return when (type) {
        HourlyConditionType.UV_INDEX -> "%.1f".format(value)
        HourlyConditionType.TEMPERATURE, HourlyConditionType.REAL_FEEL -> "${value.toInt()}"
        HourlyConditionType.WIND_SPEED -> "%.0f".format(value)
        else -> "${value.toInt()}"
    }
}

private fun getConditionChipIcon(type: HourlyConditionType): ImageVector {
    return when (type) {
        HourlyConditionType.TEMPERATURE -> Icons.Default.Thermostat
        HourlyConditionType.REAL_FEEL -> Icons.Default.DeviceThermostat
        HourlyConditionType.PRECIPITATION -> Icons.Default.Grain
        HourlyConditionType.WIND_SPEED -> Icons.Default.Air
        HourlyConditionType.HUMIDITY -> Icons.Default.WaterDrop
        HourlyConditionType.UV_INDEX -> Icons.Default.WbSunny
        HourlyConditionType.CLOUD_COVER -> Icons.Default.Cloud
    }
}

private fun getConditionColors(type: HourlyConditionType, isNight: Boolean): List<Color> {
    return when (type) {
        HourlyConditionType.TEMPERATURE -> listOf(Color(0xFFFF9F1C), Color(0xFFFF4040))
        HourlyConditionType.REAL_FEEL -> listOf(Color(0xFFFFB74D), Color(0xFFF57C00))
        HourlyConditionType.PRECIPITATION -> listOf(Color(0xFF48CAE4), Color(0xFF0077B6))
        HourlyConditionType.WIND_SPEED -> listOf(Color(0xFF64DFDF), Color(0xFF48BFE3))
        HourlyConditionType.HUMIDITY -> listOf(Color(0xFF00B4D8), Color(0xFF0077B6))
        HourlyConditionType.UV_INDEX -> listOf(Color(0xFFFFD166), Color(0xFFEF476F))
        HourlyConditionType.CLOUD_COVER -> listOf(Color(0xFF90A4AE), Color(0xFF546E7A))
    }
}
