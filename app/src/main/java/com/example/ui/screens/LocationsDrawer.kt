package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocationEntity
import com.example.ui.components.AppCopyrightFooter
import com.example.util.AppUpdateManager

@Composable
fun LocationsDrawerContent(
    locations: List<LocationEntity>,
    selectedLocationId: Long,
    isNight: Boolean,
    onSelectLocation: (Long) -> Unit,
    onAddLocationByName: (String, String?) -> Unit,
    onDeleteLocation: (LocationEntity) -> Unit,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onUseCurrentLocation: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    lastUpdatedTime: String? = null,
    isFromCache: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    val bgColor = if (isNight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isNight) Color(0xFF1E293B) else Color.White
    val textColor = if (isNight) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(bgColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Drawer Header with Close Button and Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Locations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Slide-out Menu • v2.6.5",
                        fontSize = 11.sp,
                        color = subTextColor
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_locations_drawer")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close Drawer",
                    tint = subTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Data Saver & Last Updated Information card (Preserved in Drawer)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isNight) Color(0xFF1E293B) else Color(0xFFF1F5F9),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isNight) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DataSaverOn,
                            contentDescription = "Data Saver",
                            tint = if (isNight) Color(0xFF34D399) else Color(0xFF059669),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Data Saver Active",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isNight) Color(0xFFA7F3D0) else Color(0xFF065F46)
                        )
                    }
                    Text(
                        text = if (isFromCache) "Cached" else "Online",
                        fontSize = 10.5.sp,
                        color = subTextColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lastUpdatedTime?.let { "Last updated: $it" } ?: "Zero background refresh • Saved locally",
                    fontSize = 10.5.sp,
                    color = subTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Actions Group in Drawer:
        // 1. Settings Button (Prominent inside Location pop out)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isNight) Color(0xFF1E293B) else Color(0xFFF1F5F9),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isNight) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSettings() }
                .testTag("drawer_settings_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Settings & Preferences",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "AI features, °C/°F, Theme & Art style",
                        fontSize = 10.5.sp,
                        color = subTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Check for Updates & Auto-Download APK Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isNight) Color(0xFF0F2942) else Color(0xFFE0F2FE),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isNight) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color(0xFF0284C7).copy(alpha = 0.25f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckForUpdates() }
                .testTag("drawer_check_updates_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Check for Updates",
                    tint = if (isNight) Color(0xFF38BDF8) else Color(0xFF0284C7),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Check Updates & Download APK",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Auto-download latest release from GitHub",
                        fontSize = 10.5.sp,
                        color = subTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Use Current Location GPS Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isNight) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFEBF5FF),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUseCurrentLocation() }
                .testTag("drawer_use_current_location")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Use Current Location",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = "One-shot GPS detection",
                        fontSize = 10.5.sp,
                        color = subTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Data Saver status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isNight) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DataSaverOn,
                contentDescription = "Data Saver",
                tint = if (isNight) Color(0xFF34D399) else Color(0xFF059669),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Zero background refresh. Cached in local DB.",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isNight) Color(0xFFA7F3D0) else Color(0xFF065F46)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SAVED CITIES (${locations.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = subTextColor,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        // Locations List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(locations, key = { it.id }) { loc ->
                val isSelected = loc.id == selectedLocationId || loc.isSelected
                Surface(
                    color = if (isSelected) {
                        if (isNight) Color(0xFF1E3A8A) else Color(0xFFE0F2FE)
                    } else cardBg,
                    shape = RoundedCornerShape(14.dp),
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7)
                        )
                    } else {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectLocation(loc.id)
                        }
                        .testTag("location_item_${loc.name}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF0284C7) else subTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = loc.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                                Text(
                                    text = loc.country,
                                    fontSize = 11.sp,
                                    color = subTextColor
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            if (locations.size > 1) {
                                IconButton(
                                    onClick = { onDeleteLocation(loc) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("delete_loc_${loc.name}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Add New Location Action Button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_location_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isNight) Color(0xFF2563EB) else Color(0xFF0284C7)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Add City by Name", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mandatory Copyright at the bottom of the drawer
        AppCopyrightFooter(isNight = isNight)
    }

    if (showAddDialog) {
        AddCityDrawerDialog(
            isNight = isNight,
            onAdd = { name, country ->
                onAddLocationByName(name, country)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun AddCityDrawerDialog(
    isNight: Boolean,
    onAdd: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var cityName by remember { mutableStateOf("") }
    var countryName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val popularSuggestions = listOf(
        "Tokyo" to "Japan",
        "Paris" to "France",
        "London" to "United Kingdom",
        "Rome" to "Italy",
        "Berlin" to "Germany",
        "Sydney" to "Australia",
        "Toronto" to "Canada",
        "Auckland" to "New Zealand"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add City", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "No coordinates needed. Simply enter any city name worldwide:",
                    fontSize = 12.sp,
                    color = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)
                )

                Text(
                    text = "Quick suggestions:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularSuggestions.take(3).forEach { (city, country) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isNight) Color(0xFF334155) else Color(0xFFE2E8F0),
                            modifier = Modifier.clickable {
                                cityName = city
                                countryName = country
                            }
                        ) {
                            Text(
                                text = city,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = cityName,
                    onValueChange = {
                        cityName = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("City Name (e.g. Paris, Tokyo, Berlin)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_city_name")
                )
                OutlinedTextField(
                    value = countryName,
                    onValueChange = { countryName = it },
                    label = { Text("Country (Optional, e.g. France)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_country_name")
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cityName.isBlank()) {
                        errorMessage = "Please enter a city name"
                    } else {
                        onAdd(cityName.trim(), countryName.trim().ifEmpty { null })
                    }
                },
                modifier = Modifier.testTag("confirm_add_location")
            ) {
                Text("Add City")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
