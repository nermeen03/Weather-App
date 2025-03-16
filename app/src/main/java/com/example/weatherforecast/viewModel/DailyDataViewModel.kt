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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DailyDataViewModel(private val dataRepository: IDailyDataRepository):ViewModel(){
    private val mutableDailyData:MutableLiveData<ForecastDataResponse> = MutableLiveData()
    val dailyData:LiveData<ForecastDataResponse> = mutableDailyData

    private val mutableCurrentWeather:MutableLiveData<CurrentWeatherResponse> = MutableLiveData()
    val currentWeather:LiveData<CurrentWeatherResponse> = mutableCurrentWeather

    private val mutableMessage:MutableLiveData<String?> = MutableLiveData()
    val message:LiveData<String?> = mutableMessage


    private val handle = CoroutineExceptionHandler { _, exception ->
        mutableMessage.postValue(exception.message)
    }

    private fun getDailyData(lat:Double,lon:Double){
        viewModelScope.launch(Dispatchers.IO + handle) {
            dataRepository.getDailyData(lat, lon)?.let {
                mutableDailyData.postValue(it)
                mutableMessage.postValue(null)
            }
        }
    }
    private fun getCurrentWeather(lat:Double,lon:Double) {
        viewModelScope.launch(Dispatchers.IO + handle){
            dataRepository.getCurrentWeather(lat, lon)?.let {
                mutableCurrentWeather.postValue(it)
                mutableMessage.postValue(null)
            }
        }
    }
    fun retryFetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            while (true) {
                if (internet.value == true) {
                    if (lat != -1.0 && lon != -1.0) {
                        getDailyData(lat, lon)
                        getCurrentWeather(lat, lon)
                        break
                    }
                    delay(1000)
                }
                else{
                    break
                }

            }
        }
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
