package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiWeatherOverview(
    val todaySummary: String,
    val upcomingDaysSummary: String,
    val dressingAndActivityTip: String,
    val isLiveGemini: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)

object GeminiWeatherService {
    private const val TAG = "GeminiWeatherService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateOverview(weather: WeatherInfo): AiWeatherOverview = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // If no real API key is configured or it's a placeholder, synthesize intelligent overview
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.length < 10) {
            Log.d(TAG, "Gemini API key is placeholder or empty; generating smart local AI overview.")
            return@withContext synthesizeLocalOverview(weather)
        }

        try {
            val prompt = buildWeatherPrompt(weather)
            val requestJson = JSONObject().apply {
                val contentsArr = JSONArray()
                val contentObj = JSONObject()
                val partsArr = JSONArray()
                partsArr.put(JSONObject().put("text", prompt))
                contentObj.put("parts", partsArr)
                contentsArr.put(contentObj)
                put("contents", contentsArr)

                // Optional system instruction
                val systemObj = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", "You are the smart, friendly Sheep Weather AI. Provide concise, helpful weather overviews with structured headers."))
                systemObj.put("parts", sysParts)
                put("systemInstruction", systemObj)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.w(TAG, "Gemini call failed with code ${response.code}: $responseBody")
                return@withContext synthesizeLocalOverview(weather)
            }

            val parsed = parseGeminiResponse(responseBody, weather)
            parsed ?: synthesizeLocalOverview(weather)
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API: ${e.localizedMessage}", e)
            synthesizeLocalOverview(weather)
        }
    }

    private fun buildWeatherPrompt(weather: WeatherInfo): String {
        val hourly8 = weather.hourly.take(8)
        val minTemp8 = hourly8.minOfOrNull { it.temperature } ?: weather.current.temperature
        val maxTemp8 = hourly8.maxOfOrNull { it.temperature } ?: weather.current.temperature
        val maxRain8 = hourly8.maxOfOrNull { it.precipitationProbability } ?: 0

        val dailySummary = weather.daily.take(5).joinToString("\n") { day ->
            "- ${day.dayOfWeek}: ${day.tempMin.toInt()}°C to ${day.tempMax.toInt()}°C, ${day.conditionDescription}, rain chance ${day.precipitationProbability}%"
        }

        return """
            Location: ${weather.locationName}, ${weather.country}
            Current: ${weather.current.temperature.toInt()}°C (Real feel: ${weather.current.realFeel.toInt()}°C), ${weather.current.conditionDescription}, Humidity: ${weather.current.humidity}%, Wind: ${weather.current.windSpeed.toInt()} km/h.
            Next 8 Hours: Temp range ${minTemp8.toInt()}°C to ${maxTemp8.toInt()}°C, max rain probability ${maxRain8}%.
            Next Few Days:
            $dailySummary

            Please write a concise 3-part overview for the user:
            1. [TODAY]: 2 clear sentences summarizing today's overall weather, temperature feel, and conditions.
            2. [UPCOMING]: 2-3 sentences summarizing the trends, temperature shifts, and rain expectations for the next few days.
            3. [TIPS]: 1-2 practical tips on clothing layers, umbrella advice, and outdoor activity.

            Return the text clearly formatted with [TODAY], [UPCOMING], and [TIPS] section tags.
        """.trimIndent()
    }

    private fun parseGeminiResponse(responseBody: String, weather: WeatherInfo): AiWeatherOverview? {
        return try {
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val rawText = parts.getJSONObject(0).optString("text", "").trim()

            if (rawText.isBlank()) return null

            var today = ""
            var upcoming = ""
            var tips = ""

            val lines = rawText.split("\n")
            var currentSection = 0 // 1 = today, 2 = upcoming, 3 = tips
            val todaySb = java.lang.StringBuilder()
            val upcomingSb = java.lang.StringBuilder()
            val tipsSb = java.lang.StringBuilder()

            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.contains("[TODAY]", ignoreCase = true) || trimmed.startsWith("1.") || trimmed.contains("Today's Overview", ignoreCase = true) -> {
                        currentSection = 1
                        val clean = trimmed.replace(Regex("(?i)\\[TODAY\\]|1\\.|Today's Overview:?|\\*\\*Today:?\\*\\*"), "").trim()
                        if (clean.isNotEmpty()) todaySb.append(clean).append(" ")
                    }
                    trimmed.contains("[UPCOMING]", ignoreCase = true) || trimmed.startsWith("2.") || trimmed.contains("Upcoming", ignoreCase = true) || trimmed.contains("Next Few Days", ignoreCase = true) -> {
                        currentSection = 2
                        val clean = trimmed.replace(Regex("(?i)\\[UPCOMING\\]|2\\.|Upcoming:?|Next Few Days:?|\\*\\*Next Few Days:?\\*\\*"), "").trim()
                        if (clean.isNotEmpty()) upcomingSb.append(clean).append(" ")
                    }
                    trimmed.contains("[TIPS]", ignoreCase = true) || trimmed.startsWith("3.") || trimmed.contains("Tips", ignoreCase = true) || trimmed.contains("Clothing", ignoreCase = true) -> {
                        currentSection = 3
                        val clean = trimmed.replace(Regex("(?i)\\[TIPS\\]|3\\.|Tips:?|Clothing & Outdoor:?|\\*\\*Tips:?\\*\\*"), "").trim()
                        if (clean.isNotEmpty()) tipsSb.append(clean).append(" ")
                    }
                    else -> {
                        when (currentSection) {
                            1 -> if (trimmed.isNotEmpty()) todaySb.append(trimmed).append(" ")
                            2 -> if (trimmed.isNotEmpty()) upcomingSb.append(trimmed).append(" ")
                            3 -> if (trimmed.isNotEmpty()) tipsSb.append(trimmed).append(" ")
                        }
                    }
                }
            }

            today = todaySb.toString().trim()
            upcoming = upcomingSb.toString().trim()
            tips = tipsSb.toString().trim()

            if (today.isEmpty()) {
                today = rawText.take(240)
            }
            if (upcoming.isEmpty()) {
                upcoming = "Expect typical seasonal trends with temperature fluctuations over the coming days."
            }
            if (tips.isEmpty()) {
                tips = "Check the 8-hour layer forecast before heading outside."
            }

            AiWeatherOverview(
                todaySummary = today,
                upcomingDaysSummary = upcoming,
                dressingAndActivityTip = tips,
                isLiveGemini = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response: ${e.localizedMessage}")
            null
        }
    }

    fun synthesizeLocalOverview(weather: WeatherInfo): AiWeatherOverview {
        val curr = weather.current
        val hourly8 = weather.hourly.take(8)
        val minTemp8 = hourly8.minOfOrNull { it.temperature } ?: curr.temperature
        val maxTemp8 = hourly8.maxOfOrNull { it.temperature } ?: curr.temperature
        val maxRain8 = hourly8.maxOfOrNull { it.precipitationProbability } ?: 0
        val daily = weather.daily.take(5)

        // Today summary
        val tempFeelDesc = when {
            curr.temperature >= 26 -> "quite warm with highs reaching ${curr.temperature.toInt()}°C"
            curr.temperature in 18.0..25.9 -> "comfortable and pleasant at ${curr.temperature.toInt()}°C"
            curr.temperature in 10.0..17.9 -> "mild and crisp, currently sitting at ${curr.temperature.toInt()}°C"
            else -> "chilly with current temperatures around ${curr.temperature.toInt()}°C"
        }

        val rainNote = if (maxRain8 >= 35) {
            "Keep an eye out for rain showers (up to $maxRain8% chance in the next 8 hours)."
        } else {
            "Dry conditions are set to hold through the next 8 hours."
        }

        val todaySummary = "Today in ${weather.locationName} is $tempFeelDesc with ${curr.conditionDescription.lowercase()}. $rainNote Wind speeds remain moderate at ${curr.windSpeed.toInt()} km/h."

        // Upcoming days summary
        val futureTemps = daily.drop(1)
        val avgHigh = if (futureTemps.isNotEmpty()) futureTemps.map { it.tempMax }.average().toInt() else curr.temperature.toInt()
        val rainyDays = futureTemps.filter { it.precipitationProbability >= 35 }
        val upcomingText = if (rainyDays.isNotEmpty()) {
            val dayNames = rainyDays.joinToString(", ") { it.dayOfWeek }
            "Over the next few days, daytime highs will hover around ${avgHigh}°C. Showers are possible on $dayNames. The rest of the week will feature mixed intervals of sunshine and clouds."
        } else {
            "Fair and settled weather is expected over the next few days with afternoon highs averaging ${avgHigh}°C. Clear skies and gentle breezes will dominate through the weekend."
        }

        // Dressing & activity tips
        val layerTip = when {
            minTemp8 >= 23 -> "1 light layer (t-shirt) is plenty."
            minTemp8 in 17.0..22.9 -> "1–2 layers (t-shirt with light overshirt) are ideal."
            minTemp8 in 10.0..16.9 -> "2 layers (t-shirt + sweater or casual jacket) recommended."
            else -> "3 layers (thermal base, sweater, and warm coat) recommended."
        }

        val umbrellaTip = if (maxRain8 >= 25) "Don't forget an umbrella! ☂️" else "No umbrella needed today. ☀️"
        val tips = "$layerTip $umbrellaTip Excellent time for outdoor walks and sheep grazing during the drier afternoon hours."

        return AiWeatherOverview(
            todaySummary = todaySummary,
            upcomingDaysSummary = upcomingText,
            dressingAndActivityTip = tips,
            isLiveGemini = false
        )
    }
}
