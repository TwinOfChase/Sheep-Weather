package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.widget.WidgetConfig
import com.example.widget.WidgetPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29]) // Test specifically on Android 10 (API 29)
class WidgetPreferencesTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testSaveAndLoadConfig() {
        val appWidgetId = 101
        val config = WidgetConfig(
            backgroundColor = WidgetConfig.COLOR_WHITE,
            transparencyPercent = 50,
            matchDarkMode = false,
            locationId = 42L
        )

        WidgetPreferences.saveConfig(context, appWidgetId, config)
        val loaded = WidgetPreferences.loadConfig(context, appWidgetId)

        assertEquals(WidgetConfig.COLOR_WHITE, loaded.backgroundColor)
        assertEquals(50, loaded.transparencyPercent)
        assertFalse(loaded.matchDarkMode)
        assertEquals(42L, loaded.locationId)

        WidgetPreferences.deleteConfig(context, appWidgetId)
        val deleted = WidgetPreferences.loadConfig(context, appWidgetId)
        assertEquals(WidgetConfig.COLOR_BLACK, deleted.backgroundColor) // default fallback
    }

    @Test
    fun testVisualResolution_solidBlack() {
        val config = WidgetConfig(
            backgroundColor = WidgetConfig.COLOR_BLACK,
            transparencyPercent = 0, // Solid
            matchDarkMode = false
        )
        val visuals = WidgetPreferences.resolveVisuals(context, config)

        assertTrue(visuals.isDarkAppearance)
        // Alpha should be 255 (0xFF)
        val alpha = (visuals.backgroundArgb ushr 24) and 0xFF
        assertEquals(255, alpha)
    }

    @Test
    fun testVisualResolution_transparencyAlphaMath() {
        val config50 = WidgetConfig(
            backgroundColor = WidgetConfig.COLOR_BLACK,
            transparencyPercent = 50, // 50% clear -> alpha ~ 127
            matchDarkMode = false
        )
        val visuals50 = WidgetPreferences.resolveVisuals(context, config50)
        val alpha50 = (visuals50.backgroundArgb ushr 24) and 0xFF
        assertTrue(alpha50 in 126..128)

        val config100 = WidgetConfig(
            backgroundColor = WidgetConfig.COLOR_BLACK,
            transparencyPercent = 100, // 100% clear -> alpha 0
            matchDarkMode = false
        )
        val visuals100 = WidgetPreferences.resolveVisuals(context, config100)
        val alpha100 = (visuals100.backgroundArgb ushr 24) and 0xFF
        assertEquals(0, alpha100)
    }

    @Test
    fun testVisualResolution_contrastTextOnHighTransparency() {
        // Even if white background is chosen, if transparency is high (e.g. 80%),
        // white text should be used to guarantee readability on various home screen wallpapers
        val config = WidgetConfig(
            backgroundColor = WidgetConfig.COLOR_WHITE,
            transparencyPercent = 80,
            matchDarkMode = false
        )
        val visuals = WidgetPreferences.resolveVisuals(context, config)
        // White text: Color.parseColor("#FFFFFF") = -1
        assertEquals(android.graphics.Color.WHITE, visuals.primaryTextColor)
    }
}
