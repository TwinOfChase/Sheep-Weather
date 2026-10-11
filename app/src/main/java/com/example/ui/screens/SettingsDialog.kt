package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppStyle
import com.example.ui.ThemeMode
import com.example.ui.components.AppCopyrightFooter
import com.example.ui.components.GeminiSparkleLogo
import com.example.util.AppUpdateManager
import com.example.util.TemperatureUnit

@Composable
fun SettingsDialog(
    isNight: Boolean,
    temperatureUnit: TemperatureUnit,
    isAiEnabled: Boolean,
    themeMode: ThemeMode,
    appStyle: AppStyle,
    isDataTrackingEnabled: Boolean,
    isUseCurrentLocationEnabled: Boolean,
    onSetTemperatureUnit: (TemperatureUnit) -> Unit,
    onSetAiEnabled: (Boolean) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetAppStyle: (AppStyle) -> Unit,
    onToggleDataTracking: (Boolean) -> Unit,
    onToggleUseCurrentLocation: (Boolean) -> Unit,
    onDetectCurrentLocation: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onToggleUseCurrentLocation(true)
            onDetectCurrentLocation()
            Toast.makeText(context, "Location permission granted. Detecting location...", Toast.LENGTH_SHORT).show()
        } else {
            onToggleUseCurrentLocation(false)
            Toast.makeText(context, "Location permission was denied", Toast.LENGTH_SHORT).show()
        }
    }

    val dialogBg = if (isNight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isNight) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val textColor = if (isNight) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("settings_dialog_surface"),
            shape = RoundedCornerShape(24.dp),
            color = dialogBg,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isNight) Color(0xFF2563EB) else Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Settings",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "Preferences, Units & Style",
                                fontSize = 12.sp,
                                color = subTextColor
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = subTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Gemini AI Features Toggle
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GeminiSparkleLogo(size = 20.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI Features",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI Weather Overview",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                Text(
                                    text = if (isAiEnabled) {
                                        "Generates smart daily briefings using Google Gemini. Card visible on main screen."
                                    } else {
                                        "AI disabled. Overview card hidden and zero AI network requests."
                                    },
                                    fontSize = 12.sp,
                                    color = subTextColor
                                )
                            }
                            Switch(
                                checked = isAiEnabled,
                                onCheckedChange = onSetAiEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = if (isNight) Color(0xFF9060EB) else Color(0xFF7C3AED)
                                ),
                                modifier = Modifier.testTag("toggle_ai_features")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Temperature Unit (°C vs °F)
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = if (isNight) Color(0xFFFF9F1C) else Color(0xFFEA580C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Temperature Unit",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Choose temperature scale across all screens, cards, and hourly breakdowns:",
                            fontSize = 12.sp,
                            color = subTextColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Celsius Option
                            val isCelsius = temperatureUnit == TemperatureUnit.CELSIUS
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCelsius) {
                                    if (isNight) Color(0xFF1E3A8A) else Color(0xFFE0F2FE)
                                } else cardBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isCelsius) (if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)) else cardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetTemperatureUnit(TemperatureUnit.CELSIUS) }
                                    .testTag("select_unit_celsius")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isCelsius) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "Celsius (°C)",
                                        fontWeight = if (isCelsius) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = textColor
                                    )
                                }
                            }

                            // Fahrenheit Option
                            val isFahrenheit = temperatureUnit == TemperatureUnit.FAHRENHEIT
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFahrenheit) {
                                    if (isNight) Color(0xFF1E3A8A) else Color(0xFFE0F2FE)
                                } else cardBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isFahrenheit) (if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)) else cardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetTemperatureUnit(TemperatureUnit.FAHRENHEIT) }
                                    .testTag("select_unit_fahrenheit")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isFahrenheit) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "Fahrenheit (°F)",
                                        fontWeight = if (isFahrenheit) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Day & Night Theme Mode (Auto, Light, Dark)
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Day & Night Theme",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (themeMode) {
                                ThemeMode.AUTO -> "Auto: Switches with local sunrise & sunset times automatically."
                                ThemeMode.LIGHT -> "Fixed Light: Bright daylight sheep pasture theme."
                                ThemeMode.DARK -> "Fixed Dark: Night sky with moon and sleeping sheep theme."
                            },
                            fontSize = 12.sp,
                            color = subTextColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Auto Option
                            val isAuto = themeMode == ThemeMode.AUTO
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAuto) {
                                    if (isNight) Color(0xFF1E3A8A) else Color(0xFFE0F2FE)
                                } else cardBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isAuto) (if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)) else cardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetThemeMode(ThemeMode.AUTO) }
                                    .testTag("select_theme_auto")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = if (isAuto) (if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)) else subTextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Auto",
                                        fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = textColor
                                    )
                                }
                            }

                            // Light Option
                            val isLight = themeMode == ThemeMode.LIGHT
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isLight) {
                                    if (isNight) Color(0xFF1E3A8A) else Color(0xFFE0F2FE)
                                } else cardBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isLight) (if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)) else cardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetThemeMode(ThemeMode.LIGHT) }
                                    .testTag("select_theme_light")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Light",
                                        fontWeight = if (isLight) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = textColor
                                    )
                                }
                            }

                            // Dark Option
                            val isDark = themeMode == ThemeMode.DARK
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) {
                                    if (isNight) Color(0xFF1E3A8A) else Color(0xFFE0F2FE)
                                } else cardBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isDark) (if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)) else cardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetThemeMode(ThemeMode.DARK) }
                                    .testTag("select_theme_dark")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightsStay,
                                        contentDescription = null,
                                        tint = Color(0xFF93C5FD),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Dark",
                                        fontWeight = if (isDark) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 4: App Visual Style (Cartoon vs Photo)
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🐑",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "App Visual Style",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toggle between cute cartoon sheep mascot canvas and realistic photographic scenery:",
                            fontSize = 12.sp,
                            color = subTextColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isCartoon = appStyle == AppStyle.CARTOON_SHEEP
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCartoon) {
                                    if (isNight) Color(0xFF1E3A8A) else Color(0xFFE0F2FE)
                                } else cardBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isCartoon) (if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)) else cardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetAppStyle(AppStyle.CARTOON_SHEEP) }
                                    .testTag("select_style_cartoon")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🐑", fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Cartoon Sheep",
                                        fontWeight = if (isCartoon) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = textColor
                                    )
                                }
                            }

                            val isPhoto = appStyle == AppStyle.REALISTIC_SHEEP
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isPhoto) {
                                    if (isNight) Color(0xFF1E3A8A) else Color(0xFFE0F2FE)
                                } else cardBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (isPhoto) (if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)) else cardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetAppStyle(AppStyle.REALISTIC_SHEEP) }
                                    .testTag("select_style_photo")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("📷", fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Realistic Photo",
                                        fontWeight = if (isPhoto) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 5: App Updates & APK Downloader
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = if (isNight) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Updates & APK Download",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Auto-download the latest compiled APK package or check GitHub releases.",
                            fontSize = 12.sp,
                            color = subTextColor
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // One-tap Auto Download APK button
                        Button(
                            onClick = {
                                AppUpdateManager.startApkDownload(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_auto_download_apk"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNight) Color(0xFF0284C7) else Color(0xFF0284C7)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download Latest APK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Check for updates button
                        OutlinedButton(
                            onClick = onCheckForUpdates,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_check_for_updates"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Check for Updates (GitHub)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 6: Current Location
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Current Location",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Current Location",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                Text(
                                    text = if (isUseCurrentLocationEnabled) {
                                        "Detects device city on demand. Zero background refresh."
                                    } else {
                                        "Disabled. Weather uses your saved favourite cities."
                                    },
                                    fontSize = 12.sp,
                                    color = subTextColor
                                )
                            }
                            Switch(
                                checked = isUseCurrentLocationEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    } else {
                                        onToggleUseCurrentLocation(false)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = if (isNight) Color(0xFF2563EB) else Color(0xFF0284C7)
                                ),
                                modifier = Modifier.testTag("toggle_current_location")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_detect_current_location"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Detect Location Now (One-Shot)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 7: Privacy & Data Tracking
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isNight) Color(0xFF34D399) else Color(0xFF059669),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Privacy & Data Usage",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Data Tracking",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                Text(
                                    text = if (isDataTrackingEnabled) {
                                        "Minimal anonymous caching telemetry enabled"
                                    } else {
                                        "Data tracking disabled. Zero telemetry collected."
                                    },
                                    fontSize = 12.sp,
                                    color = if (!isDataTrackingEnabled) {
                                        if (isNight) Color(0xFF34D399) else Color(0xFF059669)
                                    } else subTextColor
                                )
                            }
                            Switch(
                                checked = isDataTrackingEnabled,
                                onCheckedChange = onToggleDataTracking,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = if (isNight) Color(0xFF059669) else Color(0xFF10B981)
                                ),
                                modifier = Modifier.testTag("toggle_data_tracking")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 8: Support & Feedback
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (isNight) Color(0xFFFBBF24) else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Support & Feedback",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Have questions, suggestions, or bug reports? Contact us directly.",
                            fontSize = 12.sp,
                            color = subTextColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:studioofchase@gmail.com?subject=Sheep%20Weather%20App%20Feedback")
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Contact: studioofchase@gmail.com", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("feedback_contact_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNight) Color(0xFF2563EB) else Color(0xFF0284C7)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Feedback / Contact Us", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "studioofchase@gmail.com",
                            fontSize = 11.5.sp,
                            color = subTextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 9: App Version & Notice
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNight) Color(0xFF1E293B).copy(alpha = 0.7f) else Color(0xFFF1F5F9)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sheep Weather v2.5",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This app may be changing regularly to solve bugs and add new features",
                            fontSize = 12.sp,
                            color = subTextColor,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory Copyright at the bottom
                AppCopyrightFooter(isNight = isNight)

                Spacer(modifier = Modifier.height(8.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_settings_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNight) Color(0xFF334155) else Color(0xFFE2E8F0),
                        contentColor = textColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
