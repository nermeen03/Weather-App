package com.example.weatherforecast.data.pojo

import androidx.room.Entity
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable


@Entity(tableName = "fav_details", primaryKeys = ["lon", "lat"])
@TypeConverters(
    WeatherDetailsConverter::class,
    HourlyDetailsConverter::class,
    ArabicDataConverter::class
)

data class FavDetails(
    val currentWeather: WeatherDetails,
    val hourlyWeather: List<HourlyDetails>,
    val dailyWeather: List<HourlyDetails>,
    val lat: Double,
    val lon: Double,
    val location: String,
    val arabicData: List<String>
): Serializable


class WeatherDetailsConverter {
    @TypeConverter
    fun fromWeatherDetails(details: WeatherDetails): String {
        return Gson().toJson(details)
    }

    @TypeConverter
    fun toWeatherDetails(value: String): WeatherDetails {
        return Gson().fromJson(value, WeatherDetails::class.java)
    }
}

class HourlyDetailsConverter {
    @TypeConverter
    fun fromHourlyList(value: List<HourlyDetails>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toHourlyList(value: String): List<HourlyDetails> {
        val listType = object : TypeToken<List<HourlyDetails>>() {}.type
        return Gson().fromJson(value, listType)
    }
}

class ArabicDataConverter {
    @TypeConverter
    fun fromList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
