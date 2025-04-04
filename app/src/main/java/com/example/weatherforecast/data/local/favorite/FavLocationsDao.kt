package com.example.weatherforecast.data.local.favorite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface FavLocationsDao{
    @Query("Select * from locations")
    fun getAll(): Flow<List<Location>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location): Long

    @Query("DELETE FROM locations WHERE lon = :lon AND lat = :lat")
    suspend fun delete(lon: Double, lat: Double): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavDetails(favDetails: FavDetails):Long

    @Query("SELECT * FROM fav_details WHERE ABS(lon - :lon) < 0.000001 AND ABS(lat - :lat) < 0.000001")
    fun getFavDetails(lon: Double, lat: Double): Flow<FavDetails>

    @Query("DELETE FROM fav_details WHERE ABS(lon - :lon) < 0.00001 AND ABS(lat - :lat) < 0.00001")
    suspend fun deleteFavDetails(lon: Double, lat: Double): Int


}