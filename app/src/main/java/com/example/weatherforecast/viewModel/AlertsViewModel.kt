package com.example.weatherforecast.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.pojo.AlertsData
import com.example.weatherforecast.data.repo.IAlertsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch

class AlertsViewModel(private val alertsRepository: IAlertsRepository):ViewModel() {
    private val mutableResponse = MutableStateFlow(Response.Loading as Response<*>)
    val response = mutableResponse.asStateFlow()

    private val handle = CoroutineExceptionHandler { _, exception ->
        mutableResponse.value = Response.Failure(exception)
    }

    private val mutableAlertsList = MutableStateFlow<List<AlertsData>>(emptyList())
    val alertsList =mutableAlertsList.asStateFlow()

    fun insertAlert(alert: AlertsData){
        viewModelScope.launch(Dispatchers.IO+handle) {
            Log.i("TAG", "insertAlert: ${alert.time}, ${alert.date}")
            val result = alertsRepository.insertAlert(alert)
            if(result>=0){ mutableResponse.value = Response.Success }
            else{ mutableResponse.value = Response.Failure(Exception("an error occurred")) }
        }
    }
    fun deleteAlert(start:String,end:String,location:String){
        viewModelScope.launch(Dispatchers.IO+handle){
            val result = alertsRepository.delete(start,end, location)
            if(result>0){ mutableResponse.value = Response.Success }
            else{ mutableResponse.value = Response.Failure(Exception("an error occurred")) }
        }
    }
    fun getAllAlerts(){
        viewModelScope.launch(Dispatchers.IO+handle){
            alertsRepository.getAllAlerts().distinctUntilChanged().retry(3)
                .catch { e -> mutableResponse.value = Response.Failure(e) }
                .collect {
                    mutableAlertsList.value = it
                    mutableResponse.value = Response.Success
                }
        }
    }

    class AlertsViewModelFactory(private val alertsRepository: IAlertsRepository):
        ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlertsViewModel(alertsRepository) as T
        }
    }
}