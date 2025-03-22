package com.example.weatherforecast.view.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenRoute(val route: String) {
    @Serializable
    data object HomeScreenRoute : ScreenRoute("home")
    @Serializable
    data object FavScreenRoute : ScreenRoute("favorite")
    @Serializable
    data object MapScreenRoute : ScreenRoute("map")
    @Serializable
    data class DetailScreenRoute(val lat:Double, val lon:Double) : ScreenRoute("details/$lat/$lon")
    @Serializable
    data object AlertScreenRoute : ScreenRoute("alert")
    @Serializable
    data object ChoosingScreenRoute : ScreenRoute("choose")
    @Serializable
    data object SettingsScreenRoute : ScreenRoute("settings")

    companion object {
        fun detailsRoute(lat: Double, lon: Double): String = "details/$lat/$lon"
    }
}

