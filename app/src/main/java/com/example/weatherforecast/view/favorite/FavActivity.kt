package com.example.weatherforecast.view.favorite

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.R
import com.example.weatherforecast.data.local.FavLocationsDataBase
import com.example.weatherforecast.data.local.FavLocationsLocalDataSource
import com.example.weatherforecast.data.pojo.Location
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.FavLocationsRemoteDataSource
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.FavLocationsRepository
import com.example.weatherforecast.data.repo.Response
import com.example.weatherforecast.viewModel.FavLocationsViewModel

class FavActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShowFav()
        }
    }

    @Composable
    fun ShowFav() {
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

        if (favList.isEmpty()) {
            NoFav()
        } else {
            FavList(favList,viewModel)
        }
    }

    @Composable
    fun NoFav() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(color = Color.White, text = "You don't have favorites", fontSize = 20.sp)
        }
    }

    @Composable
    fun FavList(list: List<Location>,viewModel: FavLocationsViewModel) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(list.size) {
                FavRow(list[it],viewModel)
            }
        }
    }

    @Composable
    fun FavRow(item: Location, viewModel: FavLocationsViewModel) {
        val context = LocalContext.current

        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${item.name}, ${item.country}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lat: ${item.lat}, Lon: ${item.lon}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = {
                        viewModel.deleteLocation(item.lat, item.lon)
                        when (viewModel.response.value) {
                            is Response.Success -> {
                                Toast.makeText(context, "Removed successfully", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(context, "Couldn't be removed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.secondaryLight),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text("Remove")
                }
            }
        }
    }


}