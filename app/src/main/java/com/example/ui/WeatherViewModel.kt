package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LocationEntity
import com.example.data.local.WeatherDatabase
import com.example.data.model.AppStyle
import com.example.data.model.MoonInfo
import com.example.data.model.WeatherInfo
import com.example.data.remote.GeocodingResult
import com.example.data.repository.WeatherRepository
import com.example.util.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface WeatherScreen {
    object Main : WeatherScreen
    object HourlyDetail : WeatherScreen
}

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherInfo: WeatherInfo? = null,
    val appStyle: AppStyle = AppStyle.CARTOON_SHEEP,
    val locations: List<LocationEntity> = emptyList(),
    val selectedLocation: LocationEntity? = null,
    val moonInfo: MoonInfo = WeatherRepository.calculateMoonPhase(),
    val isNightOverride: Boolean? = null, // null = auto from forecast
    val isAutoThemeEnabled: Boolean = true,
    val isManualNight: Boolean = false,
    val isDataTrackingEnabled: Boolean = true,
    val isUseCurrentLocationEnabled: Boolean = false,
    val isSearchingCity: Boolean = false,
    val citySearchResults: List<GeocodingResult> = emptyList(),
    val currentScreen: WeatherScreen = WeatherScreen.Main,
    val isSplashVisible: Boolean = true,
    val isSettingsOpen: Boolean = false,
    val errorMessage: String? = null
) {
    val isNightEffective: Boolean
        get() = if (!isAutoThemeEnabled) {
            isManualNight
        } else {
            isNightOverride ?: (weatherInfo?.isNight ?: false)
        }
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WeatherDatabase.getInstance(application)
    private val repository = WeatherRepository(database.weatherDao(), context = application)
    private val prefs = application.getSharedPreferences("sheep_weather_user_settings", Context.MODE_PRIVATE)
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(
        WeatherUiState(
            isLoading = true,
            isAutoThemeEnabled = prefs.getBoolean("auto_theme_enabled", true),
            isManualNight = prefs.getBoolean("manual_night_mode", false),
            isDataTrackingEnabled = prefs.getBoolean("data_tracking_enabled", true),
            isUseCurrentLocationEnabled = prefs.getBoolean("use_current_location_enabled", false)
        )
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultLocationsIfNeeded()
            observeLocations()
        }
    }

    private fun observeLocations() {
        viewModelScope.launch {
            repository.allLocations.collect { locList ->
                val selected = locList.find { it.isSelected } ?: locList.firstOrNull()
                _uiState.update { it.copy(locations = locList, selectedLocation = selected) }
                if (selected != null) {
                    loadWeatherForLocation(selected, forceRefresh = false)
                }
            }
        }
    }

    fun loadWeatherForLocation(location: LocationEntity, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val weather = repository.getWeatherForLocation(location, forceRefresh)
                val moon = WeatherRepository.calculateMoonPhase()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        weatherInfo = weather,
                        moonInfo = moon,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to load weather"
                    )
                }
            }
        }
    }

    fun selectLocation(locationId: Long) {
        viewModelScope.launch {
            repository.selectLocation(locationId)
        }
    }

    fun addLocation(name: String, country: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            repository.addLocation(name, country, lat, lon)
        }
    }

    fun addLocationByName(cityName: String, country: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSearchingCity = false, citySearchResults = emptyList()) }
            try {
                repository.addLocationByName(cityName, country)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to add location: ${e.localizedMessage}") }
            }
        }
    }

    fun searchCity(query: String) {
        searchJob?.cancel()
        val q = query.trim()
        if (q.length < 2) {
            _uiState.update { it.copy(isSearchingCity = false, citySearchResults = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearchingCity = true) }
            val results = repository.searchCity(q)
            _uiState.update { it.copy(isSearchingCity = false, citySearchResults = results) }
        }
    }

    fun clearCitySearch() {
        searchJob?.cancel()
        _uiState.update { it.copy(isSearchingCity = false, citySearchResults = emptyList()) }
    }

    fun toggleUseCurrentLocation(enabled: Boolean, context: Context = getApplication()) {
        prefs.edit().putBoolean("use_current_location_enabled", enabled).apply()
        _uiState.update { it.copy(isUseCurrentLocationEnabled = enabled) }
        if (enabled) {
            fetchDeviceLocation(context)
        }
    }

    fun fetchDeviceLocation(context: Context = getApplication()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val loc = LocationHelper.getDeviceLocation(context)
                if (loc != null) {
                    repository.addCurrentDeviceLocation(
                        lat = loc.latitude,
                        lon = loc.longitude,
                        customName = "Current Location"
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteLocation(location: LocationEntity) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    fun toggleAppStyle() {
        _uiState.update {
            val nextStyle = if (it.appStyle == AppStyle.CARTOON_SHEEP) AppStyle.REALISTIC_SHEEP else AppStyle.CARTOON_SHEEP
            it.copy(appStyle = nextStyle)
        }
    }

    fun toggleNightMode() {
        _uiState.update {
            if (!it.isAutoThemeEnabled) {
                val newManual = !it.isManualNight
                prefs.edit().putBoolean("manual_night_mode", newManual).apply()
                it.copy(isManualNight = newManual)
            } else {
                val currentEffective = it.isNightEffective
                it.copy(isNightOverride = !currentEffective)
            }
        }
    }

    fun toggleAutoTheme(enabled: Boolean) {
        prefs.edit().putBoolean("auto_theme_enabled", enabled).apply()
        _uiState.update { it.copy(isAutoThemeEnabled = enabled) }
    }

    fun setManualNightMode(isNight: Boolean) {
        prefs.edit().putBoolean("manual_night_mode", isNight).apply()
        _uiState.update { it.copy(isManualNight = isNight) }
    }

    fun toggleDataTracking(enabled: Boolean) {
        prefs.edit().putBoolean("data_tracking_enabled", enabled).apply()
        _uiState.update { it.copy(isDataTrackingEnabled = enabled) }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun dismissSplash() {
        _uiState.update { it.copy(isSplashVisible = false) }
    }

    fun navigateToHourlyDetail() {
        _uiState.update { it.copy(currentScreen = WeatherScreen.HourlyDetail) }
    }

    fun navigateBackToMain() {
        _uiState.update { it.copy(currentScreen = WeatherScreen.Main) }
    }

    fun refreshWeather() {
        uiState.value.selectedLocation?.let {
            loadWeatherForLocation(it, forceRefresh = true)
        }
    }
}
