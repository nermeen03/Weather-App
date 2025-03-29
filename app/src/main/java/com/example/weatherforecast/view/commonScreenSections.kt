package com.example.weatherforecast.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.weatherforecast.MyApplication
import com.example.weatherforecast.R
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.pojo.DailyDetails
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.WeatherDetails
import com.example.weatherforecast.view.utils.internet
import com.example.weatherforecast.view.utils.isInternetAvailable
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
            text = stringResource(id = R.string.top_name),
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
                contentDescription = stringResource(R.string.refresh),
                tint = Color.White
            )
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TopWeatherSection(weather: String, feelLike: Double,state: String,weatherArabic: String) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    val weatherValue = if(application.getCurrentLanguage(context) == "en") weather else weatherArabic

    Log.i("TAG", "TopWeatherSection: weather is $weatherArabic")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(weatherValue, fontSize = 24.sp, color = Color.White)
            Text(stringResource(R.string.feels_like)+ " " + application.convertTemperature(feelLike) + " " + application.translateChar(), color = Color.White)
        }
        Column {
            AsyncImage(
                model = "https://openweathermap.org/img/wn/$state@2x.png",
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(stringResource(R.string.today), fontSize = 20.sp, color = Color.White)
            val today = LocalDate.now().toString()
            Text(application.convertDateToArabic(today), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun WeatherLocationSection(temp: Double,location:String,locationArabic: String) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    val locValue = if(application.getCurrentLanguage(context) == "en") location else locationArabic

    Log.i("TAG", "TopWeatherSection: loc is ${application.getCurrentLanguage(context)}")

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
                text = application.convertTemperature(temp) + " " +application.translateChar(),
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = locValue,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }


}

@Composable
fun HourlyWeatherSection(hourlyList: List<HourlyDetails>) {
    Text(stringResource(R.string.hourly_forecast), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(hourlyList.size) {
            HourlyDataCard(hourlyList[it])
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TodayDetailsSection(details: DailyDetails?) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    Text(stringResource(R.string.today_details), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherDetailItem(stringResource(R.string.pressure),
                    (application.convertToArabicNumbers(details?.pressure)
                        ?: stringResource(R.string.dash)) + stringResource(R.string.hpa))
                WeatherDetailItem(stringResource(R.string.speed),
                    (application.convertWindSpeed(details?.speed)
                        ?: stringResource(R.string.dash)) + application.translateSpeedChar())
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherDetailItem(stringResource(R.string.humidity),
                    (application.convertToArabicNumbers(details?.humidity)
                        ?: stringResource(R.string.dash)) + stringResource(R.string.percent))
                WeatherDetailItem(stringResource(R.string.cloud),
                    (application.convertToArabicNumbers(details?.cloud)
                        ?: stringResource(R.string.dash)) + stringResource(R.string.percent))
            }
        }
    }
}

@Composable
fun DailyWeatherSection(daysList: List<HourlyDetails>) {
    Text(
        stringResource(R.string.daily_forecast),
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        daysList.forEach {
            DayDataCard(it)
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HourlyDataCard(details: HourlyDetails) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
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
            Text(application.convertTimeToArabic(details.time), color = colorResource(id = R.color.light_green), fontSize = 14.sp)
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${details.state}@2x.png",
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
            Text(application.convertTemperature(details.temp)+" "+ application.translateChar(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DayDataCard(details: HourlyDetails) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
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
                    text = application.convertDayToArabic(details.time),
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
                    text = application.convertTemperature(details.temp)+" "+application.translateChar(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = stringResource(R.string.feels_like),
                        color = colorResource(id = R.color.light_green),
                        fontSize = 12.sp
                    )
                    Text(
                        text = application.convertTemperature(details.feelLike)+" "+application.translateChar(),
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
    arabicData:(List<String>)->Unit,
    viewModel: DailyDataViewModel,

) {
    val context = LocalContext.current
    val currentWeather = viewModel.currentWeatherDetails.collectAsStateWithLifecycle()
    val filteredWeather = viewModel.filteredWeatherData.collectAsStateWithLifecycle()
    val langData = viewModel.currentArabicDetails.collectAsStateWithLifecycle()

    val message = viewModel.response.collectAsStateWithLifecycle()
    val application = context.applicationContext as MyApplication
    val currentLocation = application.currentLocation.collectAsStateWithLifecycle()

    isInternetAvailable(context)
    val internet = internet.collectAsStateWithLifecycle()

    if(!application.reStarted) {
        LaunchedEffect(currentLocation.value, internet.value) {
            val lat = currentLocation.value.first
            val long = currentLocation.value.second

            if (internet.value && lat != -1.0 && long != -1.0) {
                Log.i("TAG", "MainScreen: Fetching weather data for $lat, $long")
                viewModel.fetchWeatherData(lat, long)
            }
        }
    }
    /*LaunchedEffect(Unit) {
        snapshotFlow { currentLocation.value }
            .distinctUntilChanged()
            .debounce(300)
            .flowOn(Dispatchers.IO)
            .collectLatest { loc ->
                val (lat, long) = loc
                if (internet.value && lat != -1.0 && long != -1.0) {
                    Log.i("TAG", "Fetching weather data for $lat, $long")
                    viewModel.fetchWeatherData(lat, long)
                }
            }
    }*/

    if(message.value == Response.Success && currentWeather.value != null && filteredWeather.value?.second != emptyList<HourlyDetails>() && langData.value.isNotEmpty()){
        updateCurrent(currentWeather.value!!)
        updateList(filteredWeather.value?.first?:emptyList(),filteredWeather.value?.second?: emptyList())
        arabicData(langData.value)
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
    lat:Double,long:Double,
    arabicData:(List<String>)->Unit
    ,viewModel: DailyDataViewModel
) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication

    val currentWeather = viewModel.currentWeatherDetails.collectAsStateWithLifecycle()
    val filteredWeather = viewModel.filteredWeatherData.collectAsStateWithLifecycle()
    val langData = viewModel.currentArabicDetails.collectAsStateWithLifecycle()

    val message = viewModel.response.collectAsStateWithLifecycle()

    if(!application.reStarted) {
        LaunchedEffect(lat, long) {
            if (lat != -1.0 && long != -1.0) {
                viewModel.fetchWeatherData(lat, long)
            }
        }
    }

    if(message.value == Response.Success && currentWeather.value != null && filteredWeather.value?.second != emptyList<HourlyDetails>() ){
        updateCurrent(currentWeather.value!!)
        updateList(filteredWeather.value?.first?:emptyList(),filteredWeather.value?.second?: emptyList())
        arabicData(langData.value)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopFetchingWeather()
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
fun refreshWeather(viewModel: DailyDataViewModel,context: Context) {
    val application = context.applicationContext as MyApplication

    val currentLocation =  application.currentLocation
    val lat = currentLocation.value.first
    val long = currentLocation.value.second

    Toast.makeText(context, context.getString(R.string.loading), Toast.LENGTH_SHORT).show()
    viewModel.fetchWeatherData(lat,long)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherSections(
    viewModel: DailyDataViewModel,
    context: Context,
    weather: String,
    feelLike: Double,
    state: String,
    temp: Double,
    location: String,
    hourlyList: List<HourlyDetails>,
    todayDetails: DailyDetails?,
    daysList: List<HourlyDetails>,
    arabicData:List<String>?){
    LazyColumn(
            Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { TopBar(viewModel, context) }
        item { TopWeatherSection(weather, feelLike, state, arabicData?.get(1) ?: weather) }
        item { WeatherLocationSection(temp, location, arabicData?.get(0) ?: location) }
        item { HourlyWeatherSection(hourlyList) }
        item { TodayDetailsSection(todayDetails) }
        item { DailyWeatherSection(daysList) }
    }
}
