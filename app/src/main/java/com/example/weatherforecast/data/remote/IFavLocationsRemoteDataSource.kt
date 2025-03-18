package com.example.weatherforecast.data.remote

import com.example.weatherforecast.data.pojo.Country
import com.example.weatherforecast.data.pojo.Location
import kotlinx.coroutines.flow.Flow

interface IFavLocationsRemoteDataSource {
    suspend fun getLocation(lat: Double, lon: Double): Flow<Country>
    suspend fun getMap(lat: Double, lon: Double)
}