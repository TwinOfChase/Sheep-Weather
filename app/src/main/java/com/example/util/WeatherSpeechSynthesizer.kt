package com.example.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.LayersForecast
import com.example.data.model.WeatherInfo
import com.example.data.remote.AiWeatherOverview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Formats natural spoken weather briefings for the Center Speak Button and Android Auto
 * voice readout, and manages Android's TextToSpeech lifecycle.
 */
class WeatherSpeechSynthesizer(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeechText: String? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var fallbackTimerJob: Job? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeSpokenScript = MutableStateFlow<String?>(null)
    val activeSpokenScript: StateFlow<String?> = _activeSpokenScript.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Throwable) {
            isInitialized = false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            tts?.setSpeechRate(0.98f)
            tts?.setPitch(1.02f)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    fallbackTimerJob?.cancel()
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    fallbackTimerJob?.cancel()
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    fallbackTimerJob?.cancel()
                    _isSpeaking.value = false
                }
            })

            pendingSpeechText?.let { textToSpeak ->
                pendingSpeechText = null
                speakInternal(textToSpeak)
            }
        } else {
            isInitialized = false
        }
    }

    fun toggleSpeak(script: String) {
        if (_isSpeaking.value) {
            stop()
        } else {
            speak(script)
        }
    }

    fun speak(script: String) {
        _activeSpokenScript.value = script
        if (!isInitialized || tts == null) {
            pendingSpeechText = script
            // Provide visual speaking state even if hardware TTS is still warming up or unavailable in container
            startSimulatedCaptionTimer(script)
            return
        }
        speakInternal(script)
    }

    private fun speakInternal(script: String) {
        fallbackTimerJob?.cancel()
        _isSpeaking.value = true
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val code = tts?.speak(script, TextToSpeech.QUEUE_FLUSH, params, "sheep_weather_car_briefing")
        // Ensure speaking state resets cleanly even on headless/emulator audio sinks
        startSimulatedCaptionTimer(script, onlyIfSilent = (code != TextToSpeech.SUCCESS))
    }

    private fun startSimulatedCaptionTimer(script: String, onlyIfSilent: Boolean = false) {
        fallbackTimerJob?.cancel()
        _isSpeaking.value = true
        val wordCount = script.split(Regex("\\s+")).size.coerceAtLeast(10)
        val durationMs = if (onlyIfSilent) (wordCount * 340L).coerceIn(5000L, 16000L) else (wordCount * 420L).coerceIn(7000L, 20000L)
        fallbackTimerJob = scope.launch {
            delay(durationMs)
            _isSpeaking.value = false
        }
    }

    fun stop() {
        fallbackTimerJob?.cancel()
        pendingSpeechText = null
        try {
            tts?.stop()
        } catch (_: Throwable) {}
        _isSpeaking.value = false
    }

    fun shutdown() {
        fallbackTimerJob?.cancel()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Throwable) {}
        tts = null
        _isSpeaking.value = false
    }

    companion object {
        /**
         * Builds the concise single-day progression shown in Quadrant 4 (Driving Mode) and read aloud by TTS.
         * Example:
         * "Wellington starts with clear skies and a cool 11 degrees, towards midday more overcast and temperatures of around 16. Showers starting early evening."
         */
        fun buildShortDayProgression(weather: WeatherInfo, useDegreeSymbol: Boolean = false): String {
            val unitSuffix = if (useDegreeSymbol) "°" else " degrees"
            val startHour = weather.hourly.firstOrNull()
            val middayHour = weather.hourly.getOrNull(3) ?: weather.hourly.getOrNull(2) ?: startHour
            val eveningHour = weather.hourly.getOrNull(7) ?: weather.hourly.getOrNull(5) ?: weather.hourly.lastOrNull()

            val startTemp = startHour?.temperature?.toInt() ?: weather.current.temperature.toInt()
            val midTemp = middayHour?.temperature?.toInt() ?: weather.current.temperature.toInt()
            val eveTemp = eveningHour?.temperature?.toInt() ?: startTemp

            val startFeelAdjective = when {
                startTemp <= 8 -> "chilly"
                startTemp <= 14 -> "cool"
                startTemp <= 20 -> "mild"
                startTemp <= 25 -> "pleasant"
                else -> "warm"
            }

            val startCond = formatConditionPhrase(startHour?.conditionDescription ?: weather.current.conditionDescription)
            val midCond = formatMiddayPhrase(
                startHour?.conditionDescription ?: weather.current.conditionDescription,
                middayHour?.conditionDescription ?: weather.current.conditionDescription
            )

            val eveRainProb = eveningHour?.precipitationProbability ?: 0
            val eveCondRaw = (eveningHour?.conditionDescription ?: "").lowercase(Locale.US)
            val eveningSentence = when {
                eveRainProb >= 30 || eveCondRaw.contains("rain") || eveCondRaw.contains("shower") || eveCondRaw.contains("drizzle") ->
                    "Showers starting early evening."
                eveCondRaw.contains("cloud") || eveCondRaw.contains("overcast") ->
                    "Staying partly cloudy into the evening around $eveTemp$unitSuffix."
                else ->
                    "Clear and settled conditions continuing into the evening around $eveTemp$unitSuffix."
            }

            val MidTempUnit = if (useDegreeSymbol) "$midTemp°" else "$midTemp"

            return "${weather.locationName} starts with $startCond and a $startFeelAdjective $startTemp$unitSuffix, towards midday $midCond and temperatures of around $MidTempUnit. $eveningSentence"
        }

        /**
         * Builds the full natural spoken briefing for the Center Speak Button:
         * Example:
         * "It's currently 16 degrees and feels like 12 degrees, 3 layers of clothing is recommended and an umbrella is not needed.
         *  Wellington starts with clear skies and a cool 11 degrees, towards midday more overcast and temperatures of around 16. Showers starting early evening."
         */
        fun buildSpokenBriefing(
            weather: WeatherInfo,
            layers: LayersForecast,
            aiOverview: AiWeatherOverview?,
            isParked: Boolean = false
        ): String {
            val currentTemp = weather.current.temperature.toInt()
            val feelsLike = weather.current.realFeel.toInt()

            val layerPhrase = if (layers.layerCount == 1) {
                "1 layer of clothing is recommended"
            } else {
                "${layers.layerCount} layers of clothing is recommended"
            }

            val umbrellaPhrase = when {
                layers.isUmbrellaRequired && layers.isTooWindyForUmbrella ->
                    "it is too windy for an umbrella so a waterproof jacket is recommended"
                layers.isUmbrellaRequired ->
                    "an umbrella is needed"
                layers.isTooWindyForUmbrella ->
                    "an umbrella is not needed and conditions are windy"
                else ->
                    "an umbrella is not needed"
            }

            val dayProgression = buildShortDayProgression(weather, useDegreeSymbol = false)

            val parkAddition = if (isParked) {
                " Sunrise today is at ${weather.current.sunrise} and sunset is at ${weather.current.sunset}."
            } else {
                ""
            }

            return "It's currently $currentTemp degrees and feels like $feelsLike degrees, $layerPhrase and $umbrellaPhrase. $dayProgression$parkAddition"
        }

        private fun formatConditionPhrase(desc: String): String {
            return when (desc.lowercase(Locale.US)) {
                "clear sky", "mainly clear" -> "clear skies"
                "partly cloudy" -> "partly cloudy skies"
                "overcast" -> "overcast skies"
                "light rain", "moderate rain", "rain showers" -> "light rain showers"
                else -> desc.lowercase(Locale.US)
            }
        }

        private fun formatMiddayPhrase(startDesc: String, midDesc: String): String {
            val startLow = startDesc.lowercase(Locale.US)
            val midLow = midDesc.lowercase(Locale.US)
            return when {
                midLow.contains("overcast") && !startLow.contains("overcast") -> "more overcast"
                midLow.contains("cloud") && startLow.contains("clear") -> "turning partly cloudy"
                midLow.contains("clear") && startLow.contains("cloud") -> "brightening with sunny spells"
                midLow.contains("rain") || midLow.contains("shower") -> "scattered showers developing"
                midLow.contains("overcast") -> "remaining overcast"
                midLow.contains("clear") -> "sunny skies holding"
                else -> midLow
            }
        }
    }
}
