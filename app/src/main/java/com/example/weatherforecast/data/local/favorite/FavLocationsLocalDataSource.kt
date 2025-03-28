package com.example.weatherforecast.data.local.favorite

import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.Location
import kotlinx.coroutines.flow.Flow

class FavLocationsLocalDataSource(private val favLocationsDao: FavLocationsDao) :
    IFavLocationsLocalDataSource {

    override fun getAllFav(): Flow<List<Location>> {
        return favLocationsDao.getAll()
    }

    override suspend fun insertFav(location: Location):Long{
        return favLocationsDao.insert(location)
    }

    override suspend fun deleteFav(lon: Double,lat:Double):Int{
        return favLocationsDao.delete(lon,lat)
    }

    override suspend fun getFavDetail(lon: Double, lat: Double):Flow<FavDetails>{
        return favLocationsDao.getFavDetails(lon, lat)
    }

    override suspend fun insertFavDetail(favDetails: FavDetails):Long{
        return favLocationsDao.saveFavDetails(favDetails)
    }

    override suspend fun deleteFavDetails(lon: Double, lat: Double):Int{
        return favLocationsDao.deleteFavDetails(lon, lat)
    }
}