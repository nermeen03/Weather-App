package com.example.weatherforecast.data.local

import com.example.weatherforecast.data.pojo.Location
import kotlinx.coroutines.flow.Flow

interface IFavLocationsLocalDataSource {
    fun getAllFav(): Flow<List<Location>>

    suspend fun insertFav(location: Location): Long
    suspend fun deleteFav(locationId: Int): Int
}