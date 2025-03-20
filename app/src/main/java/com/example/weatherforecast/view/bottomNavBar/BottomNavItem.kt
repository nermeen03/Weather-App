package com.example.weatherforecast.view.bottomNavBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.weatherforecast.view.navigation.ScreenRoute

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Home : BottomNavItem(ScreenRoute.HomeScreenRoute.route, Icons.Default.Home, "Home")
    data object Favorite : BottomNavItem(ScreenRoute.FavScreenRoute.route, Icons.Default.Favorite, "Favorite")
    data object Alert : BottomNavItem(ScreenRoute.AlertScreenRoute.route, Icons.Default.Notifications, "Alerts")
    data object Settings : BottomNavItem(ScreenRoute.SettingsScreenRoute.route, Icons.Default.Settings, "Settings")
}

