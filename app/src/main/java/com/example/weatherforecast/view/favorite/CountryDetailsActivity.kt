package com.example.weatherforecast.view.favorite

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.weatherforecast.MyApplication
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.local.favorite.FavLocationsDataBase
import com.example.weatherforecast.data.local.favorite.FavLocationsLocalDataSource
import com.example.weatherforecast.data.pojo.DailyDetails
import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.FavLocationsRemoteDataSource
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.DailyDataRepository
import com.example.weatherforecast.data.repo.FavLocationsRepository
import com.example.weatherforecast.view.GetWeatherDataByLoc
import com.example.weatherforecast.view.NoInternetGif
import com.example.weatherforecast.view.RetryImage
import com.example.weatherforecast.view.WaitingGif
import com.example.weatherforecast.view.WeatherSections
import com.example.weatherforecast.view.utils.internet
import com.example.weatherforecast.viewModel.DailyDataViewModel
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory
import com.example.weatherforecast.viewModel.FavLocationsViewModel


@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailsScreen(lat:Double,lon:Double,navController: NavHostController) {

    val previousEntry = navController.previousBackStackEntry
    val previousRoute = previousEntry?.destination?.route

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
        viewModel(
            factory = DailyDataViewModelFactory(
                DailyDataRepository.getRepository(
                    RetrofitHelper.retrofitInstance.create(ApiService::class.java)
                )
            )
        )

    val response by viewModel.response.collectAsState()
    val application = context.applicationContext as MyApplication

    if (internet.value && !application.reStarted) {
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
            },
            viewModel
        )
    }else{
        DetailsScreenOffline(lat, lon,viewModel)
    }


    if (internet.value && !application.reStarted) {
        when {
            response is Response.Loading -> {
                WaitingGif()
            }

            response is Response.Success && currentDetails != null -> {
                currentDetails?.let { details ->
                    temp = details.temp
                    feelLike = details.feelLike
                    weather = details.weather
                    location = "${details.place.name}, ${details.place.code}"
                    state = details.state
                    todayDetails =
                        DailyDetails(details.pressure, details.humidity, details.speed, details.cloud)
                }
                WeatherSections(
                    viewModel,
                    context,
                    lat, lon,
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
                if (previousRoute == "map") {
                    val favLocationsViewModel: FavLocationsViewModel = viewModel(
                        factory = FavLocationsViewModel.FavLocationsViewModelFactory(
                            FavLocationsRepository.getRepository(
                                FavLocationsLocalDataSource(
                                    FavLocationsDataBase.getInstance(context).getFavLocationsDao()
                                ),
                                FavLocationsRemoteDataSource(
                                    RetrofitHelper.retrofitInstance.create(
                                        ApiService::class.java
                                    )
                                )
                            )
                        )
                    )
                    favLocationsViewModel.insertLocation(FavDetails(
                        currentWeather = currentDetails!!,
                        hourlyWeather = hourlyList,
                        dailyWeather = daysList,
                        lat = lat,
                        lon = lon,
                        location = location,
                        arabicData = arabicData?.ifEmpty { listOf("No Data") } ?: listOf(
                            arabicData!![0],arabicData?.get(1)?:currentDetails!!.weather
                        )
                    ))
                    favLocationsViewModel.insertLocation(Location(location,"",lon,lat,arabicData?.get(0)?:location))
                }
            }

            response is Response.Failure -> {
                DetailsScreenOffline(lat,lon,viewModel)
            }
        }
    } else if (internet.value && application.reStarted) {
        WeatherSections(
            viewModel,
            context,
            lat, lon,
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

    else {
        NoInternetGif()
    }


}

@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailsScreenOffline(
    lat: Double,
    lon: Double,
    dailyViewModel: DailyDataViewModel? = null
) {
    val context = LocalContext.current
    val viewModel: FavLocationsViewModel = viewModel(
        factory = FavLocationsViewModel.FavLocationsViewModelFactory(
            FavLocationsRepository.getRepository(
                FavLocationsLocalDataSource(FavLocationsDataBase.getInstance(context).getFavLocationsDao()),
                FavLocationsRemoteDataSource(RetrofitHelper.retrofitInstance.create(ApiService::class.java))
            )))
    val dataViewModel: DailyDataViewModel =
        viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository(RetrofitHelper.retrofitInstance.create(ApiService::class.java))))

    val favDetails = viewModel.favDetail.collectAsStateWithLifecycle()
    val response = viewModel.detailsResponse.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getFavDetails(lon, lat)
    }

    when(response.value) {
        is Response.Success -> {
            if (favDetails.value != null) {

                val details = favDetails.value!!
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
                WeatherSections(
                    dataViewModel,
                    context,
                    lat,
                    lon,
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
        }
        is Response.Loading -> Log.d("TAG", "DetailsScreenOffline: Waiting ...")
        is Response.Failure -> {
            Log.d("TAG", "DetailsScreen: error")
            RetryImage(dailyViewModel!!, context, lat, lon)
        }
    }



}
