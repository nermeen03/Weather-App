package com.example.weatherforecast.data.repo

import android.app.Application
import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse

interface IDailyDataRepository {
    suspend fun getDailyData(): ForecastDataResponse?
    suspend fun getCurrentweather(lat: Double, lon: Double): CurrentWeatherResponse?
}