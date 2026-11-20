package com.example.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.WeatherDatabase
import com.example.data.model.WeatherInfo
import com.example.data.repository.WeatherRepository
import com.example.util.TemperatureFormatter
import com.example.util.TemperatureUnit
import java.util.Calendar
import java.util.Locale

data class MorningSummaryContent(
    val title: String,
    val shortText: String,
    val bigText: String
)

object MorningWeatherNotificationManager {
    const val PREFS_NAME = "sheep_weather_user_settings"
    const val KEY_MORNING_NOTIFICATIONS_ENABLED = "morning_notifications_enabled"
    const val KEY_MORNING_HOUR = "morning_notification_hour"
    const val KEY_MORNING_MINUTE = "morning_notification_minute"
    const val KEY_PERMISSION_PROMPTED = "morning_notification_permission_prompted"

    const val DEFAULT_MORNING_HOUR = 7
    const val DEFAULT_MORNING_MINUTE = 30

    const val CHANNEL_ID = "morning_weather_summary"
    const val NOTIFICATION_ID = 2701
    const val ACTION_MORNING_SUMMARY = "com.example.notifications.ACTION_MORNING_SUMMARY"
    private const val ALARM_REQUEST_CODE = 2701

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MORNING_NOTIFICATIONS_ENABLED, true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MORNING_NOTIFICATIONS_ENABLED, enabled)
            .apply()
        if (enabled) {
            scheduleMorningSummary(context)
        } else {
            cancelMorningSummary(context)
        }
    }

    fun getMorningTime(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hour = prefs.getInt(KEY_MORNING_HOUR, DEFAULT_MORNING_HOUR).coerceIn(0, 23)
        val minute = prefs.getInt(KEY_MORNING_MINUTE, DEFAULT_MORNING_MINUTE).coerceIn(0, 59)
        return hour to minute
    }

    fun setMorningTime(context: Context, hour: Int, minute: Int) {
        val clampedHour = hour.coerceIn(0, 23)
        val clampedMinute = minute.coerceIn(0, 59)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_MORNING_HOUR, clampedHour)
            .putInt(KEY_MORNING_MINUTE, clampedMinute)
            .apply()
        if (isEnabled(context)) {
            scheduleMorningSummary(context)
        }
    }

    fun formatMorningTime(hour: Int, minute: Int): String {
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (hour < 12) "AM" else "PM"
        return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun wasPermissionPrompted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PERMISSION_PROMPTED, false)
    }

    fun markPermissionPrompted(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PERMISSION_PROMPTED, true)
            .apply()
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_morning_name)
            val descriptionText = context.getString(R.string.notification_channel_morning_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun calculateNextTriggerMillis(
        hourOfDay: Int,
        minute: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hourOfDay.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= nowMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    fun scheduleMorningSummary(context: Context) {
        if (!isEnabled(context)) return
        createNotificationChannel(context)

        val (hour, minute) = getMorningTime(context)
        val triggerAtMillis = calculateNextTriggerMillis(hour, minute)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildAlarmPendingIntent(context)

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // Gracefully ignore if OS denies alarm scheduling
        } catch (_: Exception) {
        }
    }

    fun cancelMorningSummary(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildAlarmPendingIntent(context)
        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {
        }
    }

    private fun buildAlarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MorningSummaryReceiver::class.java).apply {
            action = ACTION_MORNING_SUMMARY
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, flags)
    }

    fun buildSummaryContent(weather: WeatherInfo, unit: TemperatureUnit): MorningSummaryContent {
        val currentTemp = TemperatureFormatter.format(weather.current.temperature, unit)
        val condition = weather.current.conditionDescription
        val today = weather.daily.firstOrNull()
        val highCelsius = today?.tempMax ?: weather.current.temperature
        val lowCelsius = today?.tempMin ?: weather.current.realFeel
        val highFormatted = TemperatureFormatter.format(highCelsius, unit)
        val lowFormatted = TemperatureFormatter.format(lowCelsius, unit)

        val title = "${weather.locationName}: $currentTemp • $condition"
        val shortText = "High $highFormatted / Low $lowFormatted • $condition"
        val bigText = "Good morning! Currently $currentTemp ($condition) in ${weather.locationName}. " +
            "Today's High: $highFormatted • Low: $lowFormatted."

        return MorningSummaryContent(
            title = title,
            shortText = shortText,
            bigText = bigText
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun showMorningSummaryNotification(
        context: Context,
        weatherOverride: WeatherInfo? = null
    ): Boolean {
        if (!hasNotificationPermission(context)) {
            return false
        }
        createNotificationChannel(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val unit = try {
            TemperatureUnit.valueOf(
                prefs.getString("temperature_unit", TemperatureUnit.CELSIUS.name)
                    ?: TemperatureUnit.CELSIUS.name
            )
        } catch (_: Exception) {
            TemperatureUnit.CELSIUS
        }

        val weather = weatherOverride ?: run {
            val database = WeatherDatabase.getInstance(context)
            val repository = WeatherRepository(database.weatherDao(), context = context)
            repository.initializeDefaultLocationsIfNeeded()
            val selected = repository.getSelectedLocation()
            repository.getWeatherForLocation(selected, forceRefresh = true)
        }

        val content = buildSummaryContent(weather, unit)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_weather)
            .setContentTitle(content.title)
            .setContentText(content.shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
