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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocationEntity
import com.example.data.model.AppStyle
import com.example.data.model.DailyWeather
import com.example.data.model.HourlyWeather
import com.example.data.model.LayersForecastCalculator
import com.example.ui.ThemeMode
import com.example.ui.WeatherUiState
import com.example.ui.components.AiOverviewCard
import com.example.ui.components.AppCopyrightFooter
import com.example.ui.components.CartoonSheepCanvas
import com.example.ui.components.ConditionIcon
import com.example.ui.components.LayersForecastCard
import com.example.ui.components.RealisticWeatherBackdrop
import com.example.util.AppUpdateManager
import com.example.util.TemperatureFormatter
import com.example.util.TemperatureUnit
import kotlinx.coroutines.launch

@Composable
fun MainWeatherScreen(
    uiState: WeatherUiState,
    onRefresh: () -> Unit,
    onSelectLocation: (Long) -> Unit,
    onAddLocationByName: (String, String?) -> Unit,
    onDeleteLocation: (LocationEntity) -> Unit,
    onOpenHourlyDetail: () -> Unit,
    onRefreshAi: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSetTemperatureUnit: (TemperatureUnit) -> Unit = {},
    onSetAiEnabled: (Boolean) -> Unit = {},
    onSetThemeMode: (ThemeMode) -> Unit = {},
    onSetAppStyle: (AppStyle) -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    onDismissUpdateDialog: () -> Unit = {},
    onToggleDataTracking: (Boolean) -> Unit = {},
    onToggleUseCurrentLocation: (Boolean) -> Unit = {},
    onFetchCurrentLocation: () -> Unit = {},
    onCloseSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val weather = uiState.weatherInfo
    val isNight = uiState.isNightEffective
    val style = uiState.appStyle
    val tempUnit = uiState.temperatureUnit

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
                    onCheckForUpdates = {
                        coroutineScope.launch { drawerState.close() }
                        onCheckForUpdates()
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
                // Top Navigation & Control Header (Cleaned up: Only Location Pill and Refresh Button)
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
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
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
                    }
                }

                // Data Saver / Cache / Privacy status indicator
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isNight) Color(0xFF0F172A).copy(alpha = 0.7f) else Color(0xFFF1F5F9).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DataSaverOn,
                                contentDescription = "Data Saver",
                                tint = if (isNight) Color(0xFF34D399) else Color(0xFF059669),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Data Saver: Zero background refresh • Cached locally",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNight) Color(0xFFA7F3D0) else Color(0xFF065F46)
                            )
                        }

                        Text(
                            text = weather?.let { "Updated: ${it.current.time}" } ?: "Offline",
                            fontSize = 10.5.sp,
                            color = subTextColor
                        )
                    }
                }

                // Blank space for mascot viewing
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .testTag("background_viewing_space")
                    )
                }

                // Primary Temperature Hero Card (Combined Actual and Real Feel)
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
                                // Title row
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
                                            text = TemperatureFormatter.format(weather.current.temperature, tempUnit),
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
                                            text = TemperatureFormatter.format(weather.current.realFeel, tempUnit),
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isNight) Color(0xFFFFB74D) else Color(0xFFD97706),
                                            letterSpacing = (-1.2).sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Grid of secondary metrics: Wind, Humidity, UV, Rain
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
                                    // Rain metric with probability % and millimeters
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

                // Sunset & Sunrise Section
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

                // 8-Hour Outfit & Layer Forecast Section
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

                // Hourly Forecast Strip
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
                                            isNight = isNight,
                                            temperatureUnit = tempUnit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Overview Section with Gemini Logo (Gated behind isAiEnabled)
                if (weather != null && uiState.isAiEnabled) {
                    item {
                        AiOverviewCard(
                            overview = uiState.aiOverview,
                            isLoading = uiState.isAiLoading,
                            isNight = isNight,
                            onRefreshAi = onRefreshAi
                        )
                    }
                }

                // 7-Day Forecast Section
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
                                        isToday = index == 0,
                                        temperatureUnit = tempUnit
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
            temperatureUnit = uiState.temperatureUnit,
            isAiEnabled = uiState.isAiEnabled,
            themeMode = uiState.themeMode,
            appStyle = uiState.appStyle,
            isDataTrackingEnabled = uiState.isDataTrackingEnabled,
            isUseCurrentLocationEnabled = uiState.isUseCurrentLocationEnabled,
            onSetTemperatureUnit = onSetTemperatureUnit,
            onSetAiEnabled = onSetAiEnabled,
            onSetThemeMode = onSetThemeMode,
            onSetAppStyle = onSetAppStyle,
            onToggleDataTracking = onToggleDataTracking,
            onToggleUseCurrentLocation = onToggleUseCurrentLocation,
            onDetectCurrentLocation = onFetchCurrentLocation,
            onCheckForUpdates = onCheckForUpdates,
            onDismiss = onCloseSettings
        )
    }

    // Update Check Result Dialog overlay
    if (uiState.isCheckingUpdates) {
        AlertDialog(
            onDismissRequest = onDismissUpdateDialog,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Checking for Updates...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(text = "Connecting to GitHub releases to check for newer APK packages.", fontSize = 13.sp)
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismissUpdateDialog) { Text("Cancel") }
            }
        )
    } else if (uiState.updateCheckResult != null) {
        val update = uiState.updateCheckResult!!
        AlertDialog(
            onDismissRequest = onDismissUpdateDialog,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = if (update.isUpdateAvailable) Color(0xFF0284C7) else Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (update.isUpdateAvailable) "Update Available!" else if (update.isError) "Update Status" else "Latest Version Installed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Current: v${update.currentVersion}", fontSize = 12.5.sp, color = subTextColor)
                        Text(text = "Latest: v${update.latestVersion}", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textColor)
                    }
                    Text(text = update.releaseTitle, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    Text(text = update.releaseNotes, fontSize = 12.5.sp, color = subTextColor)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AppUpdateManager.startApkDownload(context, update.downloadUrl)
                        onDismissUpdateDialog()
                    }
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Download & Install APK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUpdateDialog) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun HourlyStripCard(
    hour: HourlyWeather,
    isNight: Boolean,
    temperatureUnit: TemperatureUnit
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
                text = "${TemperatureFormatter.toDisplayInt(hour.temperature, temperatureUnit)}°",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isNight) Color.White else Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "RF ${TemperatureFormatter.toDisplayInt(hour.realFeel, temperatureUnit)}°",
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
    isToday: Boolean,
    temperatureUnit: TemperatureUnit
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

        // Icon and Rain probability + mls
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

        // Temperatures: Min / Max
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${TemperatureFormatter.toDisplayInt(day.tempMin, temperatureUnit)}°",
                fontSize = 14.sp,
                color = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${TemperatureFormatter.toDisplayInt(day.tempMax, temperatureUnit)}°",
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
