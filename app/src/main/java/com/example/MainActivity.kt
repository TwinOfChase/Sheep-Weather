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
import com.example.ui.screens.HourlyDetailScreen
import com.example.ui.screens.MainWeatherScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = uiState.isNightEffective, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (uiState.currentScreen) {
                        WeatherScreen.Main -> {
                            MainWeatherScreen(
                                uiState = uiState,
                                onToggleStyle = { viewModel.toggleAppStyle() },
                                onToggleNight = { viewModel.toggleNightMode() },
                                onRefresh = { viewModel.refreshWeather() },
                                onSelectLocation = { viewModel.selectLocation(it) },
                                onAddLocation = { name, country, lat, lon ->
                                    viewModel.addLocation(name, country, lat, lon)
                                },
                                onDeleteLocation = { viewModel.deleteLocation(it) },
                                onOpenHourlyDetail = { viewModel.navigateToHourlyDetail() }
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

