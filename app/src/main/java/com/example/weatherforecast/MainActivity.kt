package com.example.weatherforecast

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.weatherforecast.view.bottomNavBar.WeatherNavigationBar
import com.example.weatherforecast.view.utils.AppLocationHelper
import com.example.weatherforecast.view.utils.AppLocationHelper.getFreshLocation
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

const val My_LOCATION_PERMISSION_ID = 200

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, "2fc5f5f3f6a9b61df9391d8ae569f5e0", WellKnownTileServer.MapTiler)
        val app = this.application as MyApplication
        app.loadLanguagePreference(this)

        enableEdgeToEdge()
        setContent {
            WeatherNavigationBar()
        }
    }

    override fun onStart() {
        super.onStart()
        val app = this.application as MyApplication
        if(app.location.value == "GPS"){
            if (AppLocationHelper.checkPermissions(this)) {
                if (AppLocationHelper.isLocationEnabled(this)) {
                    lifecycleScope.launch {
                        getFreshLocation(this@MainActivity)
                    }
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
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Allow this app to display alerts over other apps.")
                .setPositiveButton("Grant Permission") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(this, "Overlay permission is needed for alerts", Toast.LENGTH_SHORT).show()
                }
                .show()
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
                lifecycleScope.launch {
                    getFreshLocation(this@MainActivity)
                }
            }
        }
    }
}
