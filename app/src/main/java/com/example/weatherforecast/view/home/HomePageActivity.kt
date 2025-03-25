package com.example.weatherforecast.view.home

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.MyApplication
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.pojo.DailyDetails
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.DailyDataRepository
import com.example.weatherforecast.view.DailyWeatherSection
import com.example.weatherforecast.view.GetWeatherData
import com.example.weatherforecast.view.HourlyWeatherSection
import com.example.weatherforecast.view.NoInternetGif
import com.example.weatherforecast.view.TodayDetailsSection
import com.example.weatherforecast.view.TopBar
import com.example.weatherforecast.view.TopWeatherSection
import com.example.weatherforecast.view.WaitingGif
import com.example.weatherforecast.view.WeatherLocationSection
import com.example.weatherforecast.view.utils.internet
import com.example.weatherforecast.view.utils.isInternetAvailable
import com.example.weatherforecast.viewModel.DailyDataViewModel
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    Log.i("TAG", "MainScreen: home page")

    var temp by rememberSaveable { mutableDoubleStateOf(0.0) }
    var feelLike by rememberSaveable { mutableDoubleStateOf(0.0) }
    var weather by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }
    var todayDetails by remember { mutableStateOf<DailyDetails?>(null) }
    var currentDetails by remember { mutableStateOf<WeatherDetails?>(null) }
    var hourlyList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }
    var daysList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }

    val context = LocalContext.current
    val viewModel: DailyDataViewModel =
        viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository(RetrofitHelper.retrofitInstance.create(ApiService::class.java))))

    isInternetAvailable(context)
    val internet = internet.collectAsStateWithLifecycle()

    val response by viewModel.response.collectAsStateWithLifecycle()

    GetWeatherData(
        updateCurrent = { newDetails ->
            currentDetails = newDetails
            MyApplication.saveWeatherData(newDetails)
        },
        updateList = { hourly, days ->
            hourlyList = hourly
            daysList = days
            MyApplication.saveWeatherLists(hourly, days)
        },
        viewModel
    )

    val savedWeather = MyApplication.getSavedWeatherData()
    val savedHourlyList = MyApplication.getSavedHourlyData()
    val savedDailyList = MyApplication.getSavedDailyData()

    LaunchedEffect(response) {
        if (response is Response.Success && currentDetails != null) {
            Log.i("TAG", "MainScreen: Updating UI after success")
            currentDetails?.let { details ->
                temp = details.temp
                feelLike = details.feelLike
                weather = details.weather
                location = "${details.place.name}, ${details.place.code}"
                state = details.state
                todayDetails = DailyDetails(details.pressure, details.humidity, details.speed, details.cloud)
            }
        } else if (!internet.value && savedWeather != null) {
            Log.i("TAG", "MainScreen: Loading saved weather data")
            savedWeather.let { details ->
                temp = details.temp
                feelLike = details.feelLike
                weather = details.weather
                location = "${details.place.name}, ${details.place.code}"
                state = details.state
                todayDetails = DailyDetails(details.pressure, details.humidity, details.speed, details.cloud)
                hourlyList = savedHourlyList
                daysList = savedDailyList
            }
        }
    }


    if (internet.value) {
        Log.i("TAG", "MainScreen: Response status - $response")

        when {
            response is Response.Loading -> {
                Log.i("TAG", "MainScreen: Loading screen")
                WaitingGif()
            }

            response is Response.Success && currentDetails != null -> {
                Log.i("TAG", "MainScreen: Displaying weather data")
                WeatherSections(viewModel,context,weather,feelLike,state, temp, location, hourlyList, todayDetails, daysList)
            }

            response is Response.Failure && savedWeather!=null&& savedHourlyList.isNotEmpty()&&savedDailyList.isNotEmpty() -> {
                WeatherSections(viewModel,context,weather,feelLike,state, temp, location, hourlyList, todayDetails, daysList)
            }

            else -> {
                Log.i("TAG", "MainScreen: Waiting for data to update")
                WaitingGif() // errorGif
            }
        }
    }else if(!internet.value&&savedWeather!=null&& savedHourlyList.isNotEmpty()&&savedDailyList.isNotEmpty()){
        WeatherSections(viewModel,context,weather,feelLike,state, temp, location, hourlyList, todayDetails, daysList)
    }else{
        NoInternetGif()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherSections(
    viewModel: DailyDataViewModel,
    context: Context,
    weather: String,
    feelLike: Double,
    state: String,
    temp: Double,
    location: String,
    hourlyList: List<HourlyDetails>,
    todayDetails: DailyDetails?,
    daysList: List<HourlyDetails>){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { TopBar(viewModel, context) }
        item { TopWeatherSection(weather, feelLike, state) }
        item { WeatherLocationSection(temp, location) }
        item { HourlyWeatherSection(hourlyList) }
        item { TodayDetailsSection(todayDetails) }
        item { DailyWeatherSection(daysList) }
    }
}
