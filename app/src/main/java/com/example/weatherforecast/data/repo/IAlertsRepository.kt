package com.example.weatherforecast.data.repo

import com.example.weatherforecast.data.pojo.AlertsData
import kotlinx.coroutines.flow.Flow

interface IAlertsRepository {
    fun getAllAlerts(): Flow<List<AlertsData>>

    suspend fun insertAlert(alert: AlertsData): Long
    suspend fun delete(start: String, end: String, location: String): Int
    fun getAlert(date: String, time: String, loc: String): AlertsData?
}