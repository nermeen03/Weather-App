package com.example.weatherforecast.viewModel


import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.pojo.Country
import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.data.repo.IDailyDataRepository
import com.example.weatherforecast.data.repo.Response
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import com.example.weatherforecast.view.utils.internet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.retry
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class DailyDataViewModel(private val dataRepository: IDailyDataRepository):ViewModel(){
    private val mutableDailyData = MutableStateFlow<ForecastDataResponse?>(null)

    private val mutableCurrentWeather  = MutableStateFlow<CurrentWeatherResponse?>(null)

    private val mutableResponse = MutableStateFlow(Response.Loading as Response<*>)
    val response = mutableResponse.asStateFlow()

    private val _filteredWeatherData = MutableStateFlow<Pair<List<HourlyDetails>, List<HourlyDetails>>?>(null)
    val filteredWeatherData: StateFlow<Pair<List<HourlyDetails>, List<HourlyDetails>>?> = _filteredWeatherData.asStateFlow()

    private val _currentWeatherDetails = MutableStateFlow<WeatherDetails?>(null)
    val currentWeatherDetails = _currentWeatherDetails.asStateFlow()


    private val handle = CoroutineExceptionHandler { _, exception ->
        mutableResponse.value = Response.Failure(exception)
    }

    private fun getDailyData(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO + handle) {
            while (true) {
                launch {
                    dataRepository.getDailyData(lat, lon)
                        .distinctUntilChanged().retry(3)
                        .catch { e -> mutableResponse.value = Response.Failure(e) }
                        .collect {
                            mutableDailyData.value = it
                            mutableResponse.value = Response.Success
                            Log.i("TAG", "getDaily: data ${mutableDailyData.value}")

                        }
                }
                delay(3600000)
            }
        }
    }

    private fun getCurrentWeather(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO + handle) {
            while (true) {
                launch {
                    dataRepository.getCurrentWeather(lat, lon)
                        .distinctUntilChanged().retry(3)
                        .catch { e -> mutableResponse.value = Response.Failure(e) }
                        .collect {
                            mutableCurrentWeather.value = it
                            mutableResponse.value = Response.Success
                            Log.i("TAG", "fetchWeatherData: data ${mutableCurrentWeather.value}")

                        }
                }
                delay(600000)
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
     fun fetchWeatherData(lat: Double, lon: Double) {
            if (internet.value == true) {
                if (lat != -1.0 && lon != -1.0) {
                    Log.i("TAG", "fetchWeatherData: start")
                    val today = LocalDate.now().toString()
                    val currentHour = LocalTime.now().hour

                    getDailyData(lat, lon)
                    getCurrentWeather(lat, lon)
                    Log.i("TAG", "fetchWeatherData: end")

                    viewModelScope.launch(Dispatchers.IO + handle) {
                        mutableDailyData.collect { dailyDetails ->
                            dailyDetails?.let {
                                val dailyDataList = dailyDetails.list
                                val todayWeather = dailyDataList.filter {
                                    it.dt_txt.startsWith(today)
                                }
                                val hourlyDetails = mutableListOf<HourlyDetails>()
                                val closestTime = if (currentHour % 3 == 0) currentHour else (currentHour / 3) * 3 + 3

                                val timeString = String.format(Locale.getDefault(), "%02d:00:00", closestTime)

                                val current = todayWeather.find {
                                    it.dt_txt.startsWith(today) && it.dt_txt.contains(timeString)
                                }


                                todayWeather.forEach {
                                    val time = it.dt_txt.split(" ")[1].substring(0, 5)
                                    val temp = it.main.temp
                                    val feel = it.main.feels_like
                                    val state = it.weather[0].icon

                                    val hourlyDetail = HourlyDetails(time, temp, feel, state)
                                    hourlyDetails.add(hourlyDetail)
                                }

                                val otherWeather = dailyDataList.filterNot {
                                    it.dt_txt.startsWith(today)
                                }
                                val daysDetails = mutableListOf<HourlyDetails>()
                                otherWeather.forEach { it ->
                                    val time = it.dt_txt
                                    val temp = it.main.temp
                                    val feel = it.main.feels_like
                                    val state = it.weather[0].icon

                                    if (time.split(" ")[1].substring(0, 5) == "00:00") {
                                        val apiDate = LocalDate.parse(time.substring(0, 10))
                                        val todayDate = LocalDate.now()

                                        val dayText = when {
                                            apiDate.isEqual(todayDate) -> "Today"
                                            apiDate.isEqual(todayDate.plusDays(1)) -> "Tomorrow"
                                            else -> apiDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                                        }

                                        val days = HourlyDetails(dayText, temp, feel, state)
                                        daysDetails.add(days)
                                    }
                                }
                                _filteredWeatherData.value = Pair(hourlyDetails, daysDetails)
                            }
                        }
                    }
                    viewModelScope.launch {
                        mutableCurrentWeather.collect { todayDetails ->
                            todayDetails?.let {
                                val temp = todayDetails.main.temp
                                val feelLike = todayDetails.main.feels_like
                                val weather = todayDetails.weather.firstOrNull()?.main ?: ""
                                val city = todayDetails.name
                                val country = todayDetails.sys.country ?: ""
                                val location = "$city, $country"
                                val pressure = todayDetails.main.pressure
                                val humidity = todayDetails.main.humidity
                                val speed = todayDetails.wind.speed
                                val cloud = todayDetails.clouds.all
                                val state = todayDetails.weather[0].icon

                                val dailyData = WeatherDetails(temp,feelLike,weather, Country(city,country),pressure,humidity,speed,cloud,state)

                                _currentWeatherDetails.value = dailyData
                                Log.i("TAG", "fetchWeatherData: result ${currentWeatherDetails.value}")
                            }
                        }
                    }
                }
            }
        }

    fun stopFetchingWeather() {
        viewModelScope.coroutineContext.cancelChildren()
    }


}
class DailyDataViewModelFactory(private val dataRepository: IDailyDataRepository):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DailyDataViewModel(dataRepository) as T
    }
}
