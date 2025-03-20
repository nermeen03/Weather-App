package com.example.weatherforecast.view.favorite

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.data.pojo.DailyDetails
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.DailyDataRepository
import com.example.weatherforecast.data.repo.Response
import com.example.weatherforecast.view.DailyWeatherSection
import com.example.weatherforecast.view.GetWeatherDataByLoc
import com.example.weatherforecast.view.HourlyWeatherSection
import com.example.weatherforecast.view.NoInternetGif
import com.example.weatherforecast.view.TodayDetailsSection
import com.example.weatherforecast.view.TopWeatherSection
import com.example.weatherforecast.view.WaitingGif
import com.example.weatherforecast.view.WeatherLocationSection
import com.example.weatherforecast.view.utils.internet
import com.example.weatherforecast.view.utils.isInternetAvailable
import com.example.weatherforecast.viewModel.DailyDataViewModel
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailsScreen(lat:Double,lon:Double) {
    
    var temp by remember { mutableDoubleStateOf(0.0) }
    var feelLike by remember { mutableDoubleStateOf(0.0) }
    var weather by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var todayDetails by remember { mutableStateOf<DailyDetails?>(null) }
    var currentDetails by remember { mutableStateOf<WeatherDetails?>(null) }
    var hourlyList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }
    var daysList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }

    val context = LocalContext.current
    val viewModel: DailyDataViewModel =
        viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository(RetrofitHelper.retrofitInstance.create(ApiService::class.java))))

    isInternetAvailable(context)
    val internet = internet.observeAsState()

    val response by viewModel.response.collectAsState()

    if(internet.value == true) {
        GetWeatherDataByLoc(
            updateCurrent = { newDetails ->
                currentDetails = newDetails
            },
            updateList = { hourly, days ->
                hourlyList = hourly
                daysList = days
            },
            lat,lon,viewModel
        )
        when (response) {
            is Response.Loading -> {
                WaitingGif()
            }

            is Response.Success -> {
                temp = currentDetails!!.temp
                feelLike = currentDetails!!.feelLike
                weather = currentDetails!!.weather
                location = "${currentDetails!!.place.name}, ${currentDetails!!.place.code}"
                state = currentDetails!!.state
                todayDetails = DailyDetails(currentDetails!!.pressure,currentDetails!!.humidity,currentDetails!!.speed,currentDetails!!.cloud)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        TopWeatherSection(weather, feelLike, state)
                    }

                    item {
                        WeatherLocationSection(temp, location)
                    }

                    item {
                        HourlyWeatherSection(hourlyList)
                    }

                    item {
                        TodayDetailsSection(todayDetails)
                    }

                    item {
                        DailyWeatherSection(daysList)
                    }
                }
            }

            is Response.Failure -> {
                val error = (response as Response.Failure).error
                Log.e("TAG", "MainScreen: $error")
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }else{
        NoInternetGif()
    }


}