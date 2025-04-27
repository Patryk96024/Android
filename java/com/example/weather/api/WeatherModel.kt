package com.example.weather.api

data class WeatherModel(
    val current: Current,
    val location: Location,
    val forecast: Forecast
)