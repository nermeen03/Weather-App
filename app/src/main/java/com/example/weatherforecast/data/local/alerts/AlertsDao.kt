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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alert: AlertsData): Long

    @Query("DELETE FROM alerts WHERE start = :start AND `end` = :end AND location = :location")
    suspend fun delete(start:String,end:String,location:String): Int
}