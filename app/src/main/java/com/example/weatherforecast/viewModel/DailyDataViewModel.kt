package com.example.weatherforecast.viewModel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.pojo.CurrentWeatherResponse
import com.example.weatherforecast.data.pojo.ForecastDataResponse
import com.example.weatherforecast.data.repo.IDailyDataRepository
import com.example.weatherforecast.data.repo.Response
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DailyDataViewModel(private val dataRepository: IDailyDataRepository):ViewModel(){
    private val mutableDailyData = MutableStateFlow<ForecastDataResponse?>(null)
    val dailyData:StateFlow<ForecastDataResponse?> = mutableDailyData

    private val mutableCurrentWeather  = MutableStateFlow<CurrentWeatherResponse?>(null)
    val currentWeather = mutableCurrentWeather.asStateFlow()

    private val mutableMessage:MutableLiveData<String?> = MutableLiveData()
    val message:LiveData<String?> = mutableMessage

    private val mutableResponse = MutableStateFlow(Response.Loading as Response<*>)
    val response = mutableResponse.asStateFlow()


    private val handle = CoroutineExceptionHandler { _, exception ->
        mutableResponse.value = Response.Failure(exception)
    }

    private fun getDailyData(lat: Double, lon: Double) {

        viewModelScope.launch(Dispatchers.IO + handle) {
            while (true) {
                launch {
                    dataRepository.getDailyData(lat, lon)
                        .distinctUntilChanged()
                        .catch { e -> mutableResponse.value = Response.Failure(e) }
                        .collect {
                            mutableDailyData.value = it
                            mutableResponse.value = Response.Success
                        }
                }
                delay(3600000)
            }
        }
    }

    private fun getCurrentWeather(lat: Double, lon: Double) {

        viewModelScope.launch(Dispatchers.IO + handle) {
            while (true) {
                launch {
                    dataRepository.getCurrentWeather(lat, lon)
                        .distinctUntilChanged()
                        .catch { e -> mutableResponse.value = Response.Failure(e) }
                        .collect {
                            mutableCurrentWeather.value = it
                            mutableResponse.value = Response.Success
                        }
                }
                delay(600000)
            }
        }

    }
    fun retryFetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch (Dispatchers.IO + handle) {
            while (true) {
                if (internet.value == true) {
                    if (lat != -1.0 && lon != -1.0) {
                        getDailyData(lat, lon)
                        getCurrentWeather(lat, lon)
                        break
                    }
                    delay(10000)
                }
                else{
                    break
                }

            }
        }
    }

    fun stopFetchingWeather() {
        viewModelScope.coroutineContext.cancelChildren()
    }
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

}
class DailyDataViewModelFactory(private val dataRepository: IDailyDataRepository):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DailyDataViewModel(dataRepository) as T
    }
}
