package com.example.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

data class WidgetConfig(
    val backgroundColor: String = COLOR_BLACK, // "BLACK" or "WHITE"
    val transparencyPercent: Int = 30,         // 0% (solid) to 100% (clear)
    val matchDarkMode: Boolean = true,
    val locationId: Long = -1L                 // -1L = follow active app location
) {
    companion object {
        const val COLOR_BLACK = "BLACK"
        const val COLOR_WHITE = "WHITE"
    }
}

data class WidgetVisuals(
    val backgroundArgb: Int,
    val primaryTextColor: Int,
    val secondaryTextColor: Int,
    val accentTextColor: Int,
    val settingsIconTint: Int,
    val isDarkAppearance: Boolean
)

object WidgetPreferences {
    private const val PREFS_NAME = "sheep_weather_widget_prefs"

    private const val KEY_PREFIX_BG = "widget_bg_"
    private const val KEY_PREFIX_TRANS = "widget_trans_"
    private const val KEY_PREFIX_MATCH_DARK = "widget_match_dark_"
    private const val KEY_PREFIX_LOC = "widget_loc_"

    fun saveConfig(context: Context, appWidgetId: Int, config: WidgetConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("$KEY_PREFIX_BG$appWidgetId", config.backgroundColor)
            .putInt("$KEY_PREFIX_TRANS$appWidgetId", config.transparencyPercent)
            .putBoolean("$KEY_PREFIX_MATCH_DARK$appWidgetId", config.matchDarkMode)
            .putLong("$KEY_PREFIX_LOC$appWidgetId", config.locationId)
            .apply()
    }

    fun loadConfig(context: Context, appWidgetId: Int): WidgetConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WidgetConfig(
            backgroundColor = prefs.getString("$KEY_PREFIX_BG$appWidgetId", WidgetConfig.COLOR_BLACK) ?: WidgetConfig.COLOR_BLACK,
            transparencyPercent = prefs.getInt("$KEY_PREFIX_TRANS$appWidgetId", 30),
            matchDarkMode = prefs.getBoolean("$KEY_PREFIX_MATCH_DARK$appWidgetId", true),
            locationId = prefs.getLong("$KEY_PREFIX_LOC$appWidgetId", -1L)
        )
    }

    fun deleteConfig(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove("$KEY_PREFIX_BG$appWidgetId")
            .remove("$KEY_PREFIX_TRANS$appWidgetId")
            .remove("$KEY_PREFIX_MATCH_DARK$appWidgetId")
            .remove("$KEY_PREFIX_LOC$appWidgetId")
            .apply()
    }

    fun isSystemInDarkTheme(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun resolveVisuals(context: Context, config: WidgetConfig): WidgetVisuals {
        val systemDark = isSystemInDarkTheme(context)
        val isDarkAppearance = if (config.matchDarkMode) {
            systemDark
        } else {
            config.backgroundColor == WidgetConfig.COLOR_BLACK
        }

        // Calculate opacity: 0% transparency = 255 alpha, 100% transparency = 0 alpha
        val alpha = (((100 - config.transparencyPercent.coerceIn(0, 100)) / 100f) * 255).toInt().coerceIn(0, 255)
        val rgb = if (isDarkAppearance) 0x111827 else 0xFFFFFF
        val backgroundArgb = Color.argb(alpha, (rgb shr 16) and 0xFF, (rgb shr 8) and 0xFF, rgb and 0xFF)

        // For text contrast: If dark appearance OR high transparency (> 60%), use bright text
        val useBrightText = isDarkAppearance || config.transparencyPercent >= 60

        val primaryTextColor = if (useBrightText) Color.parseColor("#FFFFFF") else Color.parseColor("#0F172A")
        val secondaryTextColor = if (useBrightText) Color.parseColor("#D1D5DB") else Color.parseColor("#475569")
        val accentTextColor = if (useBrightText) Color.parseColor("#9CA3AF") else Color.parseColor("#64748B")
        val settingsTint = if (useBrightText) Color.parseColor("#9CA3AF") else Color.parseColor("#64748B")

        return WidgetVisuals(
            backgroundArgb = backgroundArgb,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            accentTextColor = accentTextColor,
            settingsIconTint = settingsTint,
            isDarkAppearance = isDarkAppearance
        )
    }
}
