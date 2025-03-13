package com.example.weatherforecast.data.remote

import com.example.weatherforecast.data.pojo.ForecastDataResponse

interface IDailyRemoteDataSource {
    suspend fun getDailyData(): ForecastDataResponse?
}