package com.example.weatherforecast.data.local

import android.content.Context
import com.example.weatherforecast.data.pojo.Location
import kotlinx.coroutines.flow.Flow

class FavLocationsLocalDataSource(private val favLocationsDao:FavLocationsDao) :
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
}