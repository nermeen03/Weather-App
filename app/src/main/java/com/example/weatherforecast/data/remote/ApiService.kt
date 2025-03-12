package com.example.weatherforecast.data.remote

import retrofit2.Response
import retrofit2.http.GET


// 2fc5f5f3f6a9b61df9391d8ae569f5e0

interface ApiService {

    @GET("data/2.5/forecast?lat=44.34&lon=10.99&appid=2fc5f5f3f6a9b61df9391d8ae569f5e0")
    suspend fun get5DaysEvery3HoursData():Response<ForecastDataResponse>
}