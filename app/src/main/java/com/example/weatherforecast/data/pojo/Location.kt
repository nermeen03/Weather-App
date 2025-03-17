package com.example.weatherforecast.data.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,
    val name:String,
    val lon:Double,val lat:Double,val country:String,)