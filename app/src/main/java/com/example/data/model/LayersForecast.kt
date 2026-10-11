package com.example.data.model

data class LayersForecast(
    val layerCount: Int,
    val layerEmojiString: String,
    val layerTitle: String,
    val layerDescription: String,
    val clothingItems: List<String>,
    val isUmbrellaRequired: Boolean,
    val isTooWindyForUmbrella: Boolean,
    val umbrellaStatus: String,
    val umbrellaEmoji: String,
    val umbrellaNote: String,
    val minTemp: Double,
    val maxTemp: Double,
    val maxRainProb: Int,
    val maxWindSpeed: Double
)

object LayersForecastCalculator {

    /**
     * Threshold in km/h where wind becomes strong enough to invert, break,
     * or make handheld umbrellas dangerous/ineffective.
     */
    const val HIGH_WIND_UMBRELLA_THRESHOLD_KMH = 35.0

    fun calculate(hourlyList: List<HourlyWeather>, fallbackTemp: Double = 20.0): LayersForecast {
        // Reflect the next few hours (e.g., upcoming 4-5 hours)
        val nextFew = hourlyList.take(5).ifEmpty { hourlyList.take(3) }
        val minTemp = nextFew.minOfOrNull { it.temperature } ?: fallbackTemp
        val maxTemp = nextFew.maxOfOrNull { it.temperature } ?: fallbackTemp
        val minRealFeel = nextFew.minOfOrNull { it.realFeel } ?: minTemp
        val maxRainProb = nextFew.maxOfOrNull { it.precipitationProbability } ?: 0
        val maxWind = nextFew.maxOfOrNull { it.windSpeed } ?: 10.0

        // Determine rain presence across the next few hours
        val rainHour = nextFew.firstOrNull {
            it.precipitationProbability >= 25 || isRainCode(it.weatherCode)
        }
        val isRainExpected = maxRainProb >= 25 || rainHour != null
        val isTooWindy = maxWind >= HIGH_WIND_UMBRELLA_THRESHOLD_KMH

        // Effective feel takes the colder of temp and wind-chill real feel
        val effectiveMin = minOf(minTemp, minRealFeel)

        val (layerCount, layerTitle, layerDesc, clothingItems) = when {
            effectiveMin >= 24.0 -> {
                Quad(
                    1,
                    "1 Layer Recommended",
                    "Single light layer. Breathable and comfortable for warm conditions over the next few hours.",
                    listOf("T-Shirt", "Shorts / Light Pants")
                )
            }
            effectiveMin in 17.5..23.9 -> {
                Quad(
                    2,
                    "1–2 Layers Recommended",
                    "A light base with an optional overshirt or light layer handy.",
                    listOf("T-Shirt", "Light Overshirt / Cardigan")
                )
            }
            effectiveMin in 11.0..17.4 -> {
                Quad(
                    2,
                    "2 Layers Recommended",
                    "T-shirt combined with a sweater, hoodie, or casual windbreaker.",
                    listOf("Base T-Shirt", "Sweater / Hoodie", "Casual Jacket")
                )
            }
            effectiveMin in 4.0..10.9 -> {
                Quad(
                    3,
                    "3 Layers Recommended",
                    "Base layer, warm knit/fleece sweater, and a protective outer jacket.",
                    listOf("Base Layer", "Fleece / Knit Sweater", "Warm Coat / Jacket")
                )
            }
            else -> {
                Quad(
                    4,
                    "3–4 Layers Recommended",
                    "Heavy insulation required: thermal underlayer, sweater, heavy coat, and winter accessories.",
                    listOf("Thermal Underlayer", "Warm Sweater", "Insulated Winter Coat", "Beanie / Gloves")
                )
            }
        }

        val umbrellaStatus: String
        val umbrellaEmoji: String
        val umbrellaNote: String

        when {
            // Case 1: Rain expected, but too windy for an umbrella
            isRainExpected && isTooWindy -> {
                umbrellaStatus = "Too Windy for Umbrella"
                umbrellaEmoji = "💨 ☂️"
                val rainTimeStr = if (rainHour != null) " around ${rainHour.displayHour}" else ""
                umbrellaNote = "Rain expected$rainTimeStr (up to ${maxRainProb}%), but winds of ${maxWind.toInt()} km/h are too strong for an umbrella! Wear a waterproof hooded raincoat instead."
            }
            // Case 2: Rain expected and wind is safe/moderate
            isRainExpected -> {
                umbrellaStatus = "Umbrella Needed"
                umbrellaEmoji = "☂️"
                umbrellaNote = if (rainHour != null) {
                    "Rain expected around ${rainHour.displayHour} (up to ${maxRainProb}% chance). Moderate winds (${maxWind.toInt()} km/h) — keep an umbrella handy!"
                } else {
                    "${maxRainProb}% precipitation chance over the next few hours. Pack an umbrella just in case!"
                }
            }
            // Case 3: Dry, but very windy
            isTooWindy -> {
                umbrellaStatus = "No Umbrella Needed"
                umbrellaEmoji = "🌂"
                umbrellaNote = "Dry skies over the next few hours. Note: High winds of ${maxWind.toInt()} km/h — umbrellas would be blown around anyway!"
            }
            // Case 4: Dry and calm
            else -> {
                umbrellaStatus = "No Umbrella Needed"
                umbrellaEmoji = "🌂"
                umbrellaNote = "Dry skies expected over the next few hours (rain chance under 25%). Leave the umbrella at home!"
            }
        }

        return LayersForecast(
            layerCount = layerCount,
            layerEmojiString = "👕 $layerCount",
            layerTitle = layerTitle,
            layerDescription = layerDesc,
            clothingItems = clothingItems,
            isUmbrellaRequired = isRainExpected,
            isTooWindyForUmbrella = isTooWindy,
            umbrellaStatus = umbrellaStatus,
            umbrellaEmoji = umbrellaEmoji,
            umbrellaNote = umbrellaNote,
            minTemp = minTemp,
            maxTemp = maxTemp,
            maxRainProb = maxRainProb,
            maxWindSpeed = maxWind
        )
    }

    private fun isRainCode(code: Int): Boolean {
        return code in 51..67 || code in 80..82 || code in 95..99
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
