package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM favorite_locations ORDER BY id ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM favorite_locations ORDER BY id ASC")
    suspend fun getAllLocationsList(): List<LocationEntity>

    @Query("SELECT * FROM favorite_locations WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedLocation(): LocationEntity?

    @Query("SELECT * FROM favorite_locations WHERE id = :id")
    suspend fun getLocationById(id: Long): LocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Update
    suspend fun updateLocation(location: LocationEntity)

    @Query("UPDATE favorite_locations SET isSelected = 0")
    suspend fun clearSelectedLocation()

    @Query("UPDATE favorite_locations SET isSelected = 1 WHERE id = :id")
    suspend fun setSelectedLocation(id: Long)

    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    @Query("SELECT COUNT(*) FROM favorite_locations")
    suspend fun getLocationCount(): Int

    // Cache operations
    @Query("SELECT * FROM weather_cache WHERE locationKey = :key LIMIT 1")
    suspend fun getCachedWeather(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE locationKey = :key")
    suspend fun clearCache(key: String)
}
