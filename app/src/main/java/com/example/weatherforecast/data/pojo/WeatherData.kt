package com.example.weatherforecast.data.pojo

data class HourlyDetails(
    val time: String,
    val temp: Double,
    val feelLike: Double,
    val state: String
)

data class WeatherDetails(
    val temp: Double,
    val feelLike: Double,
    val weather: String,
    val place : Country,
    val pressure: Int,
    val humidity: Int,
    val speed: Double,
    val cloud: Int,
    val state: String
)
data class DailyDetails(val pressure: Int,
                        val humidity: Int,
                        val speed: Double,
                        val cloud: Int)