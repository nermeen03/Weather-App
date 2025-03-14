package com.example.weatherforecast.data.repo

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import com.example.weatherforecast.data.remote.DailyRemoteDataSource
import com.example.weatherforecast.data.remote.IDailyRemoteDataSource

class DailyDataRepository(private val dailyRemoteDataSource: IDailyRemoteDataSource?) :
    IDailyDataRepository {
    companion object {
        @Volatile
        private var INSTANCE: DailyDataRepository? = null

         fun getRepository(): DailyDataRepository {
            return INSTANCE ?: synchronized(this) {
                DailyDataRepository(DailyRemoteDataSource()).also {
                    INSTANCE = it
                }
            }
        }
    }
    override suspend fun getDailyData(): ForecastDataResponse? {
        return dailyRemoteDataSource?.getDailyData()
    }
    override suspend fun getCurrentweather(lat:Double,lon:Double):CurrentWeatherResponse?{
        return dailyRemoteDataSource?.getCurrentWeather(lat, lon)
    }
}