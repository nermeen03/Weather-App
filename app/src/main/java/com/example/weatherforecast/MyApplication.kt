package com.example.weatherforecast

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class MyApplication : Application() {

    private val _mutableLanguage = MutableStateFlow("en")
    val language: StateFlow<String> = _mutableLanguage.asStateFlow()
    private val _mutableLocation = MutableStateFlow("GPS")
    val location: StateFlow<String> = _mutableLocation.asStateFlow()
    private val _mutableTemp = MutableStateFlow("Kelvin")
    val temp: StateFlow<String> = _mutableTemp.asStateFlow()
    private val _mutableWind = MutableStateFlow("m/s")
    val wind: StateFlow<String> = _mutableWind.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        setLanguage(this,"en")
    }

    fun setLanguage(context: Context, langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun setLocation(lang: String) {
        _mutableLocation.value = lang
    }
    fun setTemp(str: String) {
        _mutableTemp.value = str
    }
    fun setWind(str: String) {
        _mutableWind.value = str
    }

    fun translateText(text: String):String {
        if (_mutableLanguage.value == "en") {
            return text
        }

        var message = text
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.ARABIC)
            .build()

        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText -> message = translatedText }
                    .addOnFailureListener { message = text }
            }
            .addOnFailureListener { message = text }
        Log.i("TAG", "translateText: $message")
        return message
    }

    fun convertTemperature(kelvin: Double): Double {
        return when (_mutableTemp.value) {
            "Celsius" -> kelvin - 273.15
            "Fahrenheit" -> ((kelvin - 273.15) * 9 / 5) + 32
            else -> kelvin
        }
    }

    fun translateChar(text: Char): String {
        if(_mutableLanguage.value == "en"){
            return text.toString()
        }

        val result = when (text) {
            'c', 'C' -> "س"
            'f', 'F' -> "ف"
            else -> "ك"
        }
        return result
    }

    fun convertWindSpeed(meterPerSec: Double): Double {
        return if (_mutableWind.value == "mph") meterPerSec * 2.23694 else meterPerSec
    }

    /*companion object {
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
    }*/
}
