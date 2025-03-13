package com.example.weatherforecast.data.repo

import android.app.Application
import com.example.weatherforecast.data.pojo.ForecastDataResponse

interface IDailyDataRepository {
    suspend fun getDailyData(): ForecastDataResponse?
}