package com.example.weatherforecast.view.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow

val mutableInternet: MutableStateFlow<Boolean> = MutableStateFlow(false)
val internet: MutableStateFlow<Boolean> = mutableInternet

fun isInternetAvailable(context: Context) {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            mutableInternet.value = (true)
        }

        override fun onLost(network: Network) {
            mutableInternet.value = (false)
        }
    }

    val networkRequest = NetworkRequest.Builder().build()
    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
}