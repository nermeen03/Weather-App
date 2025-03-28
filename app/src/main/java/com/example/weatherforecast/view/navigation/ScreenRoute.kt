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
    data object SettingsScreenRoute : ScreenRoute("settings")
    @Serializable
    data object DetailsOfflineRoute : ScreenRoute("details_offline_screen/{lat}/{lon}") {
        fun withArgs(lat: Double, lon: Double): String = "details_offline_screen/$lat/$lon"
    }
    companion object {
        fun detailsRoute(lat: Double, lon: Double): String = "details/$lat/$lon"
    }
}

