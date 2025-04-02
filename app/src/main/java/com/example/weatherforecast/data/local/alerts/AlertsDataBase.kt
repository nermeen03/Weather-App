package com.example.weatherforecast.data.local.alerts

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherforecast.data.pojo.AlertsData

@Database(entities = [AlertsData::class], version = 5)
abstract class AlertsDataBase: RoomDatabase() {
    abstract fun getAlertsDao(): AlertsDao
    companion object{
        private var INSTANCE: AlertsDataBase?=null

        fun getInstance(ctx: Context): AlertsDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext, AlertsDataBase::class.java, "alerts_database")
                    .build()
                INSTANCE = instance
                instance }
        }
    }
}