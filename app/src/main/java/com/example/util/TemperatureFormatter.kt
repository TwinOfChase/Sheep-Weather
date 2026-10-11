package com.example.util

enum class TemperatureUnit(val symbol: String, val label: String) {
    CELSIUS("°C", "Celsius (°C)"),
    FAHRENHEIT("°F", "Fahrenheit (°F)")
}

object TemperatureFormatter {
    fun convert(celsius: Double, unit: TemperatureUnit): Double {
        return when (unit) {
            TemperatureUnit.CELSIUS -> celsius
            TemperatureUnit.FAHRENHEIT -> (celsius * 9.0 / 5.0) + 32.0
        }
    }

    fun toDisplayInt(celsius: Double, unit: TemperatureUnit): Int {
        return Math.round(convert(celsius, unit)).toInt()
    }

    fun format(celsius: Double, unit: TemperatureUnit, withUnitSymbol: Boolean = true): String {
        val converted = toDisplayInt(celsius, unit)
        return if (withUnitSymbol) {
            "$converted${unit.symbol}"
        } else {
            "$converted°"
        }
    }
}
