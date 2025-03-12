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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherforecast.data.remote.ApiService
import com.example.weatherforecast.data.remote.RetrofitHelper
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
            MainScreen()
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Preview(
    showBackground = true,
    backgroundColor = 0x48319D
)@Composable
 fun MainScreen(){

    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            get()
        }
    }


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
suspend fun get(){
    withContext(Dispatchers.IO) {
        val api = RetrofitHelper.retrofitInstance.create(ApiService::class.java)
        try {
            val response = api.get5DaysEvery3HoursData()
            if (response.isSuccessful) {
                Log.d("TAG", "Success: ${response.body()}")
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error fetching weather data", e)
            return@withContext
        }
    }
}