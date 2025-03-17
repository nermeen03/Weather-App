package com.example.weatherforecast.data.remote

import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import kotlinx.coroutines.flow.Flow

interface IDailyRemoteDataSource {
    fun getDailyData(lat:Double,lon:Double): Flow<ForecastDataResponse?>
    fun getCurrentWeather(lat: Double, lon: Double): Flow<CurrentWeatherResponse?>
}