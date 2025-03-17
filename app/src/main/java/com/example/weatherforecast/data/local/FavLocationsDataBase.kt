package com.example.weatherforecast.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherforecast.data.pojo.Location

@Database(entities = [Location::class], version = 2)
abstract class FavLocationsDataBase: RoomDatabase() {

    abstract fun getFavLocationsDao():FavLocationsDao
    companion object{
        private var INSTANCE:FavLocationsDataBase?=null

        fun getInstance(ctx: Context):FavLocationsDataBase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext, FavLocationsDataBase::class.java, "app_dateBase")
                    .build()
                INSTANCE = instance
                instance }
        }
    }
}