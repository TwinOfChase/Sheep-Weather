package com.example

import com.example.data.model.HourlyWeather
import com.example.data.model.LayersForecastCalculator
import com.example.data.remote.GeminiWeatherService
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyWeather
import com.example.data.model.WeatherInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LayersAndAiOverviewTest {

    private fun createSampleHourly(temp: Double, precipProb: Int, code: Int = 0, wind: Double = 12.0): HourlyWeather {
        return HourlyWeather(
            timeIso = "2026-09-21T14:00",
            displayHour = "2 PM",
            temperature = temp,
            realFeel = temp - 1.0,
            weatherCode = code,
            conditionDescription = "Partly Cloudy",
            isDay = true,
            precipitationProbability = precipProb,
            windSpeed = wind,
            humidity = 55,
            uvIndex = 3.5,
            cloudCover = 30
        )
    }

    @Test
    fun testWarmDryWeatherLayersCalculation() {
        val hours = List(5) { createSampleHourly(temp = 25.0, precipProb = 5, wind = 10.0) }
        val forecast = LayersForecastCalculator.calculate(hours, fallbackTemp = 25.0)

        assertEquals(1, forecast.layerCount)
        assertEquals("👕 1", forecast.layerEmojiString)
        assertFalse(forecast.isUmbrellaRequired)
        assertFalse(forecast.isTooWindyForUmbrella)
        assertEquals("No Umbrella Needed", forecast.umbrellaStatus)
    }

    @Test
    fun testColdWeatherLayersCalculation() {
        val hours = List(5) { createSampleHourly(temp = 8.0, precipProb = 10, wind = 15.0) }
        val forecast = LayersForecastCalculator.calculate(hours, fallbackTemp = 8.0)

        assertEquals(3, forecast.layerCount)
        assertEquals("👕 3", forecast.layerEmojiString)
        assertFalse(forecast.isUmbrellaRequired)
        assertFalse(forecast.isTooWindyForUmbrella)
    }

    @Test
    fun testRainTriggersUmbrellaRequirementWhenCalm() {
        val hours = listOf(
            createSampleHourly(temp = 16.0, precipProb = 10, wind = 12.0),
            createSampleHourly(temp = 15.0, precipProb = 20, wind = 14.0),
            createSampleHourly(temp = 14.0, precipProb = 65, code = 61, wind = 18.0), // rain code
            createSampleHourly(temp = 14.0, precipProb = 40, wind = 15.0),
            createSampleHourly(temp = 13.0, precipProb = 20, wind = 12.0)
        )
        val forecast = LayersForecastCalculator.calculate(hours, fallbackTemp = 15.0)

        assertTrue(forecast.isUmbrellaRequired)
        assertFalse(forecast.isTooWindyForUmbrella)
        assertEquals("Umbrella Needed", forecast.umbrellaStatus)
        assertEquals("☂️", forecast.umbrellaEmoji)
    }

    @Test
    fun testHighWindWithRainTriggersTooWindyForUmbrella() {
        val hours = listOf(
            createSampleHourly(temp = 15.0, precipProb = 10, wind = 20.0),
            createSampleHourly(temp = 14.0, precipProb = 70, code = 61, wind = 42.0), // rain + 42 km/h wind
            createSampleHourly(temp = 14.0, precipProb = 50, wind = 38.0),
            createSampleHourly(temp = 13.0, precipProb = 30, wind = 25.0)
        )
        val forecast = LayersForecastCalculator.calculate(hours, fallbackTemp = 14.0)

        assertTrue(forecast.isUmbrellaRequired)
        assertTrue(forecast.isTooWindyForUmbrella)
        assertEquals("Too Windy for Umbrella", forecast.umbrellaStatus)
        assertEquals("💨 ☂️", forecast.umbrellaEmoji)
        assertTrue(forecast.umbrellaNote.contains("too strong for an umbrella"))
    }

    @Test
    fun testHighWindWithoutRain() {
        val hours = List(5) { createSampleHourly(temp = 18.0, precipProb = 5, wind = 40.0) }
        val forecast = LayersForecastCalculator.calculate(hours, fallbackTemp = 18.0)

        assertFalse(forecast.isUmbrellaRequired)
        assertTrue(forecast.isTooWindyForUmbrella)
        assertEquals("No Umbrella Needed", forecast.umbrellaStatus)
        assertTrue(forecast.umbrellaNote.contains("High winds"))
    }

    @Test
    fun testAiOverviewLocalSynthesis() {
        val sampleWeather = WeatherInfo(
            locationName = "London",
            country = "United Kingdom",
            latitude = 51.5074,
            longitude = -0.1278,
            current = CurrentWeather(
                time = "2026-09-21T14:00",
                temperature = 18.0,
                realFeel = 17.0,
                weatherCode = 1,
                conditionDescription = "Mainly Clear",
                isDay = true,
                humidity = 60,
                windSpeed = 15.0,
                uvIndex = 4.0,
                cloudCover = 20
            ),
            hourly = List(24) { createSampleHourly(temp = 18.0, precipProb = 10) },
            daily = listOf(
                DailyWeather(
                    dateIso = "2026-09-21",
                    dayOfWeek = "Today",
                    formattedDate = "Sep 21",
                    weatherCode = 1,
                    conditionDescription = "Mainly Clear",
                    tempMax = 20.0,
                    tempMin = 12.0,
                    realFeelMax = 19.0,
                    realFeelMin = 11.0,
                    precipitationProbability = 10,
                    sunrise = "6:45 AM",
                    sunset = "7:05 PM"
                ),
                DailyWeather(
                    dateIso = "2026-09-22",
                    dayOfWeek = "Tue",
                    formattedDate = "Sep 22",
                    weatherCode = 2,
                    conditionDescription = "Partly Cloudy",
                    tempMax = 21.0,
                    tempMin = 13.0,
                    realFeelMax = 20.0,
                    realFeelMin = 12.0,
                    precipitationProbability = 15,
                    sunrise = "6:47 AM",
                    sunset = "7:03 PM"
                )
            ),
            isNight = false,
            lastUpdatedMillis = System.currentTimeMillis()
        )

        val overview = GeminiWeatherService.synthesizeLocalOverview(sampleWeather)
        assertNotNull(overview)
        assertTrue(overview.todaySummary.isNotEmpty())
        assertTrue(overview.upcomingDaysSummary.isNotEmpty())
        assertTrue(overview.dressingAndActivityTip.isNotEmpty())
        assertTrue(overview.todaySummary.contains("London"))
    }

    @Test
    fun testTemperatureFormatterCelsiusAndFahrenheit() {
        val celsius = 20.0
        val fahrenheit = com.example.util.TemperatureFormatter.convert(celsius, com.example.util.TemperatureUnit.FAHRENHEIT)
        assertEquals(68.0, fahrenheit, 0.1)

        val formattedC = com.example.util.TemperatureFormatter.format(celsius, com.example.util.TemperatureUnit.CELSIUS)
        assertEquals("20°C", formattedC)

        val formattedF = com.example.util.TemperatureFormatter.format(celsius, com.example.util.TemperatureUnit.FAHRENHEIT)
        assertEquals("68°F", formattedF)

        val freezingC = 0.0
        assertEquals(32, com.example.util.TemperatureFormatter.toDisplayInt(freezingC, com.example.util.TemperatureUnit.FAHRENHEIT))
    }

    @Test
    fun testWeatherStaleCheckAndVersion() {
        val now = System.currentTimeMillis()
        val staleMillis = now - 3_600_001L // 1 hour and 1 ms ago
        val freshMillis = now - 1_800_000L // 30 minutes ago

        val isStale = (now - staleMillis) >= 3_600_000L
        val isFresh = (now - freshMillis) < 3_600_000L

        assertTrue(isStale)
        assertTrue(isFresh)

        val version = com.example.util.AppUpdateManager.getCurrentVersion()
        assertEquals("2.6.5", version)
    }
}
