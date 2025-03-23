package com.example.weatherforecast

import android.app.Application
import android.content.Context

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
            private set

        private const val PREF_NAME = "AppPreferences"

        fun setPreference(key: String, value: String) {
            val sharedPref = instance.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            sharedPref.edit().putString(key, value).apply()
        }

        fun getPreference(key: String, defaultValue: String = ""): String {
            val sharedPref = instance.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString(key, defaultValue) ?: defaultValue
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
