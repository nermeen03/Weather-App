package com.example.weatherforecast.view.favorite

import android.util.Log
import android.widget.Toast
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
import com.example.weatherforecast.R
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.local.favorite.FavLocationsDataBase
import com.example.weatherforecast.data.local.favorite.FavLocationsLocalDataSource
import com.example.weatherforecast.data.pojo.LocalNames
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.pojo.NameResponseItem
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.FavLocationsRemoteDataSource
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.FavLocationsRepository
import com.example.weatherforecast.viewModel.FavLocationsViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView


@Composable
fun MapScreen(navController:NavHostController) {
    val previousEntry = navController.previousBackStackEntry
    val previousRoute = previousEntry?.destination?.route

    Log.i("TAG", "$previousRoute == favorite")

    val context = LocalContext.current
    val apiKey = stringResource(R.string.map_api)

    val viewModel: FavLocationsViewModel = viewModel(factory = FavLocationsViewModel.FavLocationsViewModelFactory(
        FavLocationsRepository.getRepository(
            FavLocationsLocalDataSource(FavLocationsDataBase.getInstance(context).getFavLocationsDao()),
            FavLocationsRemoteDataSource(RetrofitHelper.retrofitInstance.create(ApiService::class.java))
        )
    ))
    LaunchedEffect(Unit) {
        viewModel.loadCountries(context)
    }
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val responseState by viewModel.response.collectAsState()
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
                    viewModel.onMapReady(map, apiKey)
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

            viewModel.filterCountries(searchQuery.value)
            val filtered = viewModel.filteredCountries.collectAsStateWithLifecycle().value

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
                                    viewModel.addMark(mapLibreMap, latLng)
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
                    SelectedLocationByName(locationSelected!!,viewModel)
                }
            }
        }
        selectedLocation?.let { location ->

            LaunchedEffect(location) {
                viewModel.getLocationName(location.latitude, location.longitude)
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
                                SelectedLocationByName(locationName!!, viewModel)
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

@Composable
fun SelectedLocationByName(location: NameResponseItem, viewModel: FavLocationsViewModel) {
    val context = LocalContext.current

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
                    viewModel.insertLocation(
                        Location(
                            name = location.name,
                            country = location.country,
                            lon = location.lon,
                            lat = location.lat
                        )
                    )

                    when (viewModel.response.value) {
                        is Response.Success -> {
                            Toast.makeText(context,
                                context.getString(R.string.added_successfully), Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            Toast.makeText(context,
                                context.getString(R.string.couldn_t_be_added), Toast.LENGTH_SHORT).show()
                        }
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
        }
    }
}
