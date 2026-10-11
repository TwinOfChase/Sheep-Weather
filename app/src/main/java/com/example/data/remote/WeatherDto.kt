package com.example.data.remote

import com.squareup.moshi.Json

data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val current: CurrentUnitsDto?,
    val hourly: HourlyDto?,
    val daily: DailyDto?
)

data class CurrentUnitsDto(
    val time: String?,
    @Json(name = "temperature_2m") val temperature2m: Double?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int?,
    @Json(name = "apparent_temperature") val apparentTemperature: Double?,
    @Json(name = "is_day") val isDay: Int?,
    @Json(name = "precipitation") val precipitation: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int?,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double?
)

data class HourlyDto(
    val time: List<String>?,
    @Json(name = "temperature_2m") val temperature2m: List<Double>?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Int>?,
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double>?,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>?,
    @Json(name = "precipitation") val precipitation: List<Double>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>?,
    @Json(name = "uv_index") val uvIndex: List<Double>?,
    @Json(name = "cloud_cover") val cloudCover: List<Int>?
)

data class DailyDto(
    val time: List<String>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>?,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>?,
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double>?,
    @Json(name = "apparent_temperature_min") val apparentTemperatureMin: List<Double>?,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>? = null,
    val sunrise: List<String>?,
    val sunset: List<String>?
)
