package com.example.weatherforecast

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyApplication : Application() {

    private val _mutableLocation = MutableStateFlow("GPS")
    val location: StateFlow<String> = _mutableLocation.asStateFlow()
    private val _mutableCurrentLocation = MutableStateFlow(Pair(-1.0,-1.0))
    val currentLocation: StateFlow<Pair<Double, Double>> = _mutableCurrentLocation.asStateFlow()
    private val _mutableTemp = MutableStateFlow("K")
    val temp: StateFlow<String> = _mutableTemp.asStateFlow()
    private val _mutableWind = MutableStateFlow("m/s")
    val wind: StateFlow<String> = _mutableWind.asStateFlow()

    var reStarted = false

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val storedLang = sharedPreferences.getString("LANGUAGE", "default")
        Log.i("LANG_CHECK", "Stored Language: $storedLang")

        if (!sharedPreferences.contains("LANGUAGE")) {
            setLanguage(this, "en")
            saveLanguagePreference(this, "en")
            Log.i("LANG_CHECK", "No language found, setting default to English")
        } else {
            val langCode = storedLang ?: "en"
            setLanguage(this, langCode)
            Log.i("LANG_CHECK", "Using stored language: $langCode")
        }
    }



    fun setLanguage(context: Context, langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        saveLanguagePreference(context, langCode)

    }




    fun setRestarted(){
        reStarted = true
    }

    fun setLocation(lang: String) {
        _mutableLocation.value = lang
    }
    fun setCurrentLocation(loc: Pair<Double, Double>) {
        if (_mutableCurrentLocation.value != loc) {
            _mutableCurrentLocation.value = loc
        }
    }

    fun setTemp(str: String) {
        _mutableTemp.value = str
    }
    fun setWind(str: String) {
        _mutableWind.value = str
    }

    private fun convertToArabicNumbers(number: Double?): String? {
        number?:let {
            return null
        }
        if (Locale.getDefault().language == "en") {
            return number.toString() 
        }
        val arabicLocale = Locale("ar")
        return NumberFormat.getInstance(arabicLocale).format(number) 
    }
    fun convertToArabicNumbers(number: Int?): String? {
        number?:let {
            return null
        }
        if (Locale.getDefault().language == "en") {
            return number.toString()
        }
        val arabicLocale = Locale("ar")
        return NumberFormat.getInstance(arabicLocale).format(number)
    }

    fun convertTimeToArabic(time: String, inputFormat: String = "HH:mm"): String {
        if (Locale.getDefault().language == "en") {
            return time
        }

        val locale = Locale("ar")
        val sdf = SimpleDateFormat(inputFormat, Locale.ENGLISH)
        val date: Date = sdf.parse(time) ?: return time
        val arabicFormat = SimpleDateFormat("HH:mm", locale)
        return convertNumbersInString(arabicFormat.format(date))
    }
    private fun convertNumbersInString(text: String): String {
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
        return text.map { char ->
            if (char.isDigit()) arabicDigits[char.toString().toInt()] else char
        }.joinToString("")
    }

    fun convertDateToArabic(dateString: String, inputFormat: String = "yyyy-MM-dd"): String {
        if (Locale.getDefault().language == "en") {
            return dateString
        }

        val locale = Locale("ar")
        val sdf = SimpleDateFormat(inputFormat, Locale.ENGLISH)
        val date: Date = sdf.parse(dateString) ?: return dateString
        val arabicFormat = SimpleDateFormat("dd MMMM yyyy", locale)

        return convertNumbersInString(arabicFormat.format(date))
    }

    fun convertDayToArabic(day:String):String{
        if (Locale.getDefault().language == "en") {
            return day
        }
        return when(day) {
            "Sunday" -> "الأحد"
            "Monday" -> "الإثنين"
            "Tuesday" -> "الثلاثاء"
            "Wednesday" -> "الأربعاء"
            "Thursday" -> "الخميس"
            "Friday" -> "الجمعة"
            "Saturday" -> "السبت"
            else -> "غدا"
        }
    }

    fun convertWeatherToArabic(weather:String):String{
        if (Locale.getDefault().language == "en") {
            return weather
        }
        return when(weather){
            "clear sky" -> "سماء صافية"
            "few clouds" -> "غيوم قليلة"
            "scattered clouds" -> "غيوم متناثرة"
            "broken clouds" -> "غيوم متقطعة"
            "shower rain" -> "أمطار متفرقة"
            "rain" -> "مطر"
            "thunderstorm" -> "عاصفة رعدية"
            "snow" -> "ثلج"
            "mist" -> "ضباب"
            else -> "غيوم متفرقه"
        }
    }

    fun convertTemperature(kelvin: Double): String {
        val result =  when (_mutableTemp.value) {
            "Celsius","C","°م" -> kelvin - 273.15
            "Fahrenheit","F","ف" -> ((kelvin - 273.15) * 9 / 5) + 32
            else -> kelvin
        }
        if (Locale.getDefault().language == "en") {
            return result.toString()
        }
        return convertToArabicNumbers(result)!!
    }

    fun translateChar(): String {
        if(Locale.getDefault().language == "en"){
            return _mutableTemp.value
        }

        val result = when (_mutableTemp.value) {
            "Celsius" -> getString(R.string.celsius)
            "Fahrenheit" -> getString(R.string.fahrenheit)
            "K" -> "ك"
            "F" -> "ف"
            "C" -> "°م"
            else -> this.getString(R.string.kelvin)
        }
        return result
    }

    fun translateSpeedChar(): String {
        if(Locale.getDefault().language == "en"){
            return _mutableWind.value
        }

        val result = when (_mutableWind.value) {
            "mph","p",this.getString(R.string.mph) -> "ميل/س"
            else -> "كم/س"
        }
        return result
    }

    fun convertWindSpeed(meterPerSec: Double?): String? {
        if (meterPerSec == null){
            return null
        }
        val result =  if (_mutableWind.value == "mph") meterPerSec * 2.23694 else meterPerSec
        val formattedResult = String.format(Locale.US, "%.2f", result)
        return if (Locale.getDefault().language == "en") {
            formattedResult
        } else {
            convertToArabicNumbers(formattedResult.toDouble())!!
        }
    }

    private fun saveLanguagePreference(context: Context, langCode: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("Language", langCode)
        editor.apply()
    }

    fun loadLanguagePreference(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val savedLang = sharedPreferences.getString("Language", "en")

        val locale = Locale(savedLang!!)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }


    companion object {
        private lateinit var sharedPreferences: SharedPreferences
        private const val PREF_NAME = "WeatherPreferences"
        private const val WEATHER_KEY = "SavedWeather"
        private const val HOURLY_KEY = "SavedHourly"
        private const val DAILY_KEY = "SavedDaily"

        fun saveWeatherData(weather: WeatherDetails) {
            val editor = sharedPreferences.edit()
            val weatherJson = Gson().toJson(weather)
            editor.putString(WEATHER_KEY, weatherJson)
            editor.apply()
        }

        fun saveWeatherLists(hourlyList: List<HourlyDetails>, dailyList: List<HourlyDetails>) {
            val editor = sharedPreferences.edit()
            val hourlyJson = Gson().toJson(hourlyList)
            val dailyJson = Gson().toJson(dailyList)
            editor.putString(HOURLY_KEY, hourlyJson)
            editor.putString(DAILY_KEY, dailyJson)
            editor.apply()
        }

        fun getSavedWeatherData(): WeatherDetails? {
            val weatherJson = sharedPreferences.getString(WEATHER_KEY, null)
            return weatherJson?.let { Gson().fromJson(it, WeatherDetails::class.java) }
        }

        fun getSavedHourlyData(): List<HourlyDetails> {
            val hourlyJson = sharedPreferences.getString(HOURLY_KEY, null)
            return hourlyJson?.let {
                val type = object : TypeToken<List<HourlyDetails>>() {}.type
                Gson().fromJson(it, type)
            } ?: emptyList()
        }

        fun getSavedDailyData(): List<HourlyDetails> {
            val dailyJson = sharedPreferences.getString(DAILY_KEY, null)
            return dailyJson?.let {
                val type = object : TypeToken<List<HourlyDetails>>() {}.type
                Gson().fromJson(it, type)
            } ?: emptyList()
        }
    }

}
