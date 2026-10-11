package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AiWeatherOverview

/**
 * Authentic 4-pointed Google Gemini Sparkle Logo with gradient fill.
 */
@Composable
fun GeminiSparkleLogo(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4285F4), // Google Blue
            Color(0xFF9060EB), // Gemini Violet
            Color(0xFFE040FB), // Magenta accent
            Color(0xFF42A5F5)
        ),
        start = Offset(0f, 0f),
        end = Offset(size.value * 3, size.value * 3)
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // 4-pointed hypocycloid star
            val path = Path().apply {
                moveTo(cx, 0f)
                // Top to right curve (inner bend at cx + r*k, cy - r*k)
                cubicTo(cx * 1.15f, cy * 0.85f, cx * 1.15f, cy * 0.85f, w, cy)
                // Right to bottom curve
                cubicTo(cx * 1.15f, cy * 1.15f, cx * 1.15f, cy * 1.15f, cx, h)
                // Bottom to left curve
                cubicTo(cx * 0.85f, cy * 1.15f, cx * 0.85f, cy * 1.15f, 0f, cy)
                // Left to top curve
                cubicTo(cx * 0.85f, cy * 0.85f, cx * 0.85f, cy * 0.85f, cx, 0f)
                close()
            }

            drawPath(path = path, brush = gradient)
        }
    }
}

@Composable
fun AiOverviewCard(
    overview: AiWeatherOverview?,
    isLoading: Boolean,
    isNight: Boolean,
    onRefreshAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isNight) Color(0xFF16203A) else Color.White
    val cardBorder = if (isNight) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val textColor = if (isNight) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val subTextColor = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B)

    val geminiGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4285F4),
            Color(0xFF9060EB),
            Color(0xFFD946EF)
        )
    )

    val infiniteTransition = rememberInfiniteTransition(label = "geminiShimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "geminiAlpha"
    )

    Surface(
        color = cardBg,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
            .testTag("ai_overview_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Gemini Logo + "AI Overview" + Gemini badge + Refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    GeminiSparkleLogo(size = 26.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Gemini Badge
                    Box(
                        modifier = Modifier
                            .background(
                                brush = geminiGradient,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "Gemini",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshAi,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("refresh_ai_overview_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF9060EB)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh AI Overview",
                            tint = if (isNight) Color(0xFFA5B4FC) else Color(0xFF6366F1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading && overview == null) {
                // Skeleton loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isNight) Color(0xFF1E2D52).copy(alpha = shimmerAlpha)
                            else Color(0xFFEEF2FF).copy(alpha = shimmerAlpha)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GeminiSparkleLogo(size = 20.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini is analyzing the forecast...",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNight) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
                        )
                    }
                }
            } else if (overview != null) {
                // 1. Today's Overview
                OverviewSectionBlock(
                    icon = Icons.Default.WbSunny,
                    iconTint = Color(0xFFF59E0B),
                    title = "Today's Overview",
                    content = overview.todaySummary,
                    isNight = isNight,
                    textColor = textColor,
                    subTextColor = subTextColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Next Few Days Outlook
                OverviewSectionBlock(
                    icon = Icons.Default.CalendarMonth,
                    iconTint = Color(0xFF3B82F6),
                    title = "Next Few Days",
                    content = overview.upcomingDaysSummary,
                    isNight = isNight,
                    textColor = textColor,
                    subTextColor = subTextColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Clothing & Activity Advice
                OverviewSectionBlock(
                    icon = Icons.Default.Lightbulb,
                    iconTint = Color(0xFF10B981),
                    title = "Outfit & Outdoor Tips",
                    content = overview.dressingAndActivityTip,
                    isNight = isNight,
                    textColor = textColor,
                    subTextColor = subTextColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtle footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF9060EB),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (overview.isLiveGemini) "Generated by Gemini 3.5 Flash" else "Smart AI Weather Synthesis",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNight) Color(0xFFA5B4FC) else Color(0xFF6366F1)
                        )
                    }

                    Text(
                        text = "Day & multi-day outlook",
                        fontSize = 11.sp,
                        color = subTextColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewSectionBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    content: String,
    isNight: Boolean,
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isNight) Color(0xFF1E2D52).copy(alpha = 0.7f) else Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = if (isNight) 0.25f else 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 19.sp,
                color = if (isNight) Color(0xFFCBD5E1) else Color(0xFF334155)
            )
        }
    }
}
