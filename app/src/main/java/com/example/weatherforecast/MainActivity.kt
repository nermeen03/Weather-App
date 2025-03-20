package com.example.weatherforecast

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.weatherforecast.view.bottomNavBar.WeatherNavigationBar
import com.example.weatherforecast.view.utils.AppLocationHelper
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

const val My_LOCATION_PERMISSION_ID = 200

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, "2fc5f5f3f6a9b61df9391d8ae569f5e0", WellKnownTileServer.MapTiler)

        enableEdgeToEdge()
        setContent {
            WeatherNavigationBar()
        }
    }

    override fun onStart() {
        super.onStart()
        if (AppLocationHelper.checkPermissions(this)) {
            if (AppLocationHelper.isLocationEnabled(this)) {
                AppLocationHelper.getFreshLocation(this)
            } else {
                AppLocationHelper.enableLocationServices(this)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
                My_LOCATION_PERMISSION_ID
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == My_LOCATION_PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }) {
                AppLocationHelper.getFreshLocation(this)
            }
        }
    }
}
