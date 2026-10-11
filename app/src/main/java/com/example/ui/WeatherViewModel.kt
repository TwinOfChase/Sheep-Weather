package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LocationEntity
import com.example.data.local.WeatherDatabase
import com.example.data.model.AppStyle
import com.example.data.model.LayersForecast
import com.example.data.model.LayersForecastCalculator
import com.example.data.model.MoonInfo
import com.example.data.model.WeatherInfo
import com.example.data.remote.AiWeatherOverview
import com.example.data.remote.GeminiWeatherService
import com.example.data.remote.GeocodingResult
import com.example.data.repository.WeatherRepository
import com.example.util.AppUpdateManager
import com.example.util.LocationHelper
import com.example.util.TemperatureFormatter
import com.example.util.TemperatureUnit
import com.example.util.UpdateCheckResult
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

enum class ThemeMode { AUTO, LIGHT, DARK }

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherInfo: WeatherInfo? = null,
    val appStyle: AppStyle = AppStyle.CARTOON_SHEEP,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val isAiEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val locations: List<LocationEntity> = emptyList(),
    val selectedLocation: LocationEntity? = null,
    val moonInfo: MoonInfo = WeatherRepository.calculateMoonPhase(),
    val isDataTrackingEnabled: Boolean = true,
    val isUseCurrentLocationEnabled: Boolean = false,
    val isSearchingCity: Boolean = false,
    val citySearchResults: List<GeocodingResult> = emptyList(),
    val currentScreen: WeatherScreen = WeatherScreen.Main,
    val isSplashVisible: Boolean = true,
    val isInitialLoadCompleted: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val layersForecast: LayersForecast? = null,
    val aiOverview: AiWeatherOverview? = null,
    val isAiLoading: Boolean = false,
    val isCheckingUpdates: Boolean = false,
    val updateCheckResult: UpdateCheckResult? = null,
    val errorMessage: String? = null
) {
    val isNightEffective: Boolean
        get() = when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.AUTO -> weatherInfo?.isNight ?: false
        }

    val isAutoThemeEnabled: Boolean
        get() = themeMode == ThemeMode.AUTO

    val isManualNight: Boolean
        get() = themeMode == ThemeMode.DARK
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WeatherDatabase.getInstance(application)
    private val repository = WeatherRepository(database.weatherDao(), context = application)
    private val prefs = application.getSharedPreferences("sheep_weather_user_settings", Context.MODE_PRIVATE)
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(
        WeatherUiState(
            isLoading = true,
            temperatureUnit = try {
                TemperatureUnit.valueOf(prefs.getString("temperature_unit", TemperatureUnit.CELSIUS.name) ?: TemperatureUnit.CELSIUS.name)
            } catch (_: Exception) { TemperatureUnit.CELSIUS },
            isAiEnabled = prefs.getBoolean("is_ai_enabled", true),
            themeMode = try {
                val savedMode = prefs.getString("theme_mode", null)
                if (savedMode != null) {
                    ThemeMode.valueOf(savedMode)
                } else if (!prefs.getBoolean("auto_theme_enabled", true)) {
                    if (prefs.getBoolean("manual_night_mode", false)) ThemeMode.DARK else ThemeMode.LIGHT
                } else {
                    ThemeMode.AUTO
                }
            } catch (_: Exception) { ThemeMode.AUTO },
            appStyle = try {
                AppStyle.valueOf(prefs.getString("app_style", AppStyle.CARTOON_SHEEP.name) ?: AppStyle.CARTOON_SHEEP.name)
            } catch (_: Exception) { AppStyle.CARTOON_SHEEP },
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
                val layers = LayersForecastCalculator.calculate(weather.hourly, weather.current.temperature)
                val isAi = _uiState.value.isAiEnabled
                val initialAi = if (isAi) GeminiWeatherService.synthesizeLocalOverview(weather) else null

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isInitialLoadCompleted = true,
                        weatherInfo = weather,
                        moonInfo = moon,
                        layersForecast = layers,
                        aiOverview = initialAi,
                        errorMessage = null
                    )
                }

                if (isAi) {
                    fetchGeminiOverview(weather)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isInitialLoadCompleted = true,
                        errorMessage = e.localizedMessage ?: "Failed to load weather"
                    )
                }
            }
        }
    }

    private fun fetchGeminiOverview(weather: WeatherInfo) {
        if (!_uiState.value.isAiEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            try {
                val overview = GeminiWeatherService.generateOverview(weather)
                _uiState.update { it.copy(aiOverview = overview, isAiLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false) }
            }
        }
    }

    fun refreshAiOverview() {
        if (!_uiState.value.isAiEnabled) return
        val weather = _uiState.value.weatherInfo ?: return
        fetchGeminiOverview(weather)
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

    fun setTemperatureUnit(unit: TemperatureUnit) {
        prefs.edit().putString("temperature_unit", unit.name).apply()
        _uiState.update { it.copy(temperatureUnit = unit) }
    }

    fun setAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_ai_enabled", enabled).apply()
        _uiState.update { state ->
            val newOverview = if (enabled && state.weatherInfo != null) {
                GeminiWeatherService.synthesizeLocalOverview(state.weatherInfo)
            } else null
            state.copy(isAiEnabled = enabled, aiOverview = newOverview)
        }
        if (enabled && _uiState.value.weatherInfo != null) {
            fetchGeminiOverview(_uiState.value.weatherInfo!!)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setAppStyle(style: AppStyle) {
        prefs.edit().putString("app_style", style.name).apply()
        _uiState.update { it.copy(appStyle = style) }
    }

    fun toggleDataTracking(enabled: Boolean) {
        prefs.edit().putBoolean("data_tracking_enabled", enabled).apply()
        _uiState.update { it.copy(isDataTrackingEnabled = enabled) }
    }

    fun checkForUpdates(customRepo: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdates = true, updateCheckResult = null) }
            val result = AppUpdateManager.checkForUpdates(customRepo)
            _uiState.update { it.copy(isCheckingUpdates = false, updateCheckResult = result) }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(updateCheckResult = null, isCheckingUpdates = false) }
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
