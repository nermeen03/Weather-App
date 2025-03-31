package com.example.weatherforecast.viewModel

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.pojo.AlertsData
import com.example.weatherforecast.data.repo.IAlertsRepository
import com.example.weatherforecast.view.alert.AlarmReceiver
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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
            val result = alertsRepository.insertAlert(alert)
            if(result>=0){ mutableResponse.value = Response.Success }
            else{ mutableResponse.value = Response.Failure(Exception("an error occurred")) }
        }
    }
    fun deleteAlert(data:AlertsData, context: Context, isAlarm: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("DATE", data.date)
            putExtra("TIME", data.time)
            putExtra("LOC", data.location)
            putExtra("LAT", data.lat)
            putExtra("LONG", data.lon)
            putExtra("IS_ALARM", isAlarm)
        }

        val requestCode = getUniqueRequestCode(data)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (isAlarm) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } else {
            Handler(Looper.getMainLooper()).post {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(requestCode)
            }

        }

        viewModelScope.launch(Dispatchers.IO) {
            val result = alertsRepository.delete(data.date, data.time, data.location)
            mutableResponse.value = if (result > 0) Response.Success else Response.Failure(Exception("An error occurred"))
        }
    }

    private fun getUniqueRequestCode(data: AlertsData): Int {
        return (data.date.hashCode() + data.time.hashCode() + data.location.hashCode()).absoluteValue
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