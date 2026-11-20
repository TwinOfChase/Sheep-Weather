package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import com.example.util.DevicePerformanceHelper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ExpandLess
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.ui.util.AdaptiveUiSpec
import com.example.ui.util.computeAdaptiveUiSpec
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
    val listState = rememberLazyListState()
    val isScrolling = listState.isScrollInProgress
    val isLowEnd = remember { DevicePerformanceHelper.isLowEndDevice(context) }

    val weather = uiState.weatherInfo
    val isNight = uiState.isNightEffective
    val style = uiState.appStyle
    val tempUnit = uiState.temperatureUnit

    // Stale weather check: more than 1 hour (3600s) since last refreshed
    val isMoreThanOneHourSinceRefresh = remember(weather?.lastUpdatedMillis) {
        val lastMillis = weather?.lastUpdatedMillis ?: 0L
        if (lastMillis <= 0L) true else (System.currentTimeMillis() - lastMillis) >= 3_600_000L
    }

    // High contrast typography colors
    val textColor = if (isNight) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isNight) Color(0xFF94A3B8) else Color(0xFF475569)
    val cardBg = if (isNight) Color(0xFF14203B).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.92f)
    val cardBorder = if (isNight) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val adaptive = computeAdaptiveUiSpec(maxWidth, maxHeight)
        val fs = adaptive.fontScale

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = if (isNight) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.width(adaptive.drawerWidth)
                ) {
                    LocationsDrawerContent(
                        locations = uiState.locations,
                        selectedLocationId = uiState.selectedLocation?.id ?: 0L,
                        isNight = isNight,
                        lastUpdatedTime = weather?.current?.time,
                        isFromCache = weather?.isFromCache ?: false,
                        adaptive = adaptive,
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
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Two Designs Background: Cartoon Sheep vs Realistic Sheep
                when (style) {
                    AppStyle.CARTOON_SHEEP -> {
                        CartoonSheepCanvas(
                            isNight = isNight,
                            isScrolling = isScrolling,
                            isLowEndDevice = isLowEnd,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { clip = false }
                        )
                    }
                    AppStyle.REALISTIC_SHEEP -> {
                        RealisticWeatherBackdrop(
                            isNight = isNight,
                            moonInfo = uiState.moonInfo,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { clip = false }
                        )
                    }
                }

                // 2. Foreground scrollable content with maximum readability
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        horizontal = adaptive.horizontalPadding,
                        vertical = if (adaptive.isTabletOrUnfolded) 16.dp else 10.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(adaptive.verticalSpacing)
                ) {
                // Top Navigation & Control Header (Cleaned up: Only Location Pill and Refresh Button)
                item(key = "top_control_header") {
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
                                modifier = Modifier.padding(
                                    horizontal = (14f * fs).dp,
                                    vertical = (8f * fs).dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                                    modifier = Modifier.size((18f * fs).dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = weather?.locationName ?: "Select City",
                                    fontSize = (15f * fs).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Open",
                                    tint = subTextColor,
                                    modifier = Modifier.size((18f * fs).dp)
                                )
                            }
                        }

                        // Refresh button (turns green when > 1 hour since last refresh)
                        val refreshTint = if (isMoreThanOneHourSinceRefresh) Color(0xFF22C55E) else textColor
                        val refreshBorder = if (isMoreThanOneHourSinceRefresh) Color(0xFF22C55E) else cardBorder
                        val refreshBg = if (isMoreThanOneHourSinceRefresh) {
                            if (isNight) Color(0xFF14532D).copy(alpha = 0.55f) else Color(0xFFDCFCE7)
                        } else cardBg

                        Surface(
                            color = refreshBg,
                            shape = CircleShape,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clickable { onRefresh() }
                                .border(1.5.dp, refreshBorder, CircleShape)
                                .testTag("refresh_button")
                        ) {
                            Box(modifier = Modifier.padding((8f * fs).dp)) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size((20f * fs).dp),
                                        strokeWidth = 2.dp,
                                        color = if (isMoreThanOneHourSinceRefresh) Color(0xFF22C55E) else if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = refreshTint,
                                        modifier = Modifier.size((20f * fs).dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Viewing area for mascot & backdrop (moon phase displayed safely below location and reload controls)
                item(key = "mascot_viewing_space") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(adaptive.viewingSpaceHeight)
                            .testTag("background_viewing_space")
                    ) {
                        if (isNight) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 4.dp)
                                    .testTag("moon_phase_badge")
                            ) {
                                Text(
                                    text = "🌙 ${uiState.moonInfo.phaseName} • ${uiState.moonInfo.illuminationPercent}%",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = (12f * fs).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = (12f * fs).dp, vertical = (5f * fs).dp)
                                )
                            }
                        }
                    }
                }

                // Primary Temperature Hero Card (Combined Actual and Real Feel)
                if (weather != null) {
                    item(key = "temperature_hero_card") {
                        Surface(
                            color = cardBg,
                            shape = RoundedCornerShape(adaptive.cardCornerRadius),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorder, RoundedCornerShape(adaptive.cardCornerRadius))
                                .testTag("temperature_hero_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(adaptive.cardPadding)
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
                                            modifier = Modifier.size((22f * fs).dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Temperature",
                                            fontSize = (18f * fs).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ConditionIcon(
                                            weatherCode = weather.current.weatherCode,
                                            isDay = !isNight,
                                            size = (36f * fs).dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = weather.current.conditionDescription,
                                            fontSize = (13.5f * fs).sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = subTextColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (adaptive.isTabletOrUnfolded) 16.dp else 14.dp))

                                // Combined Temperature Box displaying both Actual and Real Feel side-by-side
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = if (isNight) Color(0xFF1E2D52) else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .padding(vertical = (12f * fs).dp, horizontal = (16f * fs).dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Actual Temperature Display
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "ACTUAL",
                                            fontSize = (11f * fs).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = subTextColor,
                                            letterSpacing = 0.6.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = TemperatureFormatter.format(weather.current.temperature, tempUnit),
                                            fontSize = (36f * fs).sp,
                                            fontWeight = FontWeight.Black,
                                            color = textColor,
                                            letterSpacing = (-1.2).sp
                                        )
                                    }

                                    // Subtle divider
                                    Box(
                                        modifier = Modifier
                                            .height((38f * fs).dp)
                                            .width(1.dp)
                                            .background(if (isNight) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f))
                                    )

                                    // Real Feel Display
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "REAL FEEL",
                                            fontSize = (11f * fs).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNight) Color(0xFFFFB74D) else Color(0xFFD97706),
                                            letterSpacing = 0.6.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = TemperatureFormatter.format(weather.current.realFeel, tempUnit),
                                            fontSize = (36f * fs).sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isNight) Color(0xFFFFB74D) else Color(0xFFD97706),
                                            letterSpacing = (-1.2).sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (adaptive.isTabletOrUnfolded) 16.dp else 14.dp))

                                // Grid of secondary metrics: Wind, Humidity, UV, Rain
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    QuickMetricItem(
                                        icon = Icons.Default.Air,
                                        label = "Wind",
                                        value = "${weather.current.windSpeed.toInt()} km/h",
                                        isNight = isNight,
                                        fontScale = fs
                                    )
                                    QuickMetricItem(
                                        icon = Icons.Default.WaterDrop,
                                        label = "Humidity",
                                        value = "${weather.current.humidity}%",
                                        isNight = isNight,
                                        fontScale = fs
                                    )
                                    QuickMetricItem(
                                        icon = Icons.Default.WbSunny,
                                        label = "UV Index",
                                        value = "%.1f".format(weather.current.uvIndex),
                                        isNight = isNight,
                                        fontScale = fs
                                    )
                                    // Rain metric with probability % and millimeters
                                    QuickMetricItem(
                                        icon = Icons.Default.Grain,
                                        label = "Rain",
                                        value = "${weather.hourly.firstOrNull()?.precipitationProbability ?: 10}% • ${"%.1f".format(weather.hourly.firstOrNull()?.precipitationMm ?: weather.current.precipitationMm)} mm",
                                        isNight = isNight,
                                        fontScale = fs
                                    )
                                }
                            }
                        }
                    }
                }

                // Sunset & Sunrise Section
                if (weather != null) {
                    item(key = "sunrise_sunset_card") {
                        Surface(
                            color = cardBg,
                            shape = RoundedCornerShape(adaptive.cardCornerRadius),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorder, RoundedCornerShape(adaptive.cardCornerRadius))
                                .testTag("sunrise_sunset_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(adaptive.cardPadding)
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
                                            modifier = Modifier.size((20f * fs).dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Sunrise & Sunset",
                                            fontSize = (17f * fs).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }
                                    Text(
                                        text = "Local Solar Times",
                                        fontSize = (11.5f * fs).sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = subTextColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(if (adaptive.isTabletOrUnfolded) 16.dp else 12.dp)
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
                                            modifier = Modifier.padding((14f * fs).dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size((38f * fs).dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFEF3C7)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.WbSunny,
                                                    contentDescription = "Sunrise",
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size((22f * fs).dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Sunrise",
                                                    fontSize = (11f * fs).sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = subTextColor
                                                )
                                                Text(
                                                    text = weather.current.sunrise,
                                                    fontSize = (15f * fs).sp,
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
                                            modifier = Modifier.padding((14f * fs).dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size((38f * fs).dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFFEDD5)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.NightsStay,
                                                    contentDescription = "Sunset",
                                                    tint = Color(0xFFEA580C),
                                                    modifier = Modifier.size((22f * fs).dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Sunset",
                                                    fontSize = (11f * fs).sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = subTextColor
                                                )
                                                Text(
                                                    text = weather.current.sunset,
                                                    fontSize = (15f * fs).sp,
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
                    item(key = "outfit_layers_card") {
                        val layers = uiState.layersForecast ?: remember(weather) {
                            LayersForecastCalculator.calculate(weather.hourly, weather.current.temperature)
                        }
                        LayersForecastCard(
                            layersForecast = layers,
                            isNight = isNight,
                            adaptive = adaptive
                        )
                    }
                }

                // Hourly Forecast Strip
                if (weather != null) {
                    item(key = "hourly_forecast_strip") {
                        Surface(
                            color = cardBg,
                            shape = RoundedCornerShape(adaptive.cardCornerRadius),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorder, RoundedCornerShape(adaptive.cardCornerRadius))
                                .clickable { onOpenHourlyDetail() }
                                .testTag("hourly_forecast_strip")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = if (adaptive.isTabletOrUnfolded) 20.dp else 16.dp)
                            ) {
                                // Section header with tap hint
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = adaptive.cardPadding),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Hourly Forecast",
                                        fontSize = (17f * fs).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Deep-Dive",
                                            fontSize = (12f * fs).sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Open Hourly Detail",
                                            tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                                            modifier = Modifier.size((16f * fs).dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Horizontal strip of hours
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = if (adaptive.isTabletOrUnfolded) 18.dp else 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(if (adaptive.isTabletOrUnfolded) 12.dp else 10.dp)
                                ) {
                                    items(
                                        items = weather.hourly.take(24),
                                        key = { it.timeIso }
                                    ) { hour ->
                                        HourlyStripCard(
                                            hour = hour,
                                            isNight = isNight,
                                            temperatureUnit = tempUnit,
                                            fontScale = fs
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Overview Section with Gemini Logo (Gated behind isAiEnabled)
                if (weather != null && uiState.isAiEnabled) {
                    item(key = "ai_overview_card") {
                        AiOverviewCard(
                            overview = uiState.aiOverview,
                            isLoading = uiState.isAiLoading,
                            isNight = isNight,
                            onRefreshAi = onRefreshAi,
                            adaptive = adaptive
                        )
                    }
                }

                // 7-Day Forecast Section (Shows 3 days with a button to expand to 7)
                if (weather != null) {
                    item(key = "seven_day_forecast_card") {
                        var isForecastExpanded by remember { mutableStateOf(false) }
                        val displayedDays = if (isForecastExpanded) {
                            weather.daily.take(7)
                        } else {
                            weather.daily.take(3)
                        }

                        Surface(
                            color = cardBg,
                            shape = RoundedCornerShape(adaptive.cardCornerRadius),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorder, RoundedCornerShape(adaptive.cardCornerRadius))
                                .testTag("seven_day_forecast_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(adaptive.cardPadding)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "7-Day Forecast",
                                        fontSize = (17f * fs).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text(
                                        text = if (isForecastExpanded) "Showing 7 days" else "Showing 3 days",
                                        fontSize = (12f * fs).sp,
                                        fontWeight = FontWeight.Medium,
                                        color = subTextColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))

                                displayedDays.forEachIndexed { index, day ->
                                    DailyForecastRow(
                                        day = day,
                                        isNight = isNight,
                                        isToday = index == 0,
                                        temperatureUnit = tempUnit,
                                        fontScale = fs
                                    )
                                    if (index < displayedDays.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = (8f * fs).dp)
                                                .height(0.6.dp)
                                                .background(if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
                                        )
                                    }
                                }

                                if (weather.daily.size > 3) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isNight) Color(0xFF1E2D52) else Color(0xFFF1F5F9),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isNight) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isForecastExpanded = !isForecastExpanded }
                                            .testTag("expand_forecast_button")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = (10f * fs).dp, horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isForecastExpanded) "Show Less" else "Expand to 7-Day Forecast",
                                                fontSize = (13.5f * fs).sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isNight) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = if (isForecastExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = if (isForecastExpanded) "Show Less" else "Expand to 7-Day Forecast",
                                                tint = if (isNight) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                                                modifier = Modifier.size((18f * fs).dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom space and Mandatory Copyright
                item(key = "copyright_footer") {
                    AppCopyrightFooter(isNight = isNight, fontScale = fs)
                    Spacer(modifier = Modifier.height(20.dp))
                }
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
    temperatureUnit: TemperatureUnit,
    fontScale: Float = 1.0f
) {
    Surface(
        color = if (isNight) Color(0xFF1E2D52) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.width((78f * fontScale).dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = (12f * fontScale).dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hour.displayHour,
                fontSize = (12f * fontScale).sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isNight) Color(0xFFE2E8F0) else Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ConditionIcon(
                weatherCode = hour.weatherCode,
                isDay = hour.isDay,
                size = (28f * fontScale).dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${TemperatureFormatter.toDisplayInt(hour.temperature, temperatureUnit)}°",
                fontSize = (16f * fontScale).sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isNight) Color.White else Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "RF ${TemperatureFormatter.toDisplayInt(hour.realFeel, temperatureUnit)}°",
                fontSize = (12.5f * fontScale).sp,
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
    temperatureUnit: TemperatureUnit,
    fontScale: Float = 1.0f
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Day name & date
        Column(modifier = Modifier.width((80f * fontScale).dp)) {
            Text(
                text = day.dayOfWeek,
                fontSize = (15f * fontScale).sp,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isToday) {
                    if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                } else if (isNight) Color.White else Color(0xFF0F172A)
            )
            Text(
                text = day.formattedDate,
                fontSize = (11f * fontScale).sp,
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
                size = (26f * fontScale).dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (day.precipitationProbability > 20) {
                Text(
                    text = "${day.precipitationProbability}% (${"%.1f".format(day.precipitationMm)} mm)",
                    fontSize = (12f * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isNight) Color(0xFF67E8F9) else Color(0xFF0284C7)
                )
            } else {
                Text(
                    text = day.conditionDescription,
                    fontSize = (12f * fontScale).sp,
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
                fontSize = (14f * fontScale).sp,
                color = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${TemperatureFormatter.toDisplayInt(day.tempMax, temperatureUnit)}°",
                fontSize = (15f * fontScale).sp,
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
    isNight: Boolean,
    fontScale: Float = 1.0f
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
            modifier = Modifier.size((18f * fontScale).dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = (13f * fontScale).sp,
            fontWeight = FontWeight.Bold,
            color = if (isNight) Color.White else Color(0xFF0F172A)
        )
        Text(
            text = label,
            fontSize = (10f * fontScale).sp,
            color = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)
        )
    }
}
