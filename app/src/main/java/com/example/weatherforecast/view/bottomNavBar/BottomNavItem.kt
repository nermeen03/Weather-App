package com.example.weatherforecast.view.bottomNavBar

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.weatherforecast.R
import com.example.weatherforecast.view.navigation.ScreenRoute

sealed class BottomNavItem(val route: String, val icon: ImageVector, @StringRes val labelResId: Int) {
    data object Home : BottomNavItem(ScreenRoute.HomeScreenRoute.route, Icons.Default.Home, R.string.home)
    data object Favorite : BottomNavItem(ScreenRoute.FavScreenRoute.route, Icons.Default.Favorite, R.string.favorite)
    data object Alert : BottomNavItem(ScreenRoute.AlertScreenRoute.route, Icons.Default.Notifications, R.string.alerts)
    data object Settings : BottomNavItem(ScreenRoute.SettingsScreenRoute.route, Icons.Default.Settings, R.string.settings)
}


