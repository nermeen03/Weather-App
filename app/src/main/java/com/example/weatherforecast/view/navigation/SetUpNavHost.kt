package com.example.weatherforecast.view.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.weatherforecast.view.alert.AlertsScreen
import com.example.weatherforecast.view.favorite.DetailsScreen
import com.example.weatherforecast.view.favorite.DetailsScreenOffline
import com.example.weatherforecast.view.favorite.FavScreen
import com.example.weatherforecast.view.favorite.MapScreen
import com.example.weatherforecast.view.home.MainScreen
import com.example.weatherforecast.view.settings.SettingsScreen


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SetUpNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.HomeScreenRoute.route
    ) {
        composable(ScreenRoute.HomeScreenRoute.route) { MainScreen() }
        composable(ScreenRoute.FavScreenRoute.route){
            FavScreen(navController, navToDetails = { lat, lon ->
                navController.navigate("details/$lat/$lon") {
                    launchSingleTop = true
                }
            })
        }
        composable(ScreenRoute.MapScreenRoute.route) { MapScreen(navController) }
        composable("details/{lat}/{lon}") { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
            DetailsScreen(lat, lon)
        }
        composable(
            route = ScreenRoute.DetailsOfflineRoute.route,
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
            DetailsScreenOffline(lat, lon)
        }
        composable(ScreenRoute.AlertScreenRoute.route) { AlertsScreen() }

        composable(ScreenRoute.SettingsScreenRoute.route) { SettingsScreen(navController) }

    }
}
