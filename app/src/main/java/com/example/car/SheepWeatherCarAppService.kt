package com.example.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.ItemList
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.validation.HostValidator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.data.local.WeatherDatabase
import com.example.data.model.LayersForecast
import com.example.data.model.LayersForecastCalculator
import com.example.data.model.WeatherInfo
import com.example.data.remote.AiWeatherOverview
import com.example.data.remote.GeminiWeatherService
import com.example.data.repository.WeatherRepository
import com.example.util.WeatherSpeechSynthesizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Official Android Auto CarAppService that registers Sheep Weather in the vehicle's
 * Android Auto launcher alongside Spotify, Maps, and Messages.
 */
class SheepWeatherCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return SheepWeatherCarSession()
    }
}

class SheepWeatherCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return SheepWeatherCarQuadrantScreen(carContext)
    }
}

/**
 * Android Auto Head-Unit Screen presenting the 4 Quadrants, Center/Action Speak Aloud readout,
 * and Park Mode vs. Driving Mode detail adaptation.
 */
class SheepWeatherCarQuadrantScreen(carContext: CarContext) : Screen(carContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val database by lazy { WeatherDatabase.getInstance(carContext) }
    private val repository by lazy { WeatherRepository(database.weatherDao(), context = carContext) }
    private val speechSynthesizer by lazy { WeatherSpeechSynthesizer(carContext) }

    private var weatherInfo: WeatherInfo? = null
    private var layersForecast: LayersForecast? = null
    private var aiOverview: AiWeatherOverview? = null
    private var isParked: Boolean = true
    private var isLoading: Boolean = true
    private var showParkDetailPane: Boolean = false

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                loadWeatherForCar()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                speechSynthesizer.shutdown()
                scope.cancel()
            }
        })
    }

    private fun loadWeatherForCar() {
        scope.launch {
            isLoading = true
            invalidate()
            try {
                repository.initializeDefaultLocationsIfNeeded()
                val loc = repository.getSelectedLocation()
                val weather = repository.getWeatherForLocation(loc, forceRefresh = false)
                val layers = LayersForecastCalculator.calculate(weather.hourly, weather.current.temperature)
                val overview = GeminiWeatherService.synthesizeLocalOverview(weather)

                weatherInfo = weather
                layersForecast = layers
                aiOverview = overview
                isLoading = false
                invalidate()
            } catch (_: Exception) {
                isLoading = false
                invalidate()
            }
        }
    }

    private fun speakCurrentForecast() {
        val weather = weatherInfo ?: return
        val layers = layersForecast ?: LayersForecastCalculator.calculate(weather.hourly, weather.current.temperature)
        val script = WeatherSpeechSynthesizer.buildSpokenBriefing(
            weather = weather,
            layers = layers,
            aiOverview = aiOverview,
            isParked = isParked
        )
        speechSynthesizer.toggleSpeak(script)
        CarToast.makeText(
            carContext,
            "Speaking Forecast: ${weather.locationName} (${weather.current.temperature.toInt()}°C)",
            CarToast.LENGTH_LONG
        ).show()
        invalidate()
    }

    override fun onGetTemplate(): Template {
        val weather = weatherInfo
        if (isLoading || weather == null) {
            return PaneTemplate.Builder(
                Pane.Builder().setLoading(true).build()
            )
                .setTitle("Sheep Weather • Android Auto")
                .setHeaderAction(Action.APP_ICON)
                .build()
        }

        val layers = layersForecast ?: LayersForecastCalculator.calculate(weather.hourly, weather.current.temperature)
        val shortDayOverview = WeatherSpeechSynthesizer.buildShortDayProgression(weather, useDegreeSymbol = true)

        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle(if (speechSynthesizer.isSpeaking.value) "⏹ Stop" else "🔊 Speak")
                    .setBackgroundColor(CarColor.PRIMARY)
                    .setOnClickListener { speakCurrentForecast() }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle(if (isParked) "🅿️ Park" else "🚗 Drive")
                    .setOnClickListener {
                        isParked = !isParked
                        showParkDetailPane = isParked
                        CarToast.makeText(
                            carContext,
                            if (isParked) "Park Mode: Sunrise/Sunset, Full Hourly & Detailed AI enabled"
                            else "Drive Mode: 4-Quadrant Glanceable View",
                            CarToast.LENGTH_SHORT
                        ).show()
                        invalidate()
                    }
                    .build()
            )
            .build()

        // When in Park Mode and detail view is active, show expanded Park Mode Pane with Sunrise/Sunset,
        // full hourly forecast, and detailed AI overview
        if (showParkDetailPane && isParked) {
            val hourlyPreview = weather.hourly.take(12).joinToString(" • ") {
                "${it.displayHour}: ${it.temperature.toInt()}°"
            }
            val paneBuilder = Pane.Builder()
                .addRow(
                    Row.Builder()
                        .setTitle("1. ${weather.current.temperature.toInt()}°C (Feels ${weather.current.realFeel.toInt()}°C) • ${weather.current.conditionDescription}")
                        .addText("🌅 Sunrise ${weather.current.sunrise}  •  🌇 Sunset ${weather.current.sunset}")
                        .build()
                )
                .addRow(
                    Row.Builder()
                        .setTitle("2. Extended Hourly Forecast (Parked)")
                        .addText(hourlyPreview)
                        .build()
                )
                .addRow(
                    Row.Builder()
                        .setTitle("3. Outfit: ${layers.layerCount} Layers  •  ${getMinimalUmbrellaLabel(layers)}")
                        .build()
                )
                .addRow(
                    Row.Builder()
                        .setTitle("4. Detailed AI Overview (Today)")
                        .addText(aiOverview?.todaySummary ?: shortDayOverview)
                        .build()
                )
                .addAction(
                    Action.Builder()
                        .setTitle("🔊 Speak Full Briefing")
                        .setBackgroundColor(CarColor.PRIMARY)
                        .setOnClickListener { speakCurrentForecast() }
                        .build()
                )
                .addAction(
                    Action.Builder()
                        .setTitle("4-Quadrant Grid")
                        .setOnClickListener {
                            showParkDetailPane = false
                            invalidate()
                        }
                        .build()
                )

            return PaneTemplate.Builder(paneBuilder.build())
                .setTitle("${weather.locationName} • Park Mode")
                .setHeaderAction(Action.APP_ICON)
                .setActionStrip(actionStrip)
                .build()
        }

        // 4-Quadrant Grid Template for Android Auto Head Unit
        val fourHoursText = weather.hourly.take(4).joinToString("  ") {
            "${it.displayHour} ${it.temperature.toInt()}°"
        }

        val q1Subtitle = if (isParked) {
            "Feels ${weather.current.realFeel.toInt()}°C • 🌅${weather.current.sunrise} 🌇${weather.current.sunset}"
        } else {
            "Feels like ${weather.current.realFeel.toInt()}°C • ${weather.current.conditionDescription}"
        }

        val gridList = ItemList.Builder()
            // Quadrant 1: Temperature
            .addItem(
                GridItem.Builder()
                    .setImage(CarIcon.APP_ICON)
                    .setTitle("1. ${weather.current.temperature.toInt()}°C")
                    .setText(q1Subtitle)
                    .setOnClickListener { speakCurrentForecast() }
                    .build()
            )
            // Quadrant 2: Hourly (Max 4 hours in Drive, expandable in Park)
            .addItem(
                GridItem.Builder()
                    .setImage(CarIcon.APP_ICON)
                    .setTitle("2. Next 4 Hours")
                    .setText(fourHoursText)
                    .setOnClickListener {
                        if (isParked) {
                            showParkDetailPane = true
                            invalidate()
                        } else {
                            speakCurrentForecast()
                        }
                    }
                    .build()
            )
            // Quadrant 3: Layer / Outfit & Umbrella only
            .addItem(
                GridItem.Builder()
                    .setImage(CarIcon.APP_ICON)
                    .setTitle("3. 👕 ${layers.layerCount} Layers")
                    .setText(getMinimalUmbrellaLabel(layers))
                    .setOnClickListener { speakCurrentForecast() }
                    .build()
            )
            // Quadrant 4: AI Overview for Today
            .addItem(
                GridItem.Builder()
                    .setImage(CarIcon.APP_ICON)
                    .setTitle("4. Today AI")
                    .setText(shortDayOverview)
                    .setOnClickListener {
                        if (isParked) {
                            showParkDetailPane = true
                            invalidate()
                        } else {
                            speakCurrentForecast()
                        }
                    }
                    .build()
            )
            .build()

        return GridTemplate.Builder()
            .setSingleList(gridList)
            .setTitle("${weather.locationName} • ${if (isParked) "🅿️ Park" else "🚗 Drive"}")
            .setHeaderAction(Action.APP_ICON)
            .setActionStrip(actionStrip)
            .build()
    }

    private fun getMinimalUmbrellaLabel(layers: LayersForecast): String {
        return when {
            layers.isUmbrellaRequired && layers.isTooWindyForUmbrella -> "💨☂️ Too Windy"
            layers.isUmbrellaRequired -> "☂️ Umbrella"
            layers.isTooWindyForUmbrella -> "💨 No Umbrella"
            else -> "🌂 No Umbrella"
        }
    }
}
