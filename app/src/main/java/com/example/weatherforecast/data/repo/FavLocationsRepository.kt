package com.example.weatherforecast.data.repo

import com.example.weatherforecast.data.local.favorite.FavLocationsLocalDataSource
import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.pojo.NameResponse
import com.example.weatherforecast.data.remote.FavLocationsRemoteDataSource
import kotlinx.coroutines.flow.Flow

class FavLocationsRepository(private val favLocationsLocalDataSource: FavLocationsLocalDataSource,
                             private val favLocationsRemoteDataSource: FavLocationsRemoteDataSource) :
    IFavLocationsRepository {
    companion object {
        @Volatile
        private var INSTANCE: FavLocationsRepository? = null

        fun getRepository(favLocationsLocalDataSource: FavLocationsLocalDataSource,
                          favLocationsRemoteDataSource: FavLocationsRemoteDataSource): FavLocationsRepository {
            return INSTANCE ?: synchronized(this) {
                FavLocationsRepository(favLocationsLocalDataSource,favLocationsRemoteDataSource).also {
                    INSTANCE = it
                }
            }
        }
    }

    override fun getAllFav(): Flow<List<Location>> {
        return favLocationsLocalDataSource.getAllFav()
    }
    override suspend fun insertFav(location: Location):Long{
        return favLocationsLocalDataSource.insertFav(location)
    }
    override suspend fun deleteFav(lat: Double, lon:Double):Int{
        return favLocationsLocalDataSource.deleteFav(lon,lat)
    }
    override suspend fun getLocationName(lat: Double, lon: Double): Flow<NameResponse>{
        return favLocationsRemoteDataSource.getLocation(lat, lon)
    }
    override suspend fun getFavDetail(lon: Double, lat: Double):Flow<FavDetails>{
        return favLocationsLocalDataSource.getFavDetail(lon, lat)
    }

    override suspend fun insertFavDetail(favDetails: FavDetails):Long{
        return favLocationsLocalDataSource.insertFavDetail(favDetails)
    }

    override suspend fun deleteFavDetails(lon: Double, lat: Double):Int{
        return favLocationsLocalDataSource.deleteFavDetails(lon, lat)
    }
}