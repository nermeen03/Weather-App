package com.example.weatherforecast.view.favorite

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.weatherforecast.MyApplication
import com.example.weatherforecast.R
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.local.favorite.FavLocationsDataBase
import com.example.weatherforecast.data.local.favorite.FavLocationsLocalDataSource
import com.example.weatherforecast.data.pojo.DailyDetails
import com.example.weatherforecast.data.pojo.FavDetails
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.LocalNames
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.pojo.NameResponseItem
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.FavLocationsRemoteDataSource
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.DailyDataRepository
import com.example.weatherforecast.data.repo.FavLocationsRepository
import com.example.weatherforecast.view.GetWeatherDataByLoc
import com.example.weatherforecast.view.navigation.ScreenRoute
import com.example.weatherforecast.view.utils.internet
import com.example.weatherforecast.view.utils.isInternetAvailable
import com.example.weatherforecast.viewModel.DailyDataViewModel
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory
import com.example.weatherforecast.viewModel.FavLocationsViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapScreen(navController:NavHostController) {
    val previousEntry = navController.previousBackStackEntry
    val previousRoute = previousEntry?.destination?.route

    val context = LocalContext.current
    val apiKey = stringResource(R.string.map_api)

    val favViewModel: FavLocationsViewModel = viewModel(factory = FavLocationsViewModel.FavLocationsViewModelFactory(
        FavLocationsRepository.getRepository(
            FavLocationsLocalDataSource(FavLocationsDataBase.getInstance(context).getFavLocationsDao()),
            FavLocationsRemoteDataSource(RetrofitHelper.retrofitInstance.create(ApiService::class.java))
        )
    ))

    val detailViewModel: DailyDataViewModel =
        viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository(RetrofitHelper.retrofitInstance.create(ApiService::class.java))))


    LaunchedEffect(Unit) {
        favViewModel.loadCountries(context)
    }
    val selectedLocation by favViewModel.selectedLocation.collectAsState()
    val locationName by favViewModel.locationName.collectAsState()
    val responseState by favViewModel.response.collectAsState()
    val textFlow = MutableSharedFlow<String>(replay = 1)
    val scope = rememberCoroutineScope()
    val searchQuery = remember { mutableStateOf("") }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    var locationSelected by remember { mutableStateOf<NameResponseItem?>(null) }


    LaunchedEffect(textFlow) {
        textFlow.collect {
            searchQuery.value = it
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val mapView = MapView(ctx).apply { onCreate(null) }
                mapView.getMapAsync { map ->
                    mapLibreMap = map
                    favViewModel.onMapReady(map, apiKey)
                }
                mapView
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = {
                    scope.launch {
                        textFlow.emit(it)
                    }
                },
                placeholder = { Text(stringResource(R.string.search_location)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                trailingIcon = {
                    if (searchQuery.value.isNotEmpty()) {
                        IconButton(onClick = {
                            scope.launch { textFlow.emit("") }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.white))
            )

            favViewModel.filterCountries(searchQuery.value)
            val filtered = favViewModel.filteredCountries.collectAsStateWithLifecycle().value

            LazyColumn {
                items(filtered.size) {
                    Text(
                        text = "${filtered[it].name}, ${filtered[it].country}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(R.color.primaryLight))
                            .clickable {
                                scope.launch {
                                    val selected = filtered[it]
                                    textFlow.emit("${selected.name}, ${selected.country}")
                                    val latLng = LatLng(selected.coord.lat, selected.coord.lon)
                                    favViewModel.addMark(mapLibreMap, latLng)
                                    val name = NameResponseItem(
                                        filtered[it].country, filtered[it].coord.lat,
                                        LocalNames(
                                            be = "",
                                            cy = "",
                                            en = "",
                                            fr = "",
                                            he = "",
                                            ko = "",
                                            mk = "",
                                            ru = ""
                                        ), filtered[it].coord.lon, filtered[it].name, ""
                                    )
                                    locationSelected = name
                                }
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
        locationSelected?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
                ,
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SelectedLocationByName(locationSelected!!,favViewModel,detailViewModel,previousRoute,navController)
                }
            }
        }
        selectedLocation?.let { location ->

            LaunchedEffect(location) {
                favViewModel.getLocationName(location.latitude, location.longitude)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
                ,
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (responseState) {
                        is Response.Success -> {
                            if (locationName != null) {
                                SelectedLocationByName(locationName!!, favViewModel,detailViewModel,previousRoute,navController)
                            } else {
                                SelectedLocation(location.latitude, location.longitude)
                            }
                        }
                        is Response.Failure -> SelectedLocation(location.latitude, location.longitude)
                        is Response.Loading -> if(locationSelected == null) CircularProgressIndicator()
                    }
                }
            }
        }
    }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(onClick = { mapLibreMap?.animateCamera(CameraUpdateFactory.zoomIn()) }) {
                Text(stringResource(R.string.plus))
            }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingActionButton(onClick = { mapLibreMap?.animateCamera(CameraUpdateFactory.zoomOut()) }) {
                Text(stringResource(R.string.minus))
            }
        }
    }





@Composable
fun SelectedLocation(lat: Double, long: Double) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .background(colorResource(R.color.blue)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(color = colorResource(R.color.white),
                text = stringResource(R.string.location_format, lat.toString(), long.toString()))
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectedLocationByName(location: NameResponseItem, viewModel: FavLocationsViewModel,detailViewModel:DailyDataViewModel,route:String?,navController: NavHostController) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    var showDetails by remember { mutableStateOf(false) }
    val response by viewModel.locResponse.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(colorResource(R.color.purple_200)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.location_format)+
                        location.name+ stringResource(R.string.comma) +location.country+ stringResource(R.string.end_par),
                color = colorResource(R.color.dark),
                modifier = Modifier.padding(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if(route == "favorite") {
                        showDetails = true
                    }else{
                        application.setCurrentLocation(Pair(location.lat,location.lon))
                        Toast.makeText(
                            context,
                            "the location is ${location.name}, ${location.country}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.add_to_favorites)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.save_location))
            }
            if (showDetails) {
                val locationDetails = Location(
                    name = location.name,
                    country = location.country,
                    lon = location.lon,
                    lat = location.lat,
                    arabicName = location.name
                )
                viewModel.insertLocation(locationDetails)
                when (response) {
                    is Response.Success -> {
                        showDetails = false
                        Toast.makeText(
                            context,
                            context.getString(R.string.inserted_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                        /*SaveLocation(location,locationDetails,detailViewModel,viewModel)*/
                        coroutineScope.launch {
                            navController.navigate(
                                ScreenRoute.DetailsOfflineRoute.withArgs(
                                    location.lat,
                                    location.lon
                                )
                            )                        }
                    }
                    is Response.Failure -> {
                        Log.e("TAG", "FavRow: Failed to fetch details, inserting without weather data")
                        Toast.makeText(
                            context,
                            context.getString(R.string.an_error_occurred),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Response.Loading -> Log.d("TAG", "FavRow: Waiting for response...")
                }
            }
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun SaveLocation(
    loc: NameResponseItem,
    locationDetails:Location,
    viewModel: DailyDataViewModel,
    favViewModel: FavLocationsViewModel
) {

    Log.i("TAG", "SaveLocation: searched loc is ${loc.lon}, ${loc.lat}")
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
    isInternetAvailable(context)
    val internet = internet.collectAsState()

    val response by viewModel.response.collectAsState()
    val detailResponse by favViewModel.detailsResponse.collectAsStateWithLifecycle()

    if (internet.value) {
        Log.i("TAG", "SaveLocation: Calling GetWeatherDataByLoc()")
        GetWeatherDataByLoc(
            updateCurrent = { newDetails ->
                Log.i("TAG", "SaveLocation: Got new weather data")
                currentDetails = newDetails
            },
            updateList = { hourly, days ->
                hourlyList = hourly
                daysList = days
            },
            long = 10.0, lat = 10.0,
            arabicData = { dataArray ->
                arabicData = dataArray
                Log.i("TAG", "SaveLocation: Arabic data received: $dataArray")
                MyApplication.saveArabicData(dataArray)
            },
            viewModel = viewModel
        )
    }



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
        }else if (response is Response.Failure) {
            Log.e("TAG", "SaveLocation: API Failed - ${(response as Response.Failure).error}")
        }
    }

    if (internet.value) {
        Log.i("TAG", "MainScreen: Response status - $response")

        if (response is Response.Success && currentDetails != null) {
            Log.i("TAG", "SaveLocation: Ready to save data")

            favViewModel.insertLocation(
                FavDetails(
                    currentWeather = currentDetails!!,
                    hourlyWeather = hourlyList,
                    dailyWeather = daysList,
                    lat = loc.lat,
                    lon = loc.lon,
                    location = "${loc.name}, ${loc.country}",
                    arabicData = arabicData?.ifEmpty { listOf("No Data") } ?: listOf(loc.name, currentDetails!!.weather)
                )
            )
            Log.i("TAG", "SaveLocation: response is $detailResponse")
        } else {
            Log.i("TAG", "SaveLocation: Data is still loading")
        }
    }
}

