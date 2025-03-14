package com.example.weatherforecast.data.remote

import android.util.Log
import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse

class DailyRemoteDataSource : IDailyRemoteDataSource {

    private var apiService: ApiService = RetrofitHelper.retrofitInstance.create(ApiService::class.java)

    override suspend fun getDailyData(): ForecastDataResponse? {
        val response = apiService.get5DaysEvery3HoursData()
        if (response.isSuccessful) {
            return response.body()
        } else {
            Log.e("API_ERROR", "Error: ${response.errorBody()?.string()}")
            return null
        }
    }
    override suspend fun getCurrentWeather(lat:Double,lon:Double):CurrentWeatherResponse?{
        val response = apiService.getCurrentWeather(lat,lon)
        if(response.isSuccessful){
            return response.body()
        } else {
            Log.e("API_ERROR", "Error: ${response.errorBody()?.string()}")
            return null
        }
    }
}