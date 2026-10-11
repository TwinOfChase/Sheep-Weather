package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HourlyWeather
import com.example.data.model.LayersForecast
import com.example.data.model.LayersForecastCalculator
import com.example.data.model.WeatherInfo
import com.example.data.remote.AiWeatherOverview
import com.example.ui.components.ConditionIcon
import com.example.ui.components.GeminiSparkleLogo
import com.example.util.WeatherSpeechSynthesizer

/**
 * Android Auto / Car Display 4-Quadrant Screen:
 *  - Quadrant 1 (Top-Left): Temperature mirroring phone display (Actual + Real Feel) at larger size,
 *    plus Sunrise/Sunset when in Park Mode.
 *  - Quadrant 2 (Top-Right): Hourly Forecast for a maximum of 4 hours when driving; unlocks full
 *    scrollable 24-hour forecast when in Park Mode.
 *  - Quadrant 3 (Bottom-Left): Layer/Outfit featuring ONLY the layer amount and umbrella status
 *    with no other information.
 *  - Quadrant 4 (Bottom-Right): AI Overview for today shortened for a larger font size when driving;
 *    expands to a scrollable detailed AI overview when in Park Mode.
 *  - Center Speak Button: Anchored at the intersection of all 4 quadrants; reads aloud the
 *    natural spoken weather summary via Android TextToSpeech.
 */
