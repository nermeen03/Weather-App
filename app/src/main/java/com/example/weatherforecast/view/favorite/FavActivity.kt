package com.example.weatherforecast.view.favorite

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.SnackbarDuration
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
import com.example.weatherforecast.MyApplication
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
import com.example.weatherforecast.viewModel.FavLocationsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FavScreen(navController: NavHostController) {
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
                    if (internet.value) {
                        navController.navigate(ScreenRoute.MapScreenRoute.route) {
                            launchSingleTop = true
                        }
                    }
                    else{
                        coroutineScope.launch {
                            val result = snackBarHostState.showSnackbar(
                                message = context.getString(R.string.no_internet_connection),
                                actionLabel = context.getString(R.string.retry)
                            )
                            if (result == SnackbarResult.ActionPerformed) {
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
                Box(modifier = Modifier.weight(1f)) {
                    FavList(
                        list = favList,
                        viewModel = viewModel,
                        snackBarHostState = snackBarHostState,
                        coroutineScope = coroutineScope,
                        navController = navController
                    )
                }
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FavList(
    list: List<Location>,
    viewModel: FavLocationsViewModel,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    navController: NavHostController
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
                coroutineScope = coroutineScope,
                navController = navController
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FavRow(
    item: Location,
    viewModel: FavLocationsViewModel,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    val textValue = if (application.getCurrentLanguage(context) == "en")
        "${item.name}, ${item.country}"
    else
        item.arabicName
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                if (internet.value) {
                    navController.navigate("details/${item.lat}/${item.lon}") {
                        launchSingleTop = true
                    }
                } else {
                    viewModel.getFavDetails(item.lon, item.lat)

                    coroutineScope.launch {
                        viewModel.detailsResponse.collectLatest { response ->
                            when (response) {
                                is Response.Success -> {
                                    navController.navigate(
                                        ScreenRoute.DetailsOfflineRoute.withArgs(item.lat, item.lon)
                                    ) {
                                        popUpTo(ScreenRoute.FavScreenRoute.route) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }

                                is Response.Failure -> {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.no_offline_data_was_saved_for_this_location),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                Response.Loading -> Log.d("TAG", "FavRow: Waiting for response...")
                            }
                        }
                    }
                }

            },
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
                    text = textValue,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.lat) + application.convertToArabicNumbers(item.lat) +
                            stringResource(R.string.lon) + application.convertToArabicNumbers(item.lon),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    viewModel.deleteLocation(item.lat, item.lon)
                    coroutineScope.launch {
                        val result = snackBarHostState.showSnackbar(
                            message = context.getString(R.string.deleted_successfully),
                            actionLabel = context.getString(R.string.undo),
                            duration = SnackbarDuration.Long
                        )

                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.insertLocation(item)
                        }else{
                            withContext(Dispatchers.IO){
                                viewModel.deleteLocationDetail(item.lat, item.lon)
                            }
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

