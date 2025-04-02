package com.example.weatherforecast

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.view.utils.isInternetAvailable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyApplication : Application() {



    private val _mutableLocation = MutableStateFlow("GPS")
    val location: StateFlow<String> = _mutableLocation.asStateFlow()
    private val _mutableCurrentLocation = MutableStateFlow(Pair(-1.0,-1.0))
    val currentLocation: StateFlow<Pair<Double, Double>> = _mutableCurrentLocation.asStateFlow()
    private val _mutableCurrentLocationName = MutableStateFlow("")
    val currentLocationName: StateFlow<String> = _mutableCurrentLocationName.asStateFlow()
    private val _mutableCurrentLocationArabicName = MutableStateFlow("")
    val currentLocationArabicName: StateFlow<String> = _mutableCurrentLocationArabicName.asStateFlow()
    private val _mutableTemp = MutableStateFlow("°K")
    val temp: StateFlow<String> = _mutableTemp.asStateFlow()
    private val _mutableWind = MutableStateFlow("mps")
    val wind: StateFlow<String> = _mutableWind.asStateFlow()

    private lateinit var systemLang:String

    var reStarted = false
    val home = MutableStateFlow(false)

    override fun onCreate() {
        super.onCreate()
        systemLang = this.resources.configuration.locales.get(0).language
        sharedPreferences = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        isInternetAvailable(this)
    }

    fun getCurrentLanguage(context: Context): String {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val savedLang = sharedPreferences.getString("Language", "en")
        return savedLang?:"en"
    }
    fun setLanguage(context: Context, langCode: String) {
        val locale = when (langCode) {
            "def" -> Locale(systemLang)
            else -> Locale(langCode)
        }
        Log.i("TAG", "setLanguage: ${locale.language}")

        Locale.setDefault(locale)


        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        saveLanguagePreference(context, langCode)

    }
    fun updateLocationName(newName: String,arabicName:String) {
        _mutableCurrentLocationName.value = newName
        _mutableCurrentLocationArabicName.value = arabicName
    }


    fun setLocation(lang: String) {
        _mutableLocation.value = lang
    }
    fun setCurrentLocation(loc: Pair<Double, Double>) {
        if (_mutableCurrentLocation.value != loc) {
            _mutableCurrentLocation.value = loc
            home.value = false
        } else {
            Log.d("TAG", "setCurrentLocation: No change in location")
        }
    }

    fun setTemp(str: String) {
        _mutableTemp.value = str
    }
    fun setWind(str: String) {
        _mutableWind.value = str
    }

    fun convertToArabicNumbers(number: Double?): String? {
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

    fun convertDateToArabic(dateString: String): String {
        if (Locale.getDefault().language == "en") {
            return dateString
        }

        val locale = Locale("ar")

        val possibleFormats = listOf("yyyy-MM-dd", "dd/M/yyyy", "dd/MM/yyyy", "M/d/yyyy", "MM/dd/yyyy")

        var parsedDate: Date? = null

        for (format in possibleFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                parsedDate = sdf.parse(dateString)
                if (parsedDate != null) break
            } catch (e: ParseException) {
                // Ignore and try the next format
            }
        }

        if (parsedDate == null) return dateString

        val arabicFormat = SimpleDateFormat("dd MMMM yyyy", locale)
        return convertNumbersInString(arabicFormat.format(parsedDate))
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
    fun convertTemperature(kelvin: Double): String? {
        val result = when (_mutableTemp.value) {
            "Celsius", "°C", "°م" -> kelvin - 273.15
            "Fahrenheit", "°F", "°ف" -> ((kelvin - 273.15) * 9 / 5) + 32
            else -> kelvin
        }
        val formattedResult = String.format(Locale.ENGLISH, "%.2f", result).toDouble()

        return if (Locale.getDefault().language == "en") {
            formattedResult.toString()
        } else {
            convertToArabicNumbers(formattedResult)
        }
    }

    fun translateChar(): String {
        if(Locale.getDefault().language == "en"){
            return _mutableTemp.value
        }

        val result = when (_mutableTemp.value) {
            "Celsius" -> getString(R.string.celsius)
            "Fahrenheit" -> getString(R.string.fahrenheit)
            "°K" -> "°ك"
            "°F" -> "°ف"
            "°C" -> "°م"
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
        editor.clear().apply()
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
        private const val ARABIC_KEY = "arabicData"

        fun saveWeatherData(weather: WeatherDetails) {
            val editor = sharedPreferences.edit()
            val weatherJson = Gson().toJson(weather)
            editor.putString(WEATHER_KEY, weatherJson)
            editor.apply()
        }

        fun saveArabicData(data:List<String>) {
            val editor = sharedPreferences.edit()
            val arabicJson = Gson().toJson(data)
            editor.putString(ARABIC_KEY, arabicJson)
            editor.apply()
        }

        fun getSavedArabicData(): List<String>? {
            val arabicJson = sharedPreferences.getString(ARABIC_KEY, null)
            return arabicJson?.let { Gson().fromJson(it, Array<String>::class.java).toList() }
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
