package com.example.weatherforecast.data.repo

import com.example.weatherforecast.data.local.alerts.IAlertsLocalDataSource
import com.example.weatherforecast.data.pojo.AlertsData
import kotlinx.coroutines.flow.Flow

class AlertsRepository(private val alertsLocalDataSource: IAlertsLocalDataSource) :
    IAlertsRepository {

    companion object {
        @Volatile
        private var INSTANCE: AlertsRepository? = null

        fun getRepository(alertsLocalDataSource: IAlertsLocalDataSource): AlertsRepository {
            return INSTANCE ?: synchronized(this) {
                AlertsRepository(alertsLocalDataSource).also {
                    INSTANCE = it
                }
            }
        }
    }

     override fun getAllAlerts(): Flow<List<AlertsData>> {
        return alertsLocalDataSource.getAll()
    }
    override fun getAlert(date:String, time:String, loc:String): AlertsData? {
        return alertsLocalDataSource.getAlert(date, time, loc)
    }
     override suspend fun insertAlert(alert: AlertsData):Long{
        return alertsLocalDataSource.insert(alert)
    }
     override suspend fun delete(start:String, end:String, location:String):Int{
        return alertsLocalDataSource.delete(start,end,location)
    }
}