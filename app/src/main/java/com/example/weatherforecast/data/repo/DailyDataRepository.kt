package com.example.weatherforecast.data.repo


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
    override suspend fun getDailyData(lat:Double,lon:Double): ForecastDataResponse? {
        return dailyRemoteDataSource?.getDailyData(lat, lon)
    }
    override suspend fun getCurrentWeather(lat:Double, lon:Double):CurrentWeatherResponse?{
        return dailyRemoteDataSource?.getCurrentWeather(lat, lon)
    }
}