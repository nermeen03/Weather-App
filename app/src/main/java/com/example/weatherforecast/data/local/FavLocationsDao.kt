package com.example.weatherforecast.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.data.pojo.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface FavLocationsDao{
    @Query("Select * from locations")
    fun getAll(): Flow<List<Location>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: Location): Long

    @Query("Delete from locations where lon = :lon & lat = :lat")
    suspend fun delete(lon: Double,lat:Double): Int

}