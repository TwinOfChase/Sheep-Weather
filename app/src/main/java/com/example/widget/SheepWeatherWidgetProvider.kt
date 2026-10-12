package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.LocationEntity
import com.example.data.local.WeatherDatabase
import com.example.data.model.WeatherInfo
import com.example.data.repository.WeatherRepository
import com.example.util.TemperatureFormatter
import com.example.util.TemperatureUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SheepWeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            WidgetPreferences.deleteConfig(context, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            refreshAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, SheepWeatherWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val config = WidgetPreferences.loadConfig(context, appWidgetId)
                    val visuals = WidgetPreferences.resolveVisuals(context, config)

                    val db = WeatherDatabase.getInstance(context)
                    val dao = db.weatherDao()

                    // Resolve target location
                    val location: LocationEntity = if (config.locationId > 0) {
                        dao.getLocationById(config.locationId) ?: dao.getSelectedLocation() ?: defaultLocation()
                    } else {
                        dao.getSelectedLocation() ?: defaultLocation()
                    }

                    // Fetch cached/live weather info
                    val repo = WeatherRepository(dao, context = context)
                    val weatherInfo: WeatherInfo? = try {
                        repo.getWeatherForLocation(location, forceRefresh = false)
                    } catch (_: Exception) {
                        null
                    }

                    // Resolve temperature unit
                    val prefs = context.getSharedPreferences("sheep_weather_user_settings", Context.MODE_PRIVATE)
                    val tempUnit = try {
                        TemperatureUnit.valueOf(
                            prefs.getString("temperature_unit", TemperatureUnit.CELSIUS.name) ?: TemperatureUnit.CELSIUS.name
                        )
                    } catch (_: Exception) {
                        TemperatureUnit.CELSIUS
                    }

                    val views = RemoteViews(context.packageName, R.layout.sheep_weather_widget)

                    // 1. Dynamic Background Tint & Opacity
                    val baseRgb = if (visuals.isDarkAppearance) Color.parseColor("#111827") else Color.parseColor("#FFFFFF")
                    val alpha = (((100 - config.transparencyPercent.coerceIn(0, 100)) / 100f) * 255).toInt().coerceIn(0, 255)
                    views.setInt(R.id.widget_bg_image, "setColorFilter", baseRgb)
                    views.setInt(R.id.widget_bg_image, "setImageAlpha", alpha)

                    // 2. High-Contrast Text Colors based on theme & transparency
                    views.setTextColor(R.id.widget_text_clock, visuals.primaryTextColor)
                    views.setTextColor(R.id.widget_date_clock, visuals.secondaryTextColor)
                    views.setTextColor(R.id.widget_location, visuals.primaryTextColor)
                    views.setTextColor(R.id.widget_temperature, visuals.primaryTextColor)
                    views.setTextColor(R.id.widget_condition_desc, visuals.secondaryTextColor)
                    views.setTextColor(R.id.widget_high_low, visuals.accentTextColor)
                    views.setInt(R.id.widget_btn_settings, "setColorFilter", visuals.settingsIconTint)

                    // 3. Location & Weather Content
                    views.setTextViewText(R.id.widget_location, location.name)

                    if (weatherInfo != null) {
                        val currentTemp = TemperatureFormatter.toDisplayInt(weatherInfo.current.temperature, tempUnit)
                        views.setTextViewText(R.id.widget_temperature, "$currentTemp°")
                        views.setTextViewText(R.id.widget_condition_desc, weatherInfo.current.conditionDescription)

                        // Condition Icon
                        val iconRes = getConditionDrawable(weatherInfo.current.weatherCode, weatherInfo.current.isDay)
                        views.setImageViewResource(R.id.widget_condition_icon, iconRes)

                        // High / Low summary
                        val todayDaily = weatherInfo.daily.firstOrNull()
                        if (todayDaily != null) {
                            val maxTemp = TemperatureFormatter.toDisplayInt(todayDaily.tempMax, tempUnit)
                            val minTemp = TemperatureFormatter.toDisplayInt(todayDaily.tempMin, tempUnit)
                            views.setTextViewText(R.id.widget_high_low, "H: $maxTemp°  L: $minTemp°")
                        } else {
                            views.setTextViewText(R.id.widget_high_low, "Feels like ${TemperatureFormatter.toDisplayInt(weatherInfo.current.realFeel, tempUnit)}°")
                        }
                    } else {
                        // Fallback state before network sync
                        views.setTextViewText(R.id.widget_temperature, "--°")
                        views.setTextViewText(R.id.widget_condition_desc, "Sheep Weather")
                        views.setTextViewText(R.id.widget_high_low, "Tap to update")
                        views.setImageViewResource(R.id.widget_condition_icon, R.drawable.ic_weather_sunny)
                    }

                    // 4. Tap Actions
                    // Tap on widget root opens MainActivity for that location
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("TARGET_LOCATION_ID", location.id)
                    }
                    val mainPending = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, mainPending)

                    // Tap on settings icon opens the Samsung-style setup activity
                    val configIntent = Intent(context, SheepWeatherWidgetConfigureActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    val configPending = PendingIntent.getActivity(
                        context,
                        appWidgetId + 20000,
                        configIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_btn_settings, configPending)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun defaultLocation(): LocationEntity {
            return LocationEntity(
                name = "London",
                country = "United Kingdom",
                latitude = 51.5074,
                longitude = -0.1278,
                isSelected = true
            )
        }

        private fun getConditionDrawable(weatherCode: Int, isDay: Boolean): Int {
            return when (weatherCode) {
                0 -> if (isDay) R.drawable.ic_weather_sunny else R.drawable.ic_weather_night
                1 -> if (isDay) R.drawable.ic_weather_sunny else R.drawable.ic_weather_night
                2 -> R.drawable.ic_weather_partly_cloudy
                3 -> R.drawable.ic_weather_cloudy
                45, 48 -> R.drawable.ic_weather_cloudy
                51, 53, 55 -> R.drawable.ic_weather_rain
                61, 63, 65 -> R.drawable.ic_weather_rain
                71, 73, 75, 77, 85, 86 -> R.drawable.ic_weather_snow
                80, 81, 82 -> R.drawable.ic_weather_rain
                95, 96, 99 -> R.drawable.ic_weather_thunderstorm
                else -> if (isDay) R.drawable.ic_weather_sunny else R.drawable.ic_weather_night
            }
        }
    }
}
