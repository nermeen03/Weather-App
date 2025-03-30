package com.example.weatherforecast.data.pojo

import androidx.room.Entity

@Entity(tableName = "locations", primaryKeys = ["lon", "lat"])
data class Location(
    val name: String,
    val country: String,
    val lon: Double,
    val lat: Double,
    var arabicName:String
)


data class Country(val name: String, val code: String)

class NameResponse : ArrayList<NameResponseItem>()

data class NameResponseItem(
    val country: String,
    val lat: Double,
    val local_names: LocalNames,
    val lon: Double,
    val name: String,
    val state: String
)

data class LocalNames(
    val be: String,
    val cy: String,
    val en: String,
    val fr: String,
    val he: String,
    val ko: String,
    val mk: String,
    val ru: String
)


data class CountriesListItem(
    val coord: Coord,
    val country: String,
    val id: Int,
    val name: String,
    val state: String
)
