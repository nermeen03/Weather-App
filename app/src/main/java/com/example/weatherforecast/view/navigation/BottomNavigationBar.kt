package com.example.weatherforecast.view.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.weatherforecast.R

@Composable
fun BottomNavigationBar(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorite,
        BottomNavItem.Alert,
        BottomNavItem.Settings
    )

    NavigationBar(containerColor =
            if (isSystemInDarkTheme()) {
                colorResource(R.color.dark)
                } else {
                colorResource(R.color.blue_purple)
                }
            ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selectedItem == item) Color.Black else Color.Gray
                    )
                },
                label = {
                    Text(item.label, color = if (selectedItem == item) Color.White else Color.Gray)
                }
            )
        }
    }
}
