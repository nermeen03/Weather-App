package com.example.weatherforecast.view.alarm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.weatherforecast.R
import com.example.weatherforecast.data.local.alerts.AlertsDataBase
import com.example.weatherforecast.data.local.alerts.AlertsLocalDataSource
import com.example.weatherforecast.data.remote.DailyRemoteDataSource
import com.example.weatherforecast.data.repo.AlertsRepository
import com.example.weatherforecast.data.repo.DailyDataRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver", "BatteryLife")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val isAlarm = intent?.getBooleanExtra("IS_ALARM", false) ?: false
        val date = intent?.getStringExtra("DATE") ?: ""
        val time = intent?.getStringExtra("TIME") ?: ""
        val loc = intent?.getStringExtra("LOC") ?: ""
        val lat = intent?.getDoubleExtra("LAT", 0.0) ?: 0.0
        val lon = intent?.getDoubleExtra("LON", 0.0) ?: 0.0
        val duration = intent?.getLongExtra("DURATION", 30_000) ?: 30_000

        val alertsDao = AlertsDataBase.getInstance(context).getAlertsDao()
        val repository = AlertsRepository.getRepository(AlertsLocalDataSource(alertsDao))

        val dataRepository = DailyDataRepository(DailyRemoteDataSource())

        var message: String

        val handle = CoroutineExceptionHandler { _, exception ->
            Log.e("TAG", "onReceive: $exception")
            message = context.getString(R.string.problem_in_the_api)
        }

        CoroutineScope(Dispatchers.Main + handle).launch {
            try {
                message = withContext(Dispatchers.IO) {
                    repository.delete(date, time, loc)

                    val result = try {
                        dataRepository.getDailyData(lat, lon).firstOrNull()
                    } catch (e: Exception) {
                        Log.e("TAG", "Weather API request failed: $e")
                        null
                    }

                    if (result != null) {
                        val temp = result.list[0].main.temp
                        val feel = result.list[0].main.feels_like
                        val cloud = result.list[0].clouds.all
                        context.getString(R.string.the_temperature_at)+ loc +
                                context.getString(R.string.`is`)+temp+
                                context.getString(R.string.c)+ context.getString(R.string.but_it_feels_like)+
                                feel+context.getString(R.string.c)+ context.getString(R.string.cloud_coverage)+
                                cloud+context.getString(R.string.percent)
                    } else {
                        context.getString(R.string.problem_in_the_api)
                    }
                }

            } catch (e: Exception) {
                message = context.getString(R.string.error_in_alarm)
            }

            if (message.isNotEmpty()) {
                if (isAlarm) {

                    if (!Settings.canDrawOverlays(context)) {
                        val overlayIntent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.applicationContext.startActivity(overlayIntent)
                    } else {
                        val alarmIntent = Intent(context.applicationContext, AlarmActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra("MESSAGE", message)
                            putExtra("DURATION", duration)
                        }
                        context.applicationContext.startActivity(alarmIntent)
                    }

                }
                else {
                    showNotification(context, message)
                }
            }
        }



    }

    private fun showNotification(context: Context, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.current_weather))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
