package com.example.weatherforecast.data.local.favorite

import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.Location
import kotlinx.coroutines.flow.Flow

interface IFavLocationsLocalDataSource {
    fun getAllFav(): Flow<List<Location>>

    suspend fun insertFav(location: Location): Long
    suspend fun deleteFav(lon: Double, lat: Double): Int
    suspend fun getFavDetail(lon: Double, lat: Double): Flow<FavDetails>
    suspend fun insertFavDetail(favDetails: FavDetails): Long
    suspend fun deleteFavDetails(lon: Double, lat: Double): Int
}