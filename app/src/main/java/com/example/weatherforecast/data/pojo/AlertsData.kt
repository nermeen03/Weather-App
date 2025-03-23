package com.example.weatherforecast.data.pojo

import androidx.room.Entity


@Entity(tableName = "alerts", primaryKeys = ["date","time","location"])
data class AlertsData(
    val date:String, val time:String,
    val location:String, val lat:Double,
    val lon:Double, val type: Boolean
)