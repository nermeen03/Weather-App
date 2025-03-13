package com.example.weatherforecast.data.remote

import android.util.Log
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import retrofit2.Response

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
}