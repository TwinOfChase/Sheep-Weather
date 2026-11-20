package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class SheepWeatherPalette(
    val isChristmas: Boolean,
    val isNight: Boolean,
    val canvasBg: Color,
    val drawerBg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val innerWellBg: Color,
    val innerWellBorder: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val goldAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

fun resolveSheepWeatherPalette(
    isNight: Boolean,
    isChristmasEnabled: Boolean
): SheepWeatherPalette {
    return when {
        isChristmasEnabled && isNight -> SheepWeatherPalette(
            isChristmas = true,
            isNight = true,
            canvasBg = ChristmasPineNightCanvas,
            drawerBg = Color(0xFF0A1F18),
            cardBg = ChristmasPineCardDark.copy(alpha = 0.92f),
            cardBorder = CandlelightGold.copy(alpha = 0.22f),
            innerWellBg = ChristmasSpruceWellDark,
            innerWellBorder = DeepPineMint.copy(alpha = 0.26f),
            primaryAccent = PohutukawaCrimsonGlow,
            secondaryAccent = DeepPineMint,
            goldAccent = CandlelightGold,
            textPrimary = Color(0xFFFFF8F0),
            textSecondary = Color(0xFFB5C7BE)
        )
        isChristmasEnabled && !isNight -> SheepWeatherPalette(
            isChristmas = true,
            isNight = false,
            canvasBg = ChristmasWarmCreamCanvas,
            drawerBg = ChristmasWarmCreamCanvas,
            cardBg = ChristmasWarmCardLight.copy(alpha = 0.95f),
            cardBorder = PohutukawaCrimson.copy(alpha = 0.16f),
            innerWellBg = ChristmasWarmWellLight,
            innerWellBorder = WarmHolidayGold.copy(alpha = 0.28f),
            primaryAccent = PohutukawaCrimson,
            secondaryAccent = DeepPineGreen,
            goldAccent = WarmHolidayGold,
            textPrimary = Color(0xFF1A1412),
            textSecondary = Color(0xFF5C4D42)
        )
        !isChristmasEnabled && isNight -> SheepWeatherPalette(
            isChristmas = false,
            isNight = true,
            canvasBg = DarkCanvas,
            drawerBg = Color(0xFF0F172A),
            cardBg = Color(0xFF14203B).copy(alpha = 0.88f),
            cardBorder = Color.White.copy(alpha = 0.12f),
            innerWellBg = Color(0xFF1E2D52),
            innerWellBorder = Color(0xFF3B82F6).copy(alpha = 0.35f),
            primaryAccent = MidnightPrimary,
            secondaryAccent = MidnightSecondary,
            goldAccent = SunAmber,
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8)
        )
        else -> SheepWeatherPalette(
            isChristmas = false,
            isNight = false,
            canvasBg = LightCanvas,
            drawerBg = LightCanvas,
            cardBg = Color.White.copy(alpha = 0.92f),
            cardBorder = Color.Black.copy(alpha = 0.08f),
            innerWellBg = Color(0xFFF1F5F9),
            innerWellBorder = Color(0xFFBFDBFE),
            primaryAccent = SkyBluePrimary,
            secondaryAccent = SkyBlueSecondary,
            goldAccent = Color(0xFFD97706),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF475569)
        )
    }
}

val LocalSheepWeatherPalette = staticCompositionLocalOf {
    resolveSheepWeatherPalette(isNight = false, isChristmasEnabled = false)
}

private val DarkColorScheme =
    darkColorScheme(
        primary = MidnightPrimary,
        secondary = MidnightSecondary,
        tertiary = SunAmber,
        background = DarkCanvas,
        surface = MidnightDarkBg
    )

private val LightColorScheme =
    lightColorScheme(
        primary = SkyBluePrimary,
        secondary = SkyBlueSecondary,
        tertiary = SunAmber,
        background = LightCanvas,
        surface = LightCanvas
    )

private val ChristmasDarkColorScheme =
    darkColorScheme(
        primary = PohutukawaCrimsonGlow,
        secondary = DeepPineMint,
        tertiary = CandlelightGold,
        background = ChristmasPineNightCanvas,
        surface = ChristmasPineCardDark
    )

private val ChristmasLightColorScheme =
    lightColorScheme(
        primary = PohutukawaCrimson,
        secondary = DeepPineGreen,
        tertiary = WarmHolidayGold,
        background = ChristmasWarmCreamCanvas,
        surface = ChristmasWarmCardLight
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isChristmasEnabled: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            isChristmasEnabled && darkTheme -> ChristmasDarkColorScheme
            isChristmasEnabled && !darkTheme -> ChristmasLightColorScheme
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    val palette = resolveSheepWeatherPalette(
        isNight = darkTheme,
        isChristmasEnabled = isChristmasEnabled
    )

    CompositionLocalProvider(LocalSheepWeatherPalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
