package com.example.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocationEntity
import com.example.data.local.WeatherDatabase
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SheepWeatherWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set default cancelled result in case user backs out
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setContent {
            MyApplicationTheme {
                WidgetConfigureScreen(
                    appWidgetId = appWidgetId,
                    onSave = { config ->
                        val context = this@SheepWeatherWidgetConfigureActivity
                        val effectiveId = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                            appWidgetId
                        } else {
                            // If opened directly from in-app settings, configure all or default
                            1
                        }

                        WidgetPreferences.saveConfig(context, effectiveId, config)
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        if (effectiveId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                            SheepWeatherWidgetProvider.updateAppWidget(context, appWidgetManager, effectiveId)
                        } else {
                            SheepWeatherWidgetProvider.refreshAllWidgets(context)
                        }

                        val resultValue = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, effectiveId)
                        }
                        setResult(Activity.RESULT_OK, resultValue)
                        finish()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigureScreen(
    appWidgetId: Int,
    onSave: (WidgetConfig) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    // State initialized from existing widget config or defaults
    val initialConfig = remember {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            WidgetPreferences.loadConfig(context, appWidgetId)
        } else {
            WidgetConfig()
        }
    }

    var selectedColor by remember { mutableStateOf(initialConfig.backgroundColor) }
    var transparency by remember { mutableFloatStateOf(initialConfig.transparencyPercent.toFloat()) }
    var matchDarkMode by remember { mutableStateOf(initialConfig.matchDarkMode) }
    var selectedLocationId by remember { mutableLongStateOf(initialConfig.locationId) }

    var availableLocations by remember { mutableStateOf<List<LocationEntity>>(emptyList()) }
    var selectedLocationName by remember { mutableStateOf("London") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = WeatherDatabase.getInstance(context)
            val list = db.weatherDao().getAllLocationsList()
            availableLocations = list
            val currentSelected = if (selectedLocationId > 0) {
                list.firstOrNull { it.id == selectedLocationId }
            } else {
                list.firstOrNull { it.isSelected } ?: list.firstOrNull()
            }
            if (currentSelected != null) {
                selectedLocationName = currentSelected.name
                if (selectedLocationId <= 0) {
                    selectedLocationId = currentSelected.id
                }
            }
        }
    }

    // Samsung UI Colors
    val screenBg = if (systemDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (systemDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val textColor = if (systemDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (systemDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Resolved preview state
    val previewIsDark = if (matchDarkMode) systemDark else (selectedColor == WidgetConfig.COLOR_BLACK)
    val previewAlpha = (((100 - transparency.toInt()) / 100f) * 255).toInt().coerceIn(0, 255)
    val previewRgb = if (previewIsDark) Color(0xFF111827) else Color(0xFFFFFFFF)
    val previewUseBrightText = previewIsDark || transparency >= 60f
    val previewTextColor = if (previewUseBrightText) Color.White else Color(0xFF0F172A)
    val previewSubTextColor = if (previewUseBrightText) Color(0xFFD1D5DB) else Color(0xFF475569)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = screenBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Samsung One UI Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Widget settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBg
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // 1. LIVE WIDGET PREVIEW CARD (Samsung style on textured backdrop)
                Text(
                    text = "Preview",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = subTextColor,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            // Simulated desktop/wallpaper gradient so transparency is visually obvious
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899))
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Widget Mockup Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(previewRgb.copy(alpha = previewAlpha / 255f))
                            .border(
                                width = 1.dp,
                                color = if (previewIsDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Clock, Date, Location
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(
                                    text = "10:24",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = previewTextColor
                                )
                                Text(
                                    text = "Mon, Oct 12",
                                    fontSize = 12.sp,
                                    color = previewSubTextColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "📍 ", fontSize = 11.sp)
                                    Text(
                                        text = selectedLocationName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = previewTextColor
                                    )
                                }
                            }

                            // Right: Weather Icon, Temp, Condition, High/Low
                            Column(
                                modifier = Modifier.weight(1.2f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = "Weather Icon",
                                        tint = Color(0xFFFFA000),
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "21°",
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = previewTextColor
                                    )
                                }
                                Text(
                                    text = "Partly Cloudy",
                                    fontSize = 12.sp,
                                    color = previewTextColor
                                )
                                Text(
                                    text = "H: 23°  L: 14°",
                                    fontSize = 11.sp,
                                    color = previewSubTextColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. BACKGROUND COLOR (Samsung pill buttons)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Background color",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // White Choice
                            ColorOptionButton(
                                label = "White",
                                circleColor = Color.White,
                                isSelected = selectedColor == WidgetConfig.COLOR_WHITE && !matchDarkMode,
                                isDarkTheme = systemDark,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedColor = WidgetConfig.COLOR_WHITE
                                    matchDarkMode = false
                                }
                            )

                            // Black Choice
                            ColorOptionButton(
                                label = "Black",
                                circleColor = Color(0xFF111827),
                                isSelected = selectedColor == WidgetConfig.COLOR_BLACK && !matchDarkMode,
                                isDarkTheme = systemDark,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedColor = WidgetConfig.COLOR_BLACK
                                    matchDarkMode = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. MATCH WITH DARK MODE TOGGLE (Samsung signature feature)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { matchDarkMode = !matchDarkMode }
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Match with Dark mode",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Changes background according to day/night or system Dark mode",
                                fontSize = 13.sp,
                                color = subTextColor
                            )
                        }
                        Switch(
                            checked = matchDarkMode,
                            onCheckedChange = { matchDarkMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B82F6)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. BACKGROUND TRANSPARENCY SLIDER
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Background transparency",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "${transparency.toInt()}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = transparency,
                            onValueChange = { transparency = it },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6),
                                inactiveTrackColor = if (systemDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 30, 50, 100).forEach { preset ->
                                OutlinedButton(
                                    onClick = { transparency = preset.toFloat() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (transparency.toInt() == preset) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color.Transparent
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = if (transparency.toInt() == preset) Color(0xFF3B82F6) else if (systemDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    )
                                ) {
                                    Text(
                                        text = "$preset%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (transparency.toInt() == preset) Color(0xFF3B82F6) else textColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. LOCATION SELECTOR
                if (availableLocations.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Location",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            availableLocations.forEach { loc ->
                                val isSelected = loc.id == selectedLocationId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.12f) else Color.Transparent
                                        )
                                        .clickable {
                                            selectedLocationId = loc.id
                                            selectedLocationName = loc.name
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF3B82F6) else subTextColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = loc.name + if (loc.country.isNotBlank()) ", ${loc.country}" else "",
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF3B82F6) else textColor
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // BOTTOM BAR: Save and Cancel buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cardBg,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Cancel", fontSize = 15.sp, color = textColor)
                    }

                    Button(
                        onClick = {
                            val config = WidgetConfig(
                                backgroundColor = selectedColor,
                                transparencyPercent = transparency.toInt(),
                                matchDarkMode = matchDarkMode,
                                locationId = selectedLocationId
                            )
                            onSave(config)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        )
                    ) {
                        Text(text = "Save", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ColorOptionButton(
    label: String,
    circleColor: Color,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        Color(0xFF3B82F6)
    } else if (isDarkTheme) {
        Color(0xFF334155)
    } else {
        Color(0xFFE2E8F0)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(circleColor)
                    .border(1.dp, Color(0xFF94A3B8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = if (circleColor == Color.White) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
            )
        }
    }
}
