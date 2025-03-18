package com.example.weatherforecast.data.remote

import com.example.weatherforecast.data.pojo.Country
import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import com.example.weatherforecast.data.pojo.Location
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
    ):Response<Country>

    @GET()
    suspend fun getMap(@Query("lat") lat: Double,
    @Query("lon") lon: Double,
    @Query("appid") apiKey: String = "2fc5f5f3f6a9b61df9391d8ae569f5e0"
    ):Response<Location>
}