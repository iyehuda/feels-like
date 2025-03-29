package com.iyehuda.feelslike.data.model

data class Weather(
    val temperature: Double,
    val condition: String,
    val locationName: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeed: Double
) 