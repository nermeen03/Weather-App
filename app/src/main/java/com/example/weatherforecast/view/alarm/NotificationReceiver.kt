package com.example.weatherforecast.view.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.weatherforecast.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            showNotification(it)
        }
    }

    private fun showNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, "alert_channel")
            .setSmallIcon(R.drawable.launch_icon)
            .setContentTitle("Scheduled Notification")
            .setContentText("Your reminder is here!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(1003, builder.build())
    }
}
