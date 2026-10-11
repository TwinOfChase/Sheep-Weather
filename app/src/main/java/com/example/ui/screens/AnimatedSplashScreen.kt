package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppCopyrightFooter
import com.example.ui.components.drawCartoonSheep
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(
    isNight: Boolean,
    isLoaded: Boolean = true,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationStarted by remember { mutableStateOf(false) }
    var minDisplayPassed by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.85f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splashScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splashBreathing")
    val breathingOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splashBreathingOffset"
    )

    val sleepZOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "splashSleepZ"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        // Allow mascot animation to play for at least 1.4 seconds
        delay(1400)
        minDisplayPassed = true
    }

    // Seamless dismissal as soon as minimum animation has played and initial weather load completes
    LaunchedEffect(minDisplayPassed, isLoaded) {
        if (minDisplayPassed && isLoaded) {
            onAnimationFinished()
        }
    }

    // Safety timeout: dismiss after 4.5 seconds maximum so user is never stuck
    LaunchedEffect(Unit) {
        delay(4500)
        onAnimationFinished()
    }

    val bgBrush = if (isNight) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF070B19),
                Color(0xFF0F1A36),
                Color(0xFF16254C)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF5BA4E6),
                Color(0xFF90CAF9),
                Color(0xFFE3F2FD)
            )
        )
    }

    val textColor = if (isNight) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isNight) Color(0xFF94A3B8) else Color(0xFF475569)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bgBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onAnimationFinished()
            }
            .testTag("animated_splash_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .alpha(alphaAnim)
                .scale(scaleAnim),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Central Mascot & Title Group
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Cartoon Sheep Canvas
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .testTag("splash_cartoon_sheep_mascot")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Gentle glow beneath sheep
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (isNight) Color(0xFF3B82F6).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            radius = w * 0.48f,
                            center = center
                        )

                        drawCartoonSheep(
                            centerX = w * 0.5f,
                            centerY = h * 0.50f + breathingOffset,
                            scale = 1.15f,
                            isNight = isNight,
                            sleepZOffset = sleepZOffset
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Words "Sheep Weather" rendered in the font used for the main current temperature
                // (FontWeight.Black, letterSpacing = (-1.5).sp)
                Text(
                    text = "Sheep Weather",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("splash_title_text")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = if (isNight) Color(0xFF1E293B).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Version 2.5 • Offline First & Data Saver",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNight) Color(0xFF60A5FA) else Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom Notice & Mandatory Copyright
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Notice: "This app may be changing regularly to solve bugs and add new features"
                Surface(
                    color = if (isNight) Color(0xFF1E293B).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isNight) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "This app may be changing regularly to solve bugs and add new features",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = subTextColor,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Copyright: "Copyright Studio Of Chase 2026. All Rights Reserved."
                AppCopyrightFooter(isNight = isNight)
            }
        }
    }
}
