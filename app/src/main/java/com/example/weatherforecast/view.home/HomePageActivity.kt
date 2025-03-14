package com.example.weatherforecast.view.home

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.ktx.Resource
import com.example.weatherforecast.R
import com.example.weatherforecast.data.pojo.DailyDetails
import com.example.weatherforecast.data.pojo.HourlyDetails
import com.example.weatherforecast.data.pojo.Item0
import com.example.weatherforecast.data.repo.DailyDataRepository
import com.example.weatherforecast.viewModel.DailyDataViewModel
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
    var todayDetails by remember { mutableStateOf<DailyDetails?>(null) }
    var hourlyList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }
    var daysList by remember { mutableStateOf<List<HourlyDetails>>(emptyList()) }
    GetWeatherData(
        updateCurrent = { tempValue, feelLikeValue, weatherValue ->
            temp = tempValue
            feelLike = feelLikeValue
            weather = weatherValue
        },
        updateList = {hourly, today, days ->
            hourlyList = hourly
            todayDetails = today
            daysList = days
        }
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.primaryDark),
                        colorResource(id = R.color.secondaryDark)
                    )
                )
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(50.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(weather, color = Color.White)
                Text("feel like $feelLike", color = Color.White)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Today", color = Color.White)
                Text(today, color = Color.White)
            }
        }
        Column(Modifier.padding(20.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$temp", color = Color.White)
                Text("Norway, Nordland")
            }
            Column {
                Text("Hourly Details")
                LazyRow (
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding =
                    PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(hourlyList.size) {
                        HourlyDataCard(hourlyList[it])
                    }
                }
            }
            Column {
                Text("Today Detail", Modifier.fillMaxWidth())
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Pressure", color = Color.White)
                                Text("${todayDetails?.pressure ?: "--"}", color = Color.White)
                            }
                            Column {
                                Text("Speed", color = Color.White)
                                Text("${todayDetails?.speed ?: "--"}", color = Color.White)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Humidity", color = Color.White)
                                Text("${todayDetails?.humidity ?: "--"}", color = Color.White)
                            }
                            Column {
                                Text("Cloud", color = Color.White)
                                Text("${todayDetails?.cloud ?: "--"}", color = Color.White)
                            }
                        }
                    }
                }

            }
            Column {
                Text("Daily Details")
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding =
                    PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(daysList.size) {
                        DayDataCard(daysList[it])
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyDataCard(details: HourlyDetails) {
    Card(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(details.time, color = Color.White)
            Text("${details.temp}°C", color = Color.White)
        }
    }
}


@Composable
fun DayDataCard(details: HourlyDetails) {
    Card(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(details.time, color = Color.White)

                Text("${details.temp}°C", color = Color.White)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Feels Like", color = Color.Gray)
                Text("${details.feelLike}°C", color = Color.White)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GetWeatherData(
    updateCurrent: (Double, Double, String) -> Unit,
    updateList: (
        MutableList<HourlyDetails>,
        DailyDetails?,
        MutableList<HourlyDetails>) -> Unit){
    val viewModel: DailyDataViewModel =
        viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository()))

    val dailyData = viewModel.dailyData.observeAsState()
    val currentWeather = viewModel.currentWeather.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getDailyData()
        viewModel.getCurrentWeather()
    }

    if (dailyData.value != null) {
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

            dailyDetail = DailyDetails(pressure, humidity, speed, cloud)
        }

        todayWeather.forEach{
            val time = it.dt_txt.split(" ")[1].substring(0, 5)
            val temp = it.main.temp
            val feel = it.main.feels_like
            val hourlyDetail = HourlyDetails(time,temp,feel)
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
            if(time.split(" ")[1].substring(0, 5)=="00:00"){
                val apiDate = LocalDate.parse(time.substring(0, 10))
                val todayDate = LocalDate.now()

                val dayText = when {
                    apiDate.isEqual(todayDate) -> "Today"
                    apiDate.isEqual(todayDate.plusDays(1)) -> "Tomorrow"
                    else -> apiDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                }

                val days = HourlyDetails(dayText,temp,feel)
                daysDetails.add(days)
            }
        }
        updateList(hourlyDetails,dailyDetail,daysDetails)
    }
    if (currentWeather.value != null) {
        val temp = currentWeather.value!!.main.temp
        val feelLike = currentWeather.value!!.main.feels_like
        val weather = currentWeather.value!!.weather.firstOrNull()?.main ?: ""
        updateCurrent(temp, feelLike, weather)

    }

}