package com.example.weatherforecast.data.remote

import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse

interface IDailyRemoteDataSource {
    suspend fun getDailyData(lat:Double,lon:Double): ForecastDataResponse?
    suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherResponse?
}