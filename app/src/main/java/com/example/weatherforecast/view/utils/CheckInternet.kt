package com.example.weatherforecast.view.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

val mutableInternet: MutableLiveData<Boolean> = MutableLiveData()
val internet: LiveData<Boolean> = mutableInternet

fun isInternetAvailable(context: Context) {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            mutableInternet.postValue(true)
        }

        override fun onLost(network: Network) {
            mutableInternet.postValue(false)
        }
    }

    val networkRequest = NetworkRequest.Builder().build()
    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
}