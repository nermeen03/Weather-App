package com.example.weatherforecast.view.utils

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.weatherforecast.MyApplication
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.first

object AppLocationHelper {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    var locationState = androidx.lifecycle.MutableLiveData(Pair(-1.0, -1.0))

    fun checkPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun enableLocationServices(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    @SuppressLint("MissingPermission")
    suspend fun getFreshLocation(context: Context) {
        val application = context.applicationContext as MyApplication
        val locationType = application.location.first()

        if (locationType != "GPS") {
            Log.d("TAG", "GPS is not selected, location update ignored.")
            return
        }

        fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(500).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    val newLocation = Pair(it.latitude, it.longitude)
                    if(locationState.value != newLocation){
                        application.setCurrentLocation(newLocation)
                        locationState.value = newLocation
                        if(locationType=="Map"){
                            fusedClient.removeLocationUpdates(locationCallback)
                        }
                    }
                }
            }
        }

        fusedClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.getMainLooper()
        )
    }

}
