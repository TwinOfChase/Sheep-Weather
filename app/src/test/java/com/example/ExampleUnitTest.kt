package com.example

import com.example.data.model.CurrentWeather
import com.example.data.model.DailyWeather
import com.example.data.model.WeatherInfo
import com.example.notifications.MorningWeatherNotificationManager
import com.example.ui.WeatherUiState
import com.example.util.TemperatureUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun morningNotifications_enabledByDefault() {
    val defaultState = WeatherUiState()
    assertTrue(defaultState.isMorningNotificationsEnabled)
    assertEquals(7, defaultState.morningNotificationHour)
    assertEquals(30, defaultState.morningNotificationMinute)
  }

  @Test
  fun morningSummaryContent_includesTempConditionHighAndLow() {
    val sampleWeather = WeatherInfo(
      locationName = "Wellington",
      country = "New Zealand",
      latitude = -41.2865,
      longitude = 174.7762,
      current = CurrentWeather(
        time = "2026-09-21T07:30",
        temperature = 14.0,
        realFeel = 12.5,
        weatherCode = 2,
        conditionDescription = "Partly Cloudy",
        isDay = true,
        humidity = 72,
        windSpeed = 24.0,
        uvIndex = 3.0,
        cloudCover = 40
      ),
      hourly = emptyList(),
      daily = listOf(
        DailyWeather(
          dateIso = "2026-09-21",
          dayOfWeek = "Today",
          formattedDate = "Sep 21",
          weatherCode = 2,
          conditionDescription = "Partly Cloudy",
          tempMax = 18.0,
          tempMin = 9.0,
          realFeelMax = 17.0,
          realFeelMin = 7.5,
          precipitationProbability = 15,
          sunrise = "6:12 AM",
          sunset = "6:20 PM"
        )
      ),
      isNight = false,
      lastUpdatedMillis = 1_000_000L
    )

    val celsiusSummary = MorningWeatherNotificationManager.buildSummaryContent(sampleWeather, TemperatureUnit.CELSIUS)
    assertTrue(celsiusSummary.title.contains("14°C"))
    assertTrue(celsiusSummary.title.contains("Partly Cloudy"))
    assertTrue(celsiusSummary.shortText.contains("18°C"))
    assertTrue(celsiusSummary.shortText.contains("9°C"))
    assertTrue(celsiusSummary.bigText.contains("14°C"))
    assertTrue(celsiusSummary.bigText.contains("Partly Cloudy"))
    assertTrue(celsiusSummary.bigText.contains("18°C"))
    assertTrue(celsiusSummary.bigText.contains("9°C"))

    val fahrenheitSummary = MorningWeatherNotificationManager.buildSummaryContent(sampleWeather, TemperatureUnit.FAHRENHEIT)
    assertTrue(fahrenheitSummary.title.contains("57°F"))
    assertTrue(fahrenheitSummary.shortText.contains("64°F"))
    assertTrue(fahrenheitSummary.shortText.contains("48°F"))
  }

  @Test
  fun morningTriggerTime_schedulesNextOccurrenceCleanly() {
    val morningBefore = Calendar.getInstance().apply {
      set(2026, Calendar.SEPTEMBER, 21, 6, 45, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val sameDayTrigger = MorningWeatherNotificationManager.calculateNextTriggerMillis(
      hourOfDay = 7,
      minute = 30,
      nowMillis = morningBefore.timeInMillis
    )
    val sameDayCal = Calendar.getInstance().apply { timeInMillis = sameDayTrigger }
    assertEquals(21, sameDayCal.get(Calendar.DAY_OF_MONTH))
    assertEquals(7, sameDayCal.get(Calendar.HOUR_OF_DAY))
    assertEquals(30, sameDayCal.get(Calendar.MINUTE))

    val morningAfter = Calendar.getInstance().apply {
      set(2026, Calendar.SEPTEMBER, 21, 8, 15, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val nextDayTrigger = MorningWeatherNotificationManager.calculateNextTriggerMillis(
      hourOfDay = 7,
      minute = 30,
      nowMillis = morningAfter.timeInMillis
    )
    val nextDayCal = Calendar.getInstance().apply { timeInMillis = nextDayTrigger }
    assertEquals(22, nextDayCal.get(Calendar.DAY_OF_MONTH))
    assertEquals(7, nextDayCal.get(Calendar.HOUR_OF_DAY))
    assertEquals(30, nextDayCal.get(Calendar.MINUTE))
  }
}
