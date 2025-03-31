package com.example.weatherforecast.data.local.alerts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.data.pojo.AlertsData
import kotlinx.coroutines.flow.Flow


@Dao
interface AlertsDao {
    @Query("Select * from alerts")
    fun getAll(): Flow<List<AlertsData>>

    @Query("SELECT * FROM alerts WHERE date = :date AND time = :time AND location = :loc LIMIT 1")
    fun getAlert(date: String, time: String, loc: String): AlertsData?


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alert: AlertsData): Long

    @Query("DELETE FROM alerts WHERE date = :date AND `time` = :time AND location = :location")
    suspend fun delete(date:String,time:String,location:String): Int
}