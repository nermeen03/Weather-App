package com.example.weatherforecast.view.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import com.example.weatherforecast.R
import com.example.weatherforecast.view.home.MainScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherNavigationBar() {
    var selectedItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Home) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedItem) {
                selectedItem = it
            }
        }
    ) { paddingValues ->
        Column(
            Modifier
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
            when (selectedItem) {
                BottomNavItem.Home -> MainScreen()
                BottomNavItem.Favorite -> Log.i("TAG", "WeatherNavigationBar: fav")
                BottomNavItem.Alert -> Log.i("TAG", "WeatherNavigationBar: alert")
                BottomNavItem.Settings -> Log.i("TAG", "WeatherNavigationBar: settings")
            }
        }
    }
}
