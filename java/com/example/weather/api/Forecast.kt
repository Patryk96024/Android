package com.example.weather.api

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val hour: List<HourWeather>
)

data class HourWeather(
    val time: String,
    val temp_c: Double,
    val condition: Condition
)