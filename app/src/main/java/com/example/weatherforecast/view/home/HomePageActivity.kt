package com.example.weatherforecast.view.home

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.weatherforecast.MainActivity
import com.example.weatherforecast.R
import com.example.weatherforecast.data.pojo.DailyDetails
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.repo.DailyDataRepository
import com.example.weatherforecast.viewModel.DailyDataViewModel
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
val today = LocalDate.now().toString()
@RequiresApi(Build.VERSION_CODES.O)
val currentHour = LocalTime.now().hour

class HomePageActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
           MainScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
    showBackground = true,
    backgroundColor = 0x48319D
)@Composable
fun MainScreen() {
    var temp by remember { mutableDoubleStateOf(0.0) }
    var feelLike by remember { mutableDoubleStateOf(0.0) }
    var weather by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var todayDetails by remember { mutableStateOf<DailyDetails?>(null) }
    var hourlyList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }
    var daysList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }

    val context = LocalContext.current
    val viewModel: DailyDataViewModel =
        viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository()))

    viewModel.isInternetAvailable(context)
    val internet = viewModel.internet.observeAsState()

    if(internet.value == true) {
        GetWeatherData(
            updateCurrent = { tempValue, feelLikeValue, weatherValue, locationValue, stateValue ->
                temp = tempValue
                feelLike = feelLikeValue
                weather = weatherValue
                location = locationValue
                state = stateValue
            },
            updateList = { hourly, today, days ->
                hourlyList = hourly
                todayDetails = today
                daysList = days
            }
        )
        if (temp==0.0) {
            WaitingGif()
        }
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    TopWeatherSection(weather, feelLike, state)
                }

                item {
                    WeatherLocationSection(temp, location)
                }

                item {
                    HourlyWeatherSection(hourlyList)
                }

                item {
                    TodayDetailsSection(todayDetails)
                }

                item {
                    DailyWeatherSection(daysList)
                }
            }

        }
    }else{
        WaitingGif()
    }


}

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
    updateCurrent: (Double, Double, String, String, String) -> Unit,
    updateList: (
        MutableList<HourlyDetails>,
        DailyDetails?,
        MutableList<HourlyDetails>) -> Unit){
    val context = LocalContext.current
    val viewModel: DailyDataViewModel =
        viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository()))

    val dailyData = viewModel.dailyData.observeAsState()
    val currentWeather = viewModel.currentWeather.observeAsState()
    val message = viewModel.message.observeAsState()
    val currentLocation = MainActivity.LocationManager.locationState.observeAsState()

    val lat = currentLocation.value?.first ?: -1.0
    val long = currentLocation.value?.second ?: -1.0

    LaunchedEffect(lat, long) {
        viewModel.retryFetchWeather(lat, long)

    }

    message.value?.let {
        Log.i("TAG", "Error: ${message.value}")
        Toast.makeText(context, message.value, Toast.LENGTH_SHORT).show()
        viewModel.retryFetchWeather(lat, long)
    }




    if (dailyData.value != null && currentWeather.value != null && message.value == null) {
        val dailyDataList = dailyData.value!!.list
        val todayWeather = dailyDataList.filter {
            it.dt_txt.startsWith(today)
        }
        val hourlyDetails= mutableListOf<HourlyDetails>()
        val closestTime = if (currentHour % 3 == 0) currentHour else (currentHour / 3) * 3 + 3

        val timeString = String.format(Locale.getDefault(), "%02d:00:00", closestTime)

        val current = todayWeather.find {
            it.dt_txt.startsWith(today) && it.dt_txt.contains(timeString)
        }
        var dailyDetail: DailyDetails? = null
        if (current != null) {
            val pressure = current.main.pressure
            val humidity = current.main.humidity
            val speed = current.wind.speed
            val cloud = current.clouds.all
            val state = current.weather[0].icon

            dailyDetail = DailyDetails(pressure, humidity, speed, cloud,state)
        }

        todayWeather.forEach{
            val time = it.dt_txt.split(" ")[1].substring(0, 5)
            val temp = it.main.temp
            val feel = it.main.feels_like
            val state = it.weather[0].icon

            val hourlyDetail = HourlyDetails(time,temp,feel,state)
            hourlyDetails.add(hourlyDetail)
        }

        val otherWeather = dailyDataList.filterNot {
            it.dt_txt.startsWith(today)
        }
        val daysDetails = mutableListOf<HourlyDetails>()
        otherWeather.forEach{ it ->
            val time = it.dt_txt
            val temp = it.main.temp
            val feel = it.main.feels_like
            val state = it.weather[0].icon

            if(time.split(" ")[1].substring(0, 5)=="00:00"){
                val apiDate = LocalDate.parse(time.substring(0, 10))
                val todayDate = LocalDate.now()

                val dayText = when {
                    apiDate.isEqual(todayDate) -> "Today"
                    apiDate.isEqual(todayDate.plusDays(1)) -> "Tomorrow"
                    else -> apiDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                }

                val days = HourlyDetails(dayText, temp, feel, state)
                daysDetails.add(days)
            }
        }
        updateList(hourlyDetails,dailyDetail,daysDetails)

        val temp = currentWeather.value!!.main.temp
        val feelLike = currentWeather.value!!.main.feels_like
        val weather = currentWeather.value!!.weather.firstOrNull()?.main ?: ""
        val city = currentWeather.value?.name?:""
        val country = currentWeather.value?.sys?.country?:""
        val location = "$city, $country"
        val state = currentWeather.value!!.weather[0].icon

        updateCurrent(temp, feelLike, weather,location,state)

    }

}