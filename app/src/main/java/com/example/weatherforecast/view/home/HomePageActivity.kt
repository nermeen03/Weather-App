package com.example.weatherforecast.view.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import com.example.weatherforecast.view.GetWeatherData
import com.example.weatherforecast.view.NoInternetGif
import com.example.weatherforecast.view.WaitingGif
import com.example.weatherforecast.view.WeatherSections
import com.example.weatherforecast.view.utils.internet
import com.example.weatherforecast.view.utils.isInternetAvailable
import com.example.weatherforecast.viewModel.DailyDataViewModel
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    var temp by rememberSaveable { mutableDoubleStateOf(0.0) }
    var feelLike by rememberSaveable { mutableDoubleStateOf(0.0) }
    var weather by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }
    var todayDetails by remember { mutableStateOf<DailyDetails?>(null) }
    var currentDetails by remember { mutableStateOf<WeatherDetails?>(null) }
    var hourlyList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }
    var daysList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }
    var arabicData by remember { mutableStateOf<List<String>?>(null) }

    val context = LocalContext.current
    val viewModel: DailyDataViewModel =
        viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository(RetrofitHelper.retrofitInstance.create(ApiService::class.java))))

    isInternetAvailable(context)
    val internet = internet.collectAsStateWithLifecycle()

    val response by viewModel.response.collectAsStateWithLifecycle()
    val application = context.applicationContext as MyApplication

    if(!application.reStarted && internet.value) {
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
            arabicData = {data ->
                arabicData = data
                MyApplication.saveArabicData(arabicData!!)
            },
            viewModel
        )
    }


    val savedWeather = MyApplication.getSavedWeatherData()
    val savedHourlyList = MyApplication.getSavedHourlyData()
    val savedDailyList = MyApplication.getSavedDailyData()
    val savedArabicData = MyApplication.getSavedArabicData()

    LaunchedEffect(response) {
        if (response is Response.Success && currentDetails != null && !application.reStarted) {
            currentDetails?.let { details ->
                temp = details.temp
                feelLike = details.feelLike
                weather = details.weather
                location = "${details.place.name}, ${details.place.code}"
                application.updateLocationName(location)
                state = details.state
                todayDetails = DailyDetails(details.pressure, details.humidity, details.speed, details.cloud)
            }
        } else if ((!internet.value && savedWeather != null)||(internet.value && application.reStarted && savedWeather != null)) {
            savedWeather.let { details ->
                temp = details.temp
                feelLike = details.feelLike
                weather = details.weather
                location = "${details.place.name}, ${details.place.code}"
                application.updateLocationName(location)
                state = details.state
                todayDetails = DailyDetails(details.pressure, details.humidity, details.speed, details.cloud)
                hourlyList = savedHourlyList
                daysList = savedDailyList
                arabicData = savedArabicData
            }
        }
    }

    if (internet.value && !application.reStarted) {
        when {
            response is Response.Loading -> {
                WaitingGif()
            }
            response is Response.Success && currentDetails != null -> {
                WeatherSections(viewModel,context,weather,feelLike,state, temp, location, hourlyList, todayDetails, daysList,arabicData)
            }
            response is Response.Failure && savedWeather!=null&& savedHourlyList.isNotEmpty()&&savedDailyList.isNotEmpty() -> {
                WeatherSections(viewModel,context,weather,feelLike,state, temp, location, hourlyList, todayDetails, daysList,arabicData)
            }
            else -> {
                WaitingGif() // errorGif
            }
        }
    }else if(internet.value && application.reStarted){

        WeatherSections(viewModel,context,weather,feelLike,state, temp, location, hourlyList, todayDetails, daysList,arabicData)
    }
    else if(!internet.value && savedWeather!=null && savedDailyList.isNotEmpty()){
            var isDataLoaded by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(500)
                isDataLoaded = true
            }

            if (!isDataLoaded) {
                WaitingGif()
            } else {
                WeatherSections(
                    viewModel,
                    context,
                    weather,
                    feelLike,
                    state,
                    temp,
                    location,
                    hourlyList,
                    todayDetails,
                    daysList,
                    arabicData
                )
            }
        }else{
        NoInternetGif()
    }
}

