package com.example.weatherforecast.view.favorite

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.weatherforecast.R
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.local.favorite.FavLocationsDataBase
import com.example.weatherforecast.data.local.favorite.FavLocationsLocalDataSource
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.FavLocationsRemoteDataSource
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.FavLocationsRepository
import com.example.weatherforecast.view.navigation.ScreenRoute
import com.example.weatherforecast.view.utils.internet
import com.example.weatherforecast.view.utils.isInternetAvailable
import com.example.weatherforecast.viewModel.FavLocationsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun FavScreen(navController: NavHostController,
              navToDetails: (lat: Double, lon: Double) -> Unit) {
    val context = LocalContext.current
    val viewModel: FavLocationsViewModel = viewModel(
        factory = FavLocationsViewModel.FavLocationsViewModelFactory(
            FavLocationsRepository.getRepository(
                FavLocationsLocalDataSource(FavLocationsDataBase.getInstance(context).getFavLocationsDao()),
                FavLocationsRemoteDataSource(RetrofitHelper.retrofitInstance.create(ApiService::class.java))
            )
        )
    )

    LaunchedEffect(Unit) {
        viewModel.getAllFav()
    }

    val favList by viewModel.favLocations.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) } ,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    isInternetAvailable(context)
                    if (internet.value) {
                        navController.navigate(ScreenRoute.MapScreenRoute.route) {
                            launchSingleTop = true
                        }
                    }
                    else{
                        coroutineScope.launch {
                            val result = snackBarHostState.showSnackbar(
                                message = "No internet connection!",
                                actionLabel = "Retry"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                isInternetAvailable(context)
                                if (internet.value) {
                                    navController.navigate(ScreenRoute.MapScreenRoute.route) {
                                        launchSingleTop = true
                                    }
                                }
                            }

                        }
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.go_to_map))
            }
        }
    ) { paddingValues ->
        Column(Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSystemInDarkTheme()) {
                        listOf(
                            colorResource(id = R.color.primaryDark),
                            colorResource(id = R.color.secondaryDark)
                        )
                    } else {
                        listOf(
                            colorResource(id = R.color.primaryLight),
                            colorResource(id = R.color.secondaryLight)
                        )
                    }
                )
            )
            .padding(paddingValues)
        ) {
            if (favList.isEmpty()) {
                NoFav()
            } else {
                FavList(
                    list = favList,
                    viewModel = viewModel,
                    navToDetails = navToDetails,
                    snackBarHostState = snackBarHostState,
                    coroutineScope = coroutineScope
                )
            }
        }
    }
}

@Composable
fun NoFav() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(color = Color.White, text = stringResource(R.string.you_don_t_have_favorites), fontSize = 20.sp)
    }
}

@Composable
fun FavList(
    list: List<Location>,
    viewModel: FavLocationsViewModel,
    navToDetails: (lat: Double, lon: Double) -> Unit,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list.size) { index ->
            FavRow(
                item = list[index],
                viewModel = viewModel,
                snackBarHostState = snackBarHostState,
                coroutineScope = coroutineScope
            ) {
                val lat = list[index].lat
                val lon = list[index].lon
                navToDetails(lat, lon)
            }
        }
    }
}



@Composable
fun FavRow(
    item: Location,
    viewModel: FavLocationsViewModel,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    function: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = function),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.name}, ${item.country}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.lat) + item.lat + stringResource(R.string.lon) + item.lon,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val loc = item
                    viewModel.deleteLocation(item.lat, item.lon)
                    when (viewModel.response.value) {
                        is Response.Success -> {
                            coroutineScope.launch {
                                val result = snackBarHostState.showSnackbar(
                                    message = "Deleted Successfully!",
                                    actionLabel = "Undo"
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.insertLocation(loc)
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.couldn_t_be_removed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.secondaryLight),
                    contentColor = Color.White
                ),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(stringResource(R.string.remove))
            }
        }
    }
}


