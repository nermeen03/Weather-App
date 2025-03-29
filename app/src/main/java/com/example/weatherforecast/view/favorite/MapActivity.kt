package com.example.weatherforecast.view.favorite

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

    Log.i("TAG", "$previousRoute == favorite")

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
                    SelectedLocationByName(locationSelected!!,favViewModel,detailViewModel,previousRoute)
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
                                SelectedLocationByName(locationName!!, favViewModel,detailViewModel,previousRoute)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectedLocationByName(location: NameResponseItem, viewModel: FavLocationsViewModel,detailViewModel:DailyDataViewModel,route:String?) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    var showDetails by remember { mutableStateOf(false) }

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
                Log.i("TAG", "SelectedLocationByName: inside")
                SaveLocation(location,detailViewModel,viewModel)
            }
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun SaveLocation(
    location: NameResponseItem,
    detailViewModel: DailyDataViewModel,
    viewModel: FavLocationsViewModel
) {
    val context = LocalContext.current

    var currentDetails by remember { mutableStateOf<WeatherDetails?>(null) }
    var hourlyList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }
    var daysList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }
    var arabicData by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSaved by remember { mutableStateOf(false) }

    val response = detailViewModel.response.collectAsStateWithLifecycle()
    GetWeatherDataByLoc(
        updateCurrent = { newDetails -> currentDetails = newDetails },
        updateList = { hourly, days ->
            hourlyList = hourly
            daysList = days
        },
        arabicData = { data -> arabicData = data },
        long = location.lon,
        lat = location.lat,
        viewModel = detailViewModel
    )

    LaunchedEffect(response.value) {
        if (response.value is Response.Success && currentDetails != null) {
            Log.i("TAG", "SaveLocation: Trying to save location: ${location.name}, ${location.country}")
            if (!isSaved && currentDetails != null && hourlyList.isNotEmpty() && daysList.isNotEmpty()) {
                Log.i("TAG", "SaveLocation: inside")
                val favDetails = FavDetails(
                    currentWeather = currentDetails!!,
                    hourlyWeather = hourlyList,
                    dailyWeather = daysList,
                    lat = location.lat,
                    lon = location.lon,
                    location = "${location.name}, ${location.country}",
                    arabicData = arabicData.ifEmpty { listOf("No Data") }
                )

                val locationDetails = Location(
                    name = location.name,
                    country = location.country,
                    lon = location.lon,
                    lat = location.lat,
                    arabicName = arabicData[1]
                )

                viewModel.insertLocation(locationDetails, favDetails)
                if (response.value is Response.Success) {
                    Log.i("TAG", "SaveLocation: done")
                    Toast.makeText(context, context.getString(R.string.added_successfully)
                        , Toast.LENGTH_SHORT).show()
                    isSaved = true
                }else{
                    viewModel.insertLocation(locationDetails)
                    if (response.value is Response.Success) {
                        Log.i("TAG", "SaveLocation: done")
                        Toast.makeText(context, context.getString(R.string.added_successfully)
                            , Toast.LENGTH_SHORT).show()
                        isSaved = true
                    }else{
                        Toast.makeText(context, context.getString(R.string.an_error_occurred)
                            , Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                val locationDetails = Location(
                    name = location.name,
                    country = location.country,
                    lon = location.lon,
                    lat = location.lat,
                    arabicName = arabicData[0]
                )
                viewModel.insertLocation(locationDetails)
                if (response.value is Response.Success) {
                    Log.i("TAG", "SaveLocation: done")
                    Toast.makeText(context, context.getString(R.string.added_successfully)
                        , Toast.LENGTH_SHORT).show()
                    isSaved = true
                }else{
                    Toast.makeText(context, context.getString(R.string.an_error_occurred)
                        , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

