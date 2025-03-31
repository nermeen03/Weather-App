package com.example.weatherforecast.data.local.alerts

import com.example.weatherforecast.data.pojo.AlertsData
import kotlinx.coroutines.flow.Flow

class AlertsLocalDataSource(private val alertsDao: AlertsDao) : IAlertsLocalDataSource {

     override fun getAll(): Flow<List<AlertsData>> {
        return alertsDao.getAll()
    }

    override fun getAlert(date: String, time: String, loc: String): AlertsData? {
        return alertsDao.getAlert(date, time, loc)
    }

    override suspend fun insert(alert: AlertsData):Long{
        return alertsDao.insert(alert)
    }

     override suspend fun delete(start:String, end:String, location:String):Int{
        return alertsDao.delete(start,end,location)
    }

}