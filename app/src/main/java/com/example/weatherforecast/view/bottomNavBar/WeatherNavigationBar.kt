package com.example.weatherforecast.view.bottomNavBar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.view.navigation.SetUpNavHost

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherNavigationBar() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
                BottomNavigationBar(navController)
        }) { paddingValues ->
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
            SetUpNavHost(navController)
        }
    }
}
