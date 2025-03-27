package com.example.weatherforecast.data.remote


import com.example.weatherforecast.data.pojo.NameResponse
import kotlinx.coroutines.flow.Flow

interface IFavLocationsRemoteDataSource {
    suspend fun getLocation(lat: Double, lon: Double): Flow<NameResponse>
}