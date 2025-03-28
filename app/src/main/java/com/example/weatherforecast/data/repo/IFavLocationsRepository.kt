package com.example.weatherforecast.data.repo

import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.pojo.NameResponse
import kotlinx.coroutines.flow.Flow

interface IFavLocationsRepository {
    fun getAllFav(): Flow<List<Location>>

    suspend fun insertFav(location: Location): Long
    suspend fun deleteFav(lat: Double, lon: Double): Int
    suspend fun getLocationName(lat: Double, lon: Double): Flow<NameResponse>
    suspend fun getFavDetail(lon: Double, lat: Double): Flow<FavDetails>
    suspend fun insertFavDetail(favDetails: FavDetails): Long
    suspend fun deleteFavDetails(lon: Double, lat: Double): Int
}