package com.example.weatherforecast.data.remote

import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DailyRemoteDataSource(private var apiService: ApiService = RetrofitHelper.retrofitInstance.create(ApiService::class.java)) : IDailyRemoteDataSource {

    override fun getDailyData(lat: Double, lon: Double): Flow<ForecastDataResponse> = flow {
        val response = apiService.get5DaysEvery3HoursData(lat, lon)
        if (response.isSuccessful) {
            emit(response.body()!!)
        } else {
            throw Exception(response.message())
        }
    }

    override fun getCurrentWeather(lat: Double, lon: Double): Flow<CurrentWeatherResponse> = flow {
        val response = apiService.getCurrentWeather(lat, lon)
        if (response.isSuccessful) {
            emit(response.body()!!)
        } else {
            throw Exception(response.message())
        }
    }
}