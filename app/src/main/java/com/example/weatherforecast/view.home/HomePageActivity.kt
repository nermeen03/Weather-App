package com.example.weatherapp.view.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.RetrofitHelper
import com.example.weatherforecast.data.repo.DailyDataRepository
import com.example.weatherforecast.viewModel.DailyDataViewModel
import com.example.weatherforecast.viewModel.DailyDataViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
           get()
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Preview(
    showBackground = true,
    backgroundColor = 0x48319D
)@Composable
 fun MainScreen(){

    Column (Modifier.fillMaxSize()){
        Row(Modifier
            .fillMaxWidth()
            .padding(50.dp),horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f),horizontalAlignment = Alignment.Start) {
                Text("light snow", color = Color.White)
                Text("feel like -10", color = Color.White)
            }
            Column(modifier = Modifier.weight(1f),horizontalAlignment = Alignment.End) {
                Text("Today", color = Color.White)
                Text("Fri, 18 feb", color = Color.White)
            }
        }
        Column(){
            Column(Modifier.fillMaxWidth(),horizontalAlignment = Alignment.CenterHorizontally) {
                Text("-10")
                Text("Norway, Nordland")
            }
            Column {
                Text("Hourly Details")
                //LazyRow
            }
            Column {
                Text("Daily Details")
                //LazyColumn
            }
        }
    }

}
@Composable
fun get(){
    val viewModel: DailyDataViewModel = viewModel(factory = DailyDataViewModelFactory(DailyDataRepository.getRepository()))
    viewModel.getDailyData()
    val dailyData = viewModel.dailyData.observeAsState()

    Log.i("TAG", "get: success $dailyData")

}