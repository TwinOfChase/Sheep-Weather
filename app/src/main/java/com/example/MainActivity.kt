package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.WeatherScreen
import com.example.ui.WeatherViewModel
import com.example.ui.screens.AnimatedSplashScreen
import com.example.ui.screens.HourlyDetailScreen
import com.example.ui.screens.MainWeatherScreen
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = uiState.isNightEffective, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = uiState.isSplashVisible,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "appOpeningTransition"
                    ) { isSplash ->
                        if (isSplash) {
                            AnimatedSplashScreen(
                                isNight = uiState.isNightEffective,
                                isLoaded = uiState.isInitialLoadCompleted && uiState.weatherInfo != null,
                                onAnimationFinished = { viewModel.dismissSplash() }
                            )
                        } else {
                            when (uiState.currentScreen) {
                                WeatherScreen.Main -> {
                                    MainWeatherScreen(
                                        uiState = uiState,
                                        onRefresh = { viewModel.refreshWeather() },
                                        onSelectLocation = { viewModel.selectLocation(it) },
                                        onAddLocationByName = { name, country ->
                                            viewModel.addLocationByName(name, country)
                                        },
                                        onDeleteLocation = { viewModel.deleteLocation(it) },
                                        onOpenHourlyDetail = { viewModel.navigateToHourlyDetail() },
                                        onRefreshAi = { viewModel.refreshAiOverview() },
                                        onOpenSettings = { viewModel.openSettings() },
                                        onSetTemperatureUnit = { viewModel.setTemperatureUnit(it) },
                                        onSetAiEnabled = { viewModel.setAiEnabled(it) },
                                        onSetThemeMode = { viewModel.setThemeMode(it) },
                                        onSetAppStyle = { viewModel.setAppStyle(it) },
                                        onCheckForUpdates = { viewModel.checkForUpdates() },
                                        onDismissUpdateDialog = { viewModel.dismissUpdateDialog() },
                                        onToggleDataTracking = { viewModel.toggleDataTracking(it) },
                                        onToggleUseCurrentLocation = { viewModel.toggleUseCurrentLocation(it) },
                                        onFetchCurrentLocation = { viewModel.fetchDeviceLocation() },
                                        onCloseSettings = { viewModel.closeSettings() }
                                    )
                                }
                                WeatherScreen.HourlyDetail -> {
                                    BackHandler {
                                        viewModel.navigateBackToMain()
                                    }
                                    if (uiState.weatherInfo != null) {
                                        HourlyDetailScreen(
                                            weatherInfo = uiState.weatherInfo!!,
                                            isNight = uiState.isNightEffective,
                                            temperatureUnit = uiState.temperatureUnit,
                                            onBack = { viewModel.navigateBackToMain() }
                                        )
                                    } else {
                                        viewModel.navigateBackToMain()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

