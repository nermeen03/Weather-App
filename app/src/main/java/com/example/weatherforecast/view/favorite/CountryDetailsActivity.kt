package com.example.weatherforecast.view.favorite

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.MyApplication
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.local.favorite.FavLocationsDataBase
import com.example.weatherforecast.data.local.favorite.FavLocationsLocalDataSource
import com.example.weatherforecast.data.pojo.DailyDetails
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.FavLocationsRemoteDataSource
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.DailyDataRepository
import com.example.weatherforecast.data.repo.FavLocationsRepository
import com.example.weatherforecast.view.GetWeatherDataByLoc
import com.example.weatherforecast.view.NoInternetGif
import com.example.weatherforecast.view.WaitingGif
import com.example.weatherforecast.view.WeatherSections
import com.example.weatherforecast.view.utils.internet
import com.example.weatherforecast.view.utils.isInternetAvailable
import com.example.weatherforecast.viewModel.DailyDataViewModel
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory
import com.example.weatherforecast.viewModel.FavLocationsViewModel


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
    var arabicData by remember { mutableStateOf<List<String>?>(null) }

    val context = LocalContext.current
    val viewModel: DailyDataViewModel =
        viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository(RetrofitHelper.retrofitInstance.create(ApiService::class.java))))

    isInternetAvailable(context)
    val internet = internet.collectAsState()

    val response by viewModel.response.collectAsState()
    val application = context.applicationContext as MyApplication

    if(internet.value && !application.reStarted) {
        GetWeatherDataByLoc(
            updateCurrent = { newDetails ->
                currentDetails = newDetails
            },
            updateList = { hourly, days ->
                hourlyList = hourly
                daysList = days
            },
            lat, lon,
            arabicData = { dataArray ->
                arabicData = dataArray
                MyApplication.saveArabicData(dataArray)
            },
            viewModel
        )
    }

    LaunchedEffect(response) {
        if (response is Response.Success && currentDetails != null && !application.reStarted) {
            Log.i("TAG", "MainScreen: Updating UI after success")
            currentDetails?.let { details ->
                temp = details.temp
                feelLike = details.feelLike
                weather = details.weather
                location = "${details.place.name}, ${details.place.code}"
                state = details.state
                todayDetails = DailyDetails(details.pressure, details.humidity, details.speed, details.cloud)
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
                WeatherSections(viewModel,context,weather,feelLike,state, temp, location, hourlyList, todayDetails, daysList,arabicData)
            }

            response is Response.Failure  -> {
                Log.d("TAG", "DetailsScreen: error")
            }

            else -> {
                Log.i("TAG", "MainScreen: Waiting for data to update")
                WaitingGif() // errorGif
            }
        }
    }

    else if(internet.value && application.reStarted){
        WeatherSections(viewModel,context,weather,feelLike,state, temp, location, hourlyList, todayDetails, daysList,arabicData)
    }
    else{
        NoInternetGif()
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailsScreenOffline(
    lat: Double,
    lon: Double
) {
    val context = LocalContext.current
    val viewModel: FavLocationsViewModel = viewModel(
        factory = FavLocationsViewModel.FavLocationsViewModelFactory(
            FavLocationsRepository.getRepository(
                FavLocationsLocalDataSource(FavLocationsDataBase.getInstance(context).getFavLocationsDao()),
                FavLocationsRemoteDataSource(RetrofitHelper.retrofitInstance.create(ApiService::class.java))
            )
        )
    )

    // Fetch data when screen is first created
    LaunchedEffect(Unit) {
        viewModel.getFavDetails(lon, lat)
    }

    // Observe state from ViewModel
    val favDetails by viewModel.favDetail.collectAsStateWithLifecycle()

    Log.i("TAG", "DetailsScreenOffline: $favDetails")

    favDetails?.let { details ->
        val temp = details.currentWeather.temp
        val feelLike = details.currentWeather.feelLike
        val weather = details.currentWeather.weather
        val location = details.location
        val state = details.currentWeather.state
        val todayDetails = DailyDetails(
            details.currentWeather.pressure, details.currentWeather.humidity,
            details.currentWeather.speed, details.currentWeather.cloud
        )
        val hourlyList = details.hourlyWeather
        val daysList = details.dailyWeather
        val arabicData = details.arabicData

        val dataViewModel: DailyDataViewModel =
            viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository(RetrofitHelper.retrofitInstance.create(ApiService::class.java))))

        Log.d("TAG", "Hourly List Size: ${arabicData.size}")
        Log.d("TAG", "Daily List Size: ${daysList.size}")

        WeatherSections(dataViewModel, context, weather, feelLike, state, temp, location, hourlyList, todayDetails, daysList, arabicData)
    } ?: run {
        // Show loading or error UI
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Loading...", fontSize = 20.sp, color = Color.White)
        }
    }
}
