package com.example.weatherforecast.data.repo

import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse

interface IDailyDataRepository {
    suspend fun getDailyData(): ForecastDataResponse?
    suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherResponse?
}