@Composable
fun CarModeWeatherScreen(
    weatherInfo: WeatherInfo,
    layersForecast: LayersForecast?,
    aiOverview: AiWeatherOverview?,
    isCarParked: Boolean,
    onToggleParkMode: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechSynthesizer = remember { WeatherSpeechSynthesizer(context) }
    val isSpeaking by speechSynthesizer.isSpeaking.collectAsStateWithLifecycle()
    val activeSpokenScript by speechSynthesizer.activeSpokenScript.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            speechSynthesizer.shutdown()
        }
    }

    val layers = layersForecast ?: remember(weatherInfo) {
        LayersForecastCalculator.calculate(weatherInfo.hourly, weatherInfo.current.temperature)
    }

    val shortDayOverview = remember(weatherInfo) {
        WeatherSpeechSynthesizer.buildShortDayProgression(weatherInfo, useDegreeSymbol = true)
    }

    val spokenBriefing = remember(weatherInfo, layers, aiOverview, isCarParked) {
        WeatherSpeechSynthesizer.buildSpokenBriefing(
            weather = weatherInfo,
            layers = layers,
            aiOverview = aiOverview,
            isParked = isCarParked
        )
    }

    // Automotive Cockpit High-Contrast Palette
    val cockpitBg = Color(0xFF090F1E)
    val quadrantBg = Color(0xFF121D35)
    val quadrantBorder = if (isCarParked) Color(0xFF38BDF8).copy(alpha = 0.28f) else Color.White.copy(alpha = 0.14f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cockpitBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("car_mode_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Top Automotive Status & Gear Bar
            CarCockpitTopBar(
                locationName = weatherInfo.locationName,
                isCarParked = isCarParked,
                onToggleParkMode = onToggleParkMode,
                onBack = {
                    speechSynthesizer.stop()
                    onBack()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2x2 Four-Quadrant Container with Central Overlapping Speak Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Row: Quadrant 1 (Temperature) & Quadrant 2 (Hourly)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // QUADRANT 1: Temperature (+ Sunrise/Sunset in Park Mode)
                        QuadrantOneTemperature(
                            weatherInfo = weatherInfo,
                            isCarParked = isCarParked,
                            quadrantBg = quadrantBg,
                            quadrantBorder = quadrantBorder,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        // QUADRANT 2: Hourly (Max 4 Hours in Drive, Scrollable 24h in Park)
                        QuadrantTwoHourly(
                            hourlyList = weatherInfo.hourly,
                            isCarParked = isCarParked,
                            quadrantBg = quadrantBg,
                            quadrantBorder = quadrantBorder,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    // Bottom Row: Quadrant 3 (Layer/Outfit & Umbrella Only) & Quadrant 4 (Today's AI Overview)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // QUADRANT 3: Layer / Outfit (Amount & Umbrella ONLY)
                        QuadrantThreeLayersMinimal(
                            layers = layers,
                            quadrantBg = quadrantBg,
                            quadrantBorder = quadrantBorder,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        // QUADRANT 4: AI Overview (Shortened Large Font in Drive, Detailed Scrollable in Park)
                        QuadrantFourAiOverview(
                            shortDayOverview = shortDayOverview,
                            aiOverview = aiOverview,
                            isCarParked = isCarParked,
                            quadrantBg = quadrantBg,
                            quadrantBorder = quadrantBorder,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                // CENTER SPEAK BUTTON at the exact intersection of the 4 quadrants
                CenterSpeakButton(
                    isSpeaking = isSpeaking,
                    onClick = { speechSynthesizer.toggleSpeak(spokenBriefing) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Live Voice Readout Banner when speaking
            AnimatedVisibility(
                visible = isSpeaking && !activeSpokenScript.isNullOrBlank(),
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(200))
            ) {
                Surface(
                    color = Color(0xFF1E3A8A),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(1.dp, Color(0xFF60A5FA), RoundedCornerShape(14.dp))
                        .testTag("spoken_briefing_caption")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Reading Aloud",
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = activeSpokenScript ?: spokenBriefing,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarCockpitTopBar(
    locationName: String,
    isCarParked: Boolean,
    onToggleParkMode: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Exit Car Mode / Back to Phone View
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color(0xFF1E293B),
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onBack() }
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .testTag("car_mode_back_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Android Auto View",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "ANDROID AUTO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 0.8.sp
                    )
                }
                Text(
                    text = locationName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // Vehicle Park / Drive Mode Status & Selector
        Surface(
            color = if (isCarParked) Color(0xFF065F46) else Color(0xFF1E3A8A),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .clickable { onToggleParkMode() }
                .border(
                    1.5.dp,
                    if (isCarParked) Color(0xFF34D399) else Color(0xFF60A5FA),
                    RoundedCornerShape(22.dp)
                )
                .testTag("car_park_mode_toggle")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCarParked) Icons.Default.LocalParking else Icons.Default.DirectionsCar,
                    contentDescription = if (isCarParked) "Park Mode Active" else "Drive Mode Active",
                    tint = if (isCarParked) Color(0xFF6EE7B7) else Color(0xFF93C5FD),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (isCarParked) "PARK MODE (P)" else "DRIVE MODE (D)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = if (isCarParked) "Sun + Full Scroll + Detailed AI" else "Tap to simulate Park",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * QUADRANT 1 (Top-Left):
 * Mirrors the phone's Temperature display (Actual + Real Feel) at a larger size.
 * In Park Mode, also adds Sunrise & Sunset times.
 */
@Composable
private fun QuadrantOneTemperature(
    weatherInfo: WeatherInfo,
    isCarParked: Boolean,
    quadrantBg: Color,
    quadrantBorder: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = quadrantBg,
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
            .border(1.5.dp, quadrantBorder, RoundedCornerShape(22.dp))
            .testTag("quadrant_1_temperature")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Quadrant Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = Color(0xFFFF9F1C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "1 • TEMPERATURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.6.sp
                    )
                }

                ConditionIcon(
                    weatherCode = weatherInfo.current.weatherCode,
                    isDay = weatherInfo.current.isDay,
                    size = 28.dp
                )
            }

            // Large Mirrored Temperature & Real Feel Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${weatherInfo.current.temperature.toInt()}°C",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1.5).sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Surface(
                    color = Color(0xFF1E2D52),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Feels like ${weatherInfo.current.realFeel.toInt()}°C",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFB74D),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = weatherInfo.current.conditionDescription,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Park Mode Exclusive: Sunrise & Sunset
            if (isCarParked) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .testTag("quadrant_1_sunrise_sunset")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Sunrise",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sunrise ${weatherInfo.current.sunrise}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE68A),
                                maxLines = 1
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = "Sunset",
                                tint = Color(0xFFFB923C),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sunset ${weatherInfo.current.sunset}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFED7AA),
                                maxLines = 1
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = weatherInfo.locationName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * QUADRANT 2 (Top-Right):
 * Hourly forecast for a maximum of 4 hours when driving;
 * unlocks a scrollable 24-hour forecast when in Park Mode.
 */
@Composable
private fun QuadrantTwoHourly(
    hourlyList: List<HourlyWeather>,
    isCarParked: Boolean,
    quadrantBg: Color,
    quadrantBorder: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = quadrantBg,
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
            .border(1.5.dp, quadrantBorder, RoundedCornerShape(22.dp))
            .testTag("quadrant_2_hourly")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Quadrant Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCarParked) "2 • HOURLY (SCROLL)" else "2 • NEXT 4 HOURS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.6.sp
                    )
                }
            }

            if (isCarParked) {
                // PARK MODE: Scrollable full 24-hour forecast strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp)
                        .testTag("quadrant_2_park_scrollable_hourly"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(hourlyList.take(24)) { hour ->
                        CarHourlyColumnItem(
                            hour = hour,
                            modifier = Modifier.width(56.dp)
                        )
                    }
                }
                Text(
                    text = "↔ Swipe to scroll 24 hours (Parked)",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF34D399),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // DRIVE MODE: Strictly capped at a maximum of 4 hours
                val fourHours = hourlyList.take(4)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 6.dp)
                        .testTag("quadrant_2_drive_four_hours"),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    fourHours.forEach { hour ->
                        CarHourlyColumnItem(
                            hour = hour,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Text(
                    text = "Max 4 hours shown while driving",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CarHourlyColumnItem(
    hour: HourlyWeather,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1B2A4A),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = hour.displayHour,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            ConditionIcon(
                weatherCode = hour.weatherCode,
                isDay = hour.isDay,
                size = 24.dp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${hour.temperature.toInt()}°",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

/**
 * QUADRANT 3 (Bottom-Left):
 * Layer/Outfit featuring ONLY the layer amount and umbrella status with no other information.
 */
@Composable
private fun QuadrantThreeLayersMinimal(
    layers: LayersForecast,
    quadrantBg: Color,
    quadrantBorder: Color,
    modifier: Modifier = Modifier
) {
    val umbrellaBadgeText = when {
        layers.isUmbrellaRequired && layers.isTooWindyForUmbrella -> "💨 ☂️ Too Windy"
        layers.isUmbrellaRequired -> "☂️ Umbrella"
        layers.isTooWindyForUmbrella -> "💨 No Umbrella"
        else -> "🌂 No Umbrella"
    }

    val umbrellaColor = when {
        layers.isUmbrellaRequired && layers.isTooWindyForUmbrella -> Color(0xFFF97316)
        layers.isUmbrellaRequired -> Color(0xFF38BDF8)
        else -> Color(0xFF34D399)
    }

    Surface(
        color = quadrantBg,
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
            .border(1.5.dp, quadrantBorder, RoundedCornerShape(22.dp))
            .testTag("quadrant_3_layers_minimal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, top = 36.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "3 • LAYER / OUTFIT",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF94A3B8),
                letterSpacing = 0.6.sp,
                modifier = Modifier.fillMaxWidth()
            )

            // Strictly ONLY Layer Amount + Umbrella Status (zero extra text clutter)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Layer Amount Pill
                Surface(
                    color = Color(0xFF1E2D52),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👕 ${layers.layerCount} ${if (layers.layerCount == 1) "Layer" else "Layers"}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 2. Umbrella Pill
                Surface(
                    color = Color(0xFF1E2D52),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, umbrellaColor.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = umbrellaBadgeText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = umbrellaColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

/**
 * QUADRANT 4 (Bottom-Right):
 * AI Overview just for that day, shortened for a larger font size when driving;
 * expands to a scrollable, more detailed AI overview when in Park Mode.
 */
@Composable
private fun QuadrantFourAiOverview(
    shortDayOverview: String,
    aiOverview: AiWeatherOverview?,
    isCarParked: Boolean,
    quadrantBg: Color,
    quadrantBorder: Color,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        color = quadrantBg,
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
            .border(1.5.dp, quadrantBorder, RoundedCornerShape(22.dp))
            .testTag("quadrant_4_ai_overview")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, top = 36.dp, end = 14.dp, bottom = 12.dp)
        ) {
            // Header with Gemini Sparkle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                GeminiSparkleLogo(size = 18.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCarParked) "4 • DETAILED AI (PARK)" else "4 • TODAY AI OVERVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFA5B4FC),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isCarParked) {
                // PARK MODE: Detailed scrollable AI Overview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .testTag("quadrant_4_park_detailed_ai"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = shortDayOverview,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                    if (aiOverview != null) {
                        Text(
                            text = aiOverview.todaySummary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "Tips: ${aiOverview.dressingAndActivityTip}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6EE7B7),
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // DRIVE MODE: Shortened single-day AI overview in a larger, glanceable font size
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = shortDayOverview,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 22.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Central Speak Aloud Button anchored at the intersection of all four quadrants.
 */
@Composable
private fun CenterSpeakButton(
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "speakPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.14f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 550 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonGradient = if (isSpeaking) {
        Brush.radialGradient(
            colors = listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    if (isSpeaking) Color(0xFFEF4444).copy(alpha = 0.28f)
                    else Color(0xFF38BDF8).copy(alpha = 0.22f)
                )
        )

        // Core 84dp Circular Speak Button at the center of the 4 quadrants
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = 12.dp,
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(buttonGradient)
                .border(3.dp, Color(0xFF090F1E), CircleShape)
                .clickable { onClick() }
                .testTag("center_speak_button")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isSpeaking) "Stop Speaking" else "Speak Weather Aloud",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isSpeaking) "STOP" else "SPEAK",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
