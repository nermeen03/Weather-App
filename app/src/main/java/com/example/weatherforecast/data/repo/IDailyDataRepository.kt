package com.example.weatherforecast.data.repo

import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import kotlinx.coroutines.flow.Flow

interface IDailyDataRepository {
    suspend fun getDailyData(lat:Double,lon:Double): Flow<ForecastDataResponse?>
    fun getCurrentWeather(lat: Double, lon: Double): Flow<CurrentWeatherResponse?>
    fun getArabicData(lat: Double, lon: Double): Flow<CurrentWeatherResponse?>
}