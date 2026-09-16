package com.example.data.model

data class WeatherInfo(
    val locationName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    val isNight: Boolean,
    val lastUpdatedMillis: Long,
    val isFromCache: Boolean = false,
    val dataSizeKb: Double = 3.2
)

data class CurrentWeather(
    val time: String,
    val temperature: Double,
    val realFeel: Double,
    val weatherCode: Int,
    val conditionDescription: String,
    val isDay: Boolean,
    val humidity: Int,
    val windSpeed: Double,
    val uvIndex: Double,
    val cloudCover: Int,
    val sunrise: String = "6:15 AM",
    val sunset: String = "7:45 PM"
)

data class HourlyWeather(
    val timeIso: String,
    val displayHour: String, // e.g. "10 AM"
    val temperature: Double,
    val realFeel: Double,
    val weatherCode: Int,
    val conditionDescription: String,
    val isDay: Boolean,
    val precipitationProbability: Int,
    val windSpeed: Double,
    val humidity: Int,
    val uvIndex: Double,
    val cloudCover: Int
)

data class DailyWeather(
    val dateIso: String,
    val dayOfWeek: String, // e.g. "Today", "Tue", "Wed"
    val formattedDate: String, // e.g. "Sep 8"
    val weatherCode: Int,
    val conditionDescription: String,
    val tempMax: Double,
    val tempMin: Double,
    val realFeelMax: Double,
    val realFeelMin: Double,
    val precipitationProbability: Int,
    val sunrise: String,
    val sunset: String
)

enum class AppStyle {
    CARTOON_SHEEP,
    REALISTIC_SHEEP
}

enum class HourlyConditionType(val displayName: String, val unit: String) {
    TEMPERATURE("Temperature", "°C"),
    PRECIPITATION("Rain Chance", "%"),
    WIND_SPEED("Wind Speed", "km/h"),
    HUMIDITY("Humidity", "%"),
    UV_INDEX("UV Index", ""),
    CLOUD_COVER("Cloud Cover", "%")
}

data class MoonInfo(
    val phaseName: String,
    val illuminationPercent: Int,
    val phaseFraction: Double // 0.0 = New, 0.5 = Full
)
