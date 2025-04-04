package com.example.weatherforecast.data.repo


import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.DailyRemoteDataSource
import com.example.weatherforecast.data.remote.IDailyRemoteDataSource
import kotlinx.coroutines.flow.Flow

class DailyDataRepository(private val dailyRemoteDataSource: IDailyRemoteDataSource) :
    IDailyDataRepository {
    companion object {
        @Volatile
        private var INSTANCE: DailyDataRepository? = null

         fun getRepository(apiService: ApiService): DailyDataRepository {
            return INSTANCE ?: synchronized(this) {
                DailyDataRepository(DailyRemoteDataSource(apiService)).also {
                    INSTANCE = it
                }
            }
        }
    }
    override suspend fun getDailyData(lat:Double,lon:Double): Flow<ForecastDataResponse?> {
        return dailyRemoteDataSource.getDailyData(lat, lon)
    }
    override fun getCurrentWeather(lat:Double, lon:Double): Flow<CurrentWeatherResponse?> {
        return dailyRemoteDataSource.getCurrentWeather(lat, lon)
    }
    override fun getArabicData(lat:Double, lon:Double): Flow<CurrentWeatherResponse?> {
        return dailyRemoteDataSource.getArabicData(lat, lon)
    }
}