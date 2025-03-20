package com.example.weatherforecast.view.bottomNavBar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.weatherforecast.R


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorite,
        BottomNavItem.Alert,
        BottomNavItem.Settings
    )

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    NavigationBar(
        containerColor = if (isSystemInDarkTheme()) {
            colorResource(R.color.dark_purple)
        } else {
            colorResource(R.color.white)
        }
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.route == item.route

            NavigationBarItem(
                label = { Text(text = item.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) Color.Black else colorResource(R.color.light)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
