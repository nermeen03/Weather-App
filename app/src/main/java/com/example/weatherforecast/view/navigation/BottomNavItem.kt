package com.example.weatherforecast.view.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val icon: ImageVector, val label: String) {
    data object Home : BottomNavItem(Icons.Default.Home, "Home")
    data object Favorite : BottomNavItem(Icons.Default.Favorite, "Favorite")
    data object Alert : BottomNavItem(Icons.Default.Notifications, "Alerts")
    data object Settings : BottomNavItem(Icons.Default.Settings, "Settings")
}
