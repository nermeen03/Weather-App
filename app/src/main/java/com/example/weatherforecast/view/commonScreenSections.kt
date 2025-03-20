package com.example.weatherforecast.view

import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.weatherforecast.R
import com.example.weatherforecast.data.pojo.DailyDetails
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.data.repo.Response
import com.example.weatherforecast.view.utils.AppLocationHelper
import com.example.weatherforecast.viewModel.DailyDataViewModel
import java.time.LocalDate


@Composable
fun WaitingGif() {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Box(
        modifier = Modifier
            .fillMaxSize() ,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = R.raw.waiting,
            contentDescription = null,
            imageLoader = imageLoader,
            Modifier.fillMaxSize()
        )
    }
}

@Composable
fun NoInternetGif() {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Box(
        modifier = Modifier
            .fillMaxSize() ,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = R.raw.no,
            contentDescription = null,
            imageLoader = imageLoader,
            Modifier.fillMaxSize()
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TopBar(viewModel: DailyDataViewModel, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically ,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(1f))

        Text(
            text = "Weather App",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(4f),
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = { refreshWeather(viewModel, context) },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color.White
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TopWeatherSection(weather: String, feelLike: Double,state: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(weather, fontSize = 24.sp, color = Color.White)
            Text("Feels like $feelLike°C", color = Color.White)
        }
        Column {
            AsyncImage(
                model = "https://openweathermap.org/img/wn/$state@2x.png",
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Today", fontSize = 20.sp, color = Color.White)
            val today = LocalDate.now().toString()
            Text(today, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WeatherLocationSection(temp: Double,location:String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$temp°C",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = location,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }


}

@Composable
fun HourlyWeatherSection(hourlyList: List<HourlyDetails>) {
    Text("Hourly Forecast", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(hourlyList.size) {
            HourlyDataCard(hourlyList[it])
        }
    }
}

@Composable
fun TodayDetailsSection(details: DailyDetails?) {
    Text("Today Details", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherDetailItem("Pressure", "${details?.pressure ?: "--"} hPa")
                WeatherDetailItem("Speed", "${details?.speed ?: "--"} km/h")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherDetailItem("Humidity", "${details?.humidity ?: "--"}%")
                WeatherDetailItem("Cloud", "${details?.cloud ?: "--"}%")
            }
        }
    }
}

@Composable
fun DailyWeatherSection(daysList: List<HourlyDetails>) {
    Text(
        "Daily Forecast",
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        daysList.forEach {
            DayDataCard(it)
        }
    }
}


@Composable
fun HourlyDataCard(details: HourlyDetails) {
    Card(
        modifier = Modifier
            .size(90.dp, 130.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(details.time, color = colorResource(id = R.color.light_green), fontSize = 14.sp)
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${details.state}@2x.png",
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
            Text("${details.temp}°C", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WeatherDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = colorResource(id = R.color.light_green), fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DayDataCard(details: HourlyDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.dark)),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = details.time,
                    color = colorResource(id = R.color.light_green),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    AsyncImage(
                        model = "https://openweathermap.org/img/wn/${details.state}@2x.png",
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.CenterEnd)
                    )
                }

            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${details.temp}°C",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = "Feels like",
                        color = colorResource(id = R.color.light_green),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${details.feelLike}°C",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GetWeatherData(
    updateCurrent: (WeatherDetails) -> Unit,
    updateList: (
        List<HourlyDetails>,
        List<HourlyDetails>) -> Unit,
    viewModel: DailyDataViewModel
) {
    val currentWeather = viewModel.currentWeatherDetails.collectAsStateWithLifecycle()
    val filteredWeather = viewModel.filteredWeatherData.collectAsStateWithLifecycle()

    val message = viewModel.response.collectAsStateWithLifecycle()
    val currentLocation =  AppLocationHelper.locationState.observeAsState()

    val lat = currentLocation.value?.first ?: -1.0
    val long = currentLocation.value?.second ?: -1.0

    LaunchedEffect(lat, long) {
        viewModel.fetchWeatherData(lat, long)
    }

    if(message.value == Response.Success && currentWeather.value != null && filteredWeather.value?.second != emptyList<HourlyDetails>() ){
        updateCurrent(currentWeather.value!!)
        updateList(filteredWeather.value?.first?:emptyList(),filteredWeather.value?.second?: emptyList())

    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopFetchingWeather()
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GetWeatherDataByLoc(
    updateCurrent: (WeatherDetails) -> Unit,
    updateList: (
        List<HourlyDetails>,
        List<HourlyDetails>) -> Unit,
    lat:Double,long:Double,viewModel: DailyDataViewModel
) {
    val currentWeather = viewModel.currentWeatherDetails.collectAsStateWithLifecycle()
    val filteredWeather = viewModel.filteredWeatherData.collectAsStateWithLifecycle()

    val message = viewModel.response.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchWeatherData(lat, long)
    }

    if(message.value == Response.Success && currentWeather.value != null && filteredWeather.value?.second != emptyList<HourlyDetails>() ){
        updateCurrent(currentWeather.value!!)
        updateList(filteredWeather.value?.first?:emptyList(),filteredWeather.value?.second?: emptyList())

    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopFetchingWeather()
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
fun refreshWeather(viewModel: DailyDataViewModel,context: Context) {
    val currentLocation =  AppLocationHelper.locationState
    val lat = currentLocation.value?.first ?: -1.0
    val long = currentLocation.value?.second ?: -1.0

    Toast.makeText(context, "Loading.....", Toast.LENGTH_SHORT).show()
    viewModel.fetchWeatherData(lat,long)
}
