package com.example.weatherforecast.data.local.alerts

import com.example.weatherforecast.data.pojo.AlertsData
import kotlinx.coroutines.flow.Flow

interface IAlertsLocalDataSource {
    fun getAll(): Flow<List<AlertsData>>
    fun getAlert(date:String, time:String, loc:String): AlertsData?

    suspend fun insert(alert: AlertsData): Long
    suspend fun delete(start: String, end: String, location: String): Int
}