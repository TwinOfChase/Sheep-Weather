package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val locationKey: String, // e.g. "lat_lon"
    val jsonPayload: String,
    val fetchedAtMillis: Long,
    val dataSizeKb: Double = 3.2
)
