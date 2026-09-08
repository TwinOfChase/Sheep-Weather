package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LocationEntity
import com.example.data.local.WeatherDatabase
import com.example.data.model.AppStyle
import com.example.data.model.MoonInfo
import com.example.data.model.WeatherInfo
import com.example.data.repository.WeatherRepository
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
    val currentScreen: WeatherScreen = WeatherScreen.Main,
    val errorMessage: String? = null
) {
    val isNightEffective: Boolean
        get() = isNightOverride ?: (weatherInfo?.isNight ?: false)
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WeatherDatabase.getInstance(application)
    private val repository = WeatherRepository(database.weatherDao())

    private val _uiState = MutableStateFlow(WeatherUiState(isLoading = true))
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
            val currentEffective = it.isNightEffective
            it.copy(isNightOverride = !currentEffective)
        }
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
