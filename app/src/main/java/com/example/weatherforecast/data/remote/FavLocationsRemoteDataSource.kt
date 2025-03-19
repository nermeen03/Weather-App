package com.example.weatherforecast.data.remote

import android.util.Log
import kotlinx.coroutines.flow.flow

class FavLocationsRemoteDataSource(private val apiService: ApiService) : IFavLocationsRemoteDataSource {

    override suspend fun getLocation(lat: Double, lon: Double) = flow{
        val result = apiService.getLocationName(lat, lon)
        if (result.isSuccessful){
            emit(result.body()!!)
        }else{
            throw Exception(result.message())
        }
    }
    override suspend fun getMap(lat: Double, lon: Double){

    }
}