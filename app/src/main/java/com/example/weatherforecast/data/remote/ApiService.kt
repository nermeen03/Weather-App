package com.example.weatherforecast.data.remote

import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import com.example.weatherforecast.data.pojo.NameResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


// 2fc5f5f3f6a9b61df9391d8ae569f5e0

interface ApiService {

    @GET("data/2.5/forecast")
    suspend fun get5DaysEvery3HoursData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = "2fc5f5f3f6a9b61df9391d8ae569f5e0"
    ): Response<ForecastDataResponse>

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = "2fc5f5f3f6a9b61df9391d8ae569f5e0"
    ): Response<CurrentWeatherResponse>

    @GET("geo/1.0/reverse")
    suspend fun getLocationName(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = "2fc5f5f3f6a9b61df9391d8ae569f5e0"
    ):Response<NameResponse>

    @GET("data/2.5/weather?id=524901&lang=ar&appid=2fc5f5f3f6a9b61df9391d8ae569f5e0")
    suspend fun getArabicData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang:String = "ar",
        @Query("appid") apiKey: String = "2fc5f5f3f6a9b61df9391d8ae569f5e0"
    ):Response<CurrentWeatherResponse>
}
