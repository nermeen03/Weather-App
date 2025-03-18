package com.example.weatherforecast.data.pojo

import androidx.room.Entity

@Entity(tableName = "locations", primaryKeys = ["lon", "lat"])
data class Location(
    val name: String,
    val country: String,
    val lon: Double,
    val lat: Double
)


data class Country(val name: String, val code: String)
