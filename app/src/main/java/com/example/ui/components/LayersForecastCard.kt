package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LayersForecast

@Composable
fun LayersForecastCard(
    layersForecast: LayersForecast,
    isNight: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isNight) Color(0xFF16203A) else Color.White
    val cardBorder = if (isNight) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val textColor = if (isNight) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val subTextColor = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)

    Surface(
        color = cardBg,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
            .testTag("layers_forecast_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title and "Next Few Hours" badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👕",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "Layers & Outfit Forecast",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                // Next Few Hours Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = if (isNight) Color(0xFF1E2D52) else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isNight) Color(0xFF60A5FA) else Color(0xFF2563EB),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Next Few Hours",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNight) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Dual Hero Blocks:
            // 1) Amount of Layers badge (T-shirt emoji with number)
            // 2) Umbrella Forecast (Umbrella status, including "Too windy for umbrella")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Layer Hero Box
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isNight) Color(0xFF1E2D52) else Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isNight) Color(0xFF3B82F6).copy(alpha = 0.35f) else Color(0xFFBFDBFE)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("layers_badge_container")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AMOUNT OF LAYERS",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNight) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // T-shirt emoji with number
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "👕",
                                fontSize = 34.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${layersForecast.layerCount}",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                letterSpacing = (-1).sp
                            )
                        }
                        Text(
                            text = if (layersForecast.layerCount == 1) "1 Layer" else "${layersForecast.layerCount} Layers",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }

                // Umbrella Hero Box: handles "Too windy for umbrella", "Umbrella needed", or "No umbrella needed"
                val isWindyAndRainy = layersForecast.isTooWindyForUmbrella && layersForecast.isUmbrellaRequired
                val umbrellaBoxBg = when {
                    isWindyAndRainy -> if (isNight) Color(0xFF451A03).copy(alpha = 0.7f) else Color(0xFFFEF3C7)
                    layersForecast.isUmbrellaRequired -> if (isNight) Color(0xFF312E81).copy(alpha = 0.6f) else Color(0xFFE0E7FF)
                    else -> if (isNight) Color(0xFF1E2D52) else Color(0xFFF0FDF4)
                }
                val umbrellaBorderColor = when {
                    isWindyAndRainy -> if (isNight) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color(0xFFFDE68A)
                    layersForecast.isUmbrellaRequired -> if (isNight) Color(0xFF818CF8).copy(alpha = 0.5f) else Color(0xFFC7D2FE)
                    else -> if (isNight) Color(0xFF10B981).copy(alpha = 0.35f) else Color(0xFFBBF7D0)
                }
                val umbrellaTagColor = when {
                    isWindyAndRainy -> if (isNight) Color(0xFFFBBF24) else Color(0xFFB45309)
                    layersForecast.isUmbrellaRequired -> if (isNight) Color(0xFFA5B4FC) else Color(0xFF4338CA)
                    else -> if (isNight) Color(0xFF6EE7B7) else Color(0xFF15803D)
                }
                val umbrellaTagText = when {
                    isWindyAndRainy -> "⚠️ TOO WINDY"
                    layersForecast.isUmbrellaRequired -> "UMBRELLA NEEDED"
                    else -> "DRY CONDITIONS"
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = umbrellaBoxBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, umbrellaBorderColor),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("umbrella_badge_container")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = umbrellaTagText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = umbrellaTagColor,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Umbrella emoji with status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = layersForecast.umbrellaEmoji,
                                fontSize = 34.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = layersForecast.umbrellaStatus,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = when {
                                isWindyAndRainy -> if (isNight) Color(0xFFFDE68A) else Color(0xFF92400E)
                                layersForecast.isUmbrellaRequired -> if (isNight) Color(0xFFC7D2FE) else Color(0xFF3730A3)
                                else -> if (isNight) Color(0xFFA7F3D0) else Color(0xFF166534)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Informational Summary Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isNight) Color(0xFF1E2D52).copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = layersForecast.layerDescription,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = layersForecast.umbrellaNote,
                        fontSize = 12.sp,
                        color = subTextColor
                    )

                    if (layersForecast.isTooWindyForUmbrella) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = "Wind Alert",
                                tint = if (isNight) Color(0xFFFBBF24) else Color(0xFFD97706),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Wind speeds reaching ${layersForecast.maxWindSpeed.toInt()} km/h over the next few hours",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNight) Color(0xFFFDE68A) else Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Clothing items recommendation chips reflecting the next few hours
            val displayedItems = if (layersForecast.isTooWindyForUmbrella && layersForecast.isUmbrellaRequired) {
                listOf("Hooded Raincoat") + layersForecast.clothingItems
            } else {
                layersForecast.clothingItems
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayedItems.forEach { item ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isNight) Color(0xFF1E2D52) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (isNight) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "• $item",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isNight) Color(0xFFE2E8F0) else Color(0xFF334155)
                        )
                    }
                }
            }
        }
    }
}
