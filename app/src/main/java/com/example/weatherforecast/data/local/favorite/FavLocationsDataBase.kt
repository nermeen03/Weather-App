package com.example.weatherforecast.data.local.favorite

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherforecast.data.pojo.ArabicDataConverter
import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.HourlyDetailsConverter
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.pojo.WeatherDetailsConverter

@Database(entities = [Location::class,FavDetails::class], version = 6, exportSchema = false)
@TypeConverters(
    WeatherDetailsConverter::class,
    HourlyDetailsConverter::class,
    ArabicDataConverter::class
)
abstract class FavLocationsDataBase: RoomDatabase() {

    abstract fun getFavLocationsDao(): FavLocationsDao
    companion object{
        @Volatile
        private var INSTANCE: FavLocationsDataBase?=null

        fun getInstance(ctx: Context): FavLocationsDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext, FavLocationsDataBase::class.java, "fav_locations_database")
                    .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance }
        }
    }

}