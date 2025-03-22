package com.example.weatherforecast.data.pojo

import androidx.room.Entity


@Entity(tableName = "alerts", primaryKeys = ["start","end","location"])
data class AlertsData(
    val start:String, val end:String,
    val location:String, val lat:Double,
    val lon:Double,val type:String)