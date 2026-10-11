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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStyle
import com.example.data.model.DailyWeather
import com.example.data.model.HourlyWeather
import com.example.data.model.LayersForecast
import com.example.data.model.LayersForecastCalculator
import com.example.data.model.WeatherInfo
import com.example.ui.WeatherUiState
import com.example.ui.components.AiOverviewCard
import com.example.ui.components.AppCopyrightFooter
import com.example.ui.components.CartoonSheepCanvas
import com.example.ui.components.ConditionIcon
import com.example.ui.components.LayersForecastCard
import com.example.ui.components.RealisticWeatherBackdrop
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainWeatherScreen(
    uiState: WeatherUiState,
    onToggleStyle: () -> Unit,
    onToggleNight: () -> Unit,
    onRefresh: () -> Unit,
    onSelectLocation: (Long) -> Unit,
    onAddLocationByName: (String, String?) -> Unit,
    onDeleteLocation: (com.example.data.local.LocationEntity) -> Unit,
    onOpenHourlyDetail: () -> Unit,
    onOpenCarMode: () -> Unit = {},
    onRefreshAi: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onToggleAutoTheme: (Boolean) -> Unit = {},
    onSetManualNight: (Boolean) -> Unit = {},
    onToggleDataTracking: (Boolean) -> Unit = {},
    onToggleUseCurrentLocation: (Boolean) -> Unit = {},
    onFetchCurrentLocation: () -> Unit = {},
    onCloseSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val weather = uiState.weatherInfo
    val isNight = uiState.isNightEffective
    val style = uiState.appStyle

    // High contrast typography colors
    val textColor = if (isNight) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isNight) Color(0xFF94A3B8) else Color(0xFF475569)
    val cardBg = if (isNight) Color(0xFF14203B).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.92f)
    val cardBorder = if (isNight) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isNight) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(320.dp)
            ) {
                LocationsDrawerContent(
                    locations = uiState.locations,
                    selectedLocationId = uiState.selectedLocation?.id ?: 0L,
                    isNight = isNight,
                    onSelectLocation = { id ->
                        onSelectLocation(id)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onAddLocationByName = onAddLocationByName,
                    onDeleteLocation = onDeleteLocation,
                    onClose = {
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenSettings = {
                        coroutineScope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                    onUseCurrentLocation = {
                        coroutineScope.launch { drawerState.close() }
                        onFetchCurrentLocation()
                    },
                    onOpenCarMode = {
                        coroutineScope.launch { drawerState.close() }
                        onOpenCarMode()
                    }
                )
            }
        }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
        // 1. Two Designs Background: Cartoon Sheep vs Realistic Sheep
        when (style) {
            AppStyle.CARTOON_SHEEP -> {
                CartoonSheepCanvas(
                    isNight = isNight,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppStyle.REALISTIC_SHEEP -> {
                RealisticWeatherBackdrop(
                    isNight = isNight,
                    moonInfo = uiState.moonInfo,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. Foreground scrollable content with maximum readability
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation & Control Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location selection pill (opens slide-out drawer from left)
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .clickable {
                                coroutineScope.launch { drawerState.open() }
                            }
                            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                            .testTag("location_picker_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = weather?.locationName ?: "Select City",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Open",
                                tint = subTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Top Action Controls: Style Toggle, Day/Night preview, Refresh
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Style toggle pill: Cartoon vs Realistic
                        Surface(
                            color = cardBg,
                            shape = RoundedCornerShape(18.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clickable { onToggleStyle() }
                                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                .testTag("style_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (style == AppStyle.CARTOON_SHEEP) "🐑 Cartoon" else "📷 Photo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }

                        // Android Auto / 4-Quadrant Car Mode button
                        Surface(
                            color = cardBg,
                            shape = CircleShape,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clickable { onOpenCarMode() }
                                .border(1.dp, cardBorder, CircleShape)
                                .testTag("open_car_mode_button")
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "Android Auto Car Mode",
                                    tint = if (isNight) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Day/Night toggle button
                        Surface(
                            color = cardBg,
                            shape = CircleShape,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clickable { onToggleNight() }
                                .border(1.dp, cardBorder, CircleShape)
                                .testTag("day_night_toggle")
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Icon(
                                    imageVector = if (isNight) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                    contentDescription = "Day Night Toggle",
                                    tint = if (isNight) Color(0xFF90CAF9) else Color(0xFFFFB300),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Refresh button
                        Surface(
                            color = cardBg,
                            shape = CircleShape,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clickable { onRefresh() }
                                .border(1.dp, cardBorder, CircleShape)
                                .testTag("refresh_button")
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = textColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Settings button
                        Surface(
                            color = cardBg,
                            shape = CircleShape,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clickable { onOpenSettings() }
                                .border(1.dp, cardBorder, CircleShape)
                                .testTag("open_settings_button")
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = textColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Data Saver / Cache / Privacy status indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = if (!uiState.isDataTrackingEnabled) {
                                    if (isNight) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFE0F2FE)
                                } else if (isNight) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFD1FAE5),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (!uiState.isDataTrackingEnabled) Icons.Default.Security else Icons.Default.DataSaverOn,
                            contentDescription = null,
                            tint = if (!uiState.isDataTrackingEnabled) {
                                if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                            } else if (isNight) Color(0xFF34D399) else Color(0xFF059669),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (!uiState.isDataTrackingEnabled) {
                                "Privacy: Tracking OFF"
                            } else if (weather?.isFromCache == true) {
                                "Data Saver: Cached (${"%.1f".format(weather.dataSizeKb)} KB)"
                            } else {
                                "Data Saver: Live (${"%.1f".format(weather?.dataSizeKb ?: 3.2)} KB)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!uiState.isDataTrackingEnabled) {
                                if (isNight) Color(0xFF93C5FD) else Color(0xFF0369A1)
                            } else if (isNight) Color(0xFFA7F3D0) else Color(0xFF065F46)
                        )
                    }

                    // Night mode status explanation
                    Text(
                        text = if (!uiState.isAutoThemeEnabled) {
                            if (isNight) "🌙 Fixed Dark" else "☀️ Fixed Light"
                        } else if (isNight) "🌙 Auto Night" else "☀️ Auto Day",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = subTextColor
                    )
                }
            }

            // Blank space in place of the removed in-app widget so the cartoon sheep or realistic sheep/moon background can be viewed
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .testTag("background_viewing_space")
                )
            }

            // Primary Temperature Hero Card:
            // "Combine Real Feel and Actual Temperature sections into just Temperature displaying both"
            if (weather != null) {
                item {
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                            .testTag("temperature_hero_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // High-contrast primary metric row with "Temperature" title
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Thermostat,
                                        contentDescription = "Temperature",
                                        tint = if (isNight) Color(0xFFFF9F1C) else Color(0xFFEA580C),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Temperature",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ConditionIcon(
                                        weatherCode = weather.current.weatherCode,
                                        isDay = !isNight,
                                        size = 36.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = weather.current.conditionDescription,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = subTextColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Combined Temperature Box displaying both Actual and Real Feel side-by-side
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isNight) Color(0xFF1E2D52) else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Actual Temperature Display
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "ACTUAL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = subTextColor,
                                        letterSpacing = 0.6.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${weather.current.temperature.toInt()}°C",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor,
                                        letterSpacing = (-1.2).sp
                                    )
                                }

                                // Subtle divider
                                Box(
                                    modifier = Modifier
                                        .height(38.dp)
                                        .width(1.dp)
                                        .background(if (isNight) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f))
                                )

                                // Real Feel Display
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "REAL FEEL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNight) Color(0xFFFFB74D) else Color(0xFFD97706),
                                        letterSpacing = 0.6.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${weather.current.realFeel.toInt()}°C",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isNight) Color(0xFFFFB74D) else Color(0xFFD97706),
                                        letterSpacing = (-1.2).sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Grid of secondary metrics: Wind, Humidity, UV, Rain Prob
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                QuickMetricItem(
                                    icon = Icons.Default.Air,
                                    label = "Wind",
                                    value = "${weather.current.windSpeed.toInt()} km/h",
                                    isNight = isNight
                                )
                                QuickMetricItem(
                                    icon = Icons.Default.WaterDrop,
                                    label = "Humidity",
                                    value = "${weather.current.humidity}%",
                                    isNight = isNight
                                )
                                QuickMetricItem(
                                    icon = Icons.Default.WbSunny,
                                    label = "UV Index",
                                    value = "%.1f".format(weather.current.uvIndex),
                                    isNight = isNight
                                )
                                QuickMetricItem(
                                    icon = Icons.Default.Grain,
                                    label = "Rain",
                                    value = "${weather.hourly.firstOrNull()?.precipitationProbability ?: 10}% • ${"%.1f".format(weather.hourly.firstOrNull()?.precipitationMm ?: weather.current.precipitationMm)} mm",
                                    isNight = isNight
                                )
                            }
                        }
                    }
                }
            }

            // Sunset & Sunrise Section:
            // "Add a sunset/sunrise function"
            if (weather != null) {
                item {
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                            .testTag("sunrise_sunset_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = "Sun Cycle",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sunrise & Sunset",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                                Text(
                                    text = "Local Solar Times",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = subTextColor
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Sunrise Box
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isNight) Color(0xFF1E2D52) else Color(0xFFFFFBEB),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isNight) Color(0xFFF59E0B).copy(alpha = 0.35f) else Color(0xFFFDE68A)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFEF3C7)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WbSunny,
                                                contentDescription = "Sunrise",
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Sunrise",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = subTextColor
                                            )
                                            Text(
                                                text = weather.current.sunrise,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }
                                    }
                                }

                                // Sunset Box
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isNight) Color(0xFF1E2D52) else Color(0xFFFFF7ED),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isNight) Color(0xFFF97316).copy(alpha = 0.35f) else Color(0xFFFFEDD5)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFEDD5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NightsStay,
                                                contentDescription = "Sunset",
                                                tint = Color(0xFFEA580C),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Sunset",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = subTextColor
                                            )
                                            Text(
                                                text = weather.current.sunset,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
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

            // 8-Hour Outfit & Layer Forecast Section:
            // "Add a section featuring the amount of layers forecast information could include llike a tshirt emoji with a number and then show an umbrella if required this should be shown reflecting thw weather for the next 8 hours."
            if (weather != null) {
                item {
                    val layers = uiState.layersForecast ?: remember(weather) {
                        LayersForecastCalculator.calculate(weather.hourly, weather.current.temperature)
                    }
                    LayersForecastCard(
                        layersForecast = layers,
                        isNight = isNight
                    )
                }
            }

            // Hourly Forecast Strip:
            // "I wish for it to have an hourly forecast that shows the temperature and an icon showing the condtitions.
            // Then by tapping on the hourly forecast strip it will open another screen where you can select a specific condition to show and scrol through the hours."
            if (weather != null) {
                item {
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                            .clickable { onOpenHourlyDetail() }
                            .testTag("hourly_forecast_strip")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            // Section header with tap hint
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hourly Forecast",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Deep-Dive",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Open Hourly Detail",
                                        tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Horizontal strip of hours
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(weather.hourly.take(24)) { hour ->
                                    HourlyStripCard(
                                        hour = hour,
                                        isNight = isNight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Overview Section with Gemini Logo:
            // "Also add an ai overview section with the gemni logo. Itbshould give overview info for the day, and the next few days."
            if (weather != null) {
                item {
                    AiOverviewCard(
                        overview = uiState.aiOverview,
                        isLoading = uiState.isAiLoading,
                        isNight = isNight,
                        onRefreshAi = onRefreshAi
                    )
                }
            }

            // 7-Day Forecast Section:
            // "On the main screen you should be able to scrool to view information for the next 7 days."
            if (weather != null) {
                item {
                    Surface(
                        color = cardBg,
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "7-Day Forecast",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            weather.daily.take(7).forEachIndexed { index, day ->
                                DailyForecastRow(
                                    day = day,
                                    isNight = isNight,
                                    isToday = index == 0
                                )
                                if (index < 6) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .height(0.6.dp)
                                            .background(if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom space and Mandatory Copyright
            item {
                AppCopyrightFooter(isNight = isNight)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

    // Settings Dialog overlay
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            isNight = isNight,
            isAutoThemeEnabled = uiState.isAutoThemeEnabled,
            isManualNight = uiState.isManualNight,
            isDataTrackingEnabled = uiState.isDataTrackingEnabled,
            isUseCurrentLocationEnabled = uiState.isUseCurrentLocationEnabled,
            onToggleAutoTheme = onToggleAutoTheme,
            onSetManualNight = onSetManualNight,
            onToggleDataTracking = onToggleDataTracking,
            onToggleUseCurrentLocation = onToggleUseCurrentLocation,
            onDetectCurrentLocation = onFetchCurrentLocation,
            onDismiss = onCloseSettings
        )
    }
}

@Composable
private fun HourlyStripCard(
    hour: HourlyWeather,
    isNight: Boolean
) {
    Surface(
        color = if (isNight) Color(0xFF1E2D52) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.width(78.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hour.displayHour,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isNight) Color(0xFFE2E8F0) else Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ConditionIcon(
                weatherCode = hour.weatherCode,
                isDay = hour.isDay,
                size = 28.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${hour.temperature.toInt()}°",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isNight) Color.White else Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(3.dp))
            // Real Feel indicator in strip - cleanly abbreviated so it always fits on one line
            Text(
                text = "RF ${hour.realFeel.toInt()}°",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNight) Color(0xFFFFB74D) else Color(0xFFD97706),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun DailyForecastRow(
    day: DailyWeather,
    isNight: Boolean,
    isToday: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Day name & date
        Column(modifier = Modifier.width(80.dp)) {
            Text(
                text = day.dayOfWeek,
                fontSize = 15.sp,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isToday) {
                    if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                } else if (isNight) Color.White else Color(0xFF0F172A)
            )
            Text(
                text = day.formattedDate,
                fontSize = 11.sp,
                color = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        }

        // Icon and Rain probability
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            ConditionIcon(
                weatherCode = day.weatherCode,
                isDay = true,
                size = 26.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (day.precipitationProbability > 20) {
                Text(
                    text = "${day.precipitationProbability}% (${"%.1f".format(day.precipitationMm)} mm)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isNight) Color(0xFF67E8F9) else Color(0xFF0284C7)
                )
            } else {
                Text(
                    text = day.conditionDescription,
                    fontSize = 12.sp,
                    color = if (isNight) Color(0xFFCBD5E1) else Color(0xFF64748B),
                    maxLines = 1
                )
            }
        }

        // Temperatures: Min / Max (Real Feel removed from other condition displays)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${day.tempMin.toInt()}°",
                fontSize = 14.sp,
                color = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${day.tempMax.toInt()}°",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNight) Color.White else Color(0xFF0F172A)
            )
        }
    }
}

@Composable
private fun QuickMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isNight: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isNight) Color.White else Color(0xFF0F172A)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)
        )
    }
}
