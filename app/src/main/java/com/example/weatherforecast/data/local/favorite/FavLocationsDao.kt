package com.example.weatherforecast.data.local.favorite

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

    @Query("DELETE FROM locations WHERE lon = :lon AND lat = :lat")
    suspend fun delete(lon: Double, lat: Double): Int

}