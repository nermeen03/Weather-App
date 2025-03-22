package com.example.weatherforecast.view.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.weatherforecast.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            Toast.makeText(it, "Alarm Triggered!", Toast.LENGTH_SHORT).show()
            showNotification(it)
        }
    }


    private fun showNotification(context: Context) {
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)

        val builder = NotificationCompat.Builder(context, "alert_channel")
            .setSmallIcon(R.drawable.launch_icon)
            .setContentTitle("Alarm Alert")
            .setContentText("Your alarm is ringing!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager?.notify(1002, builder.build())
    }
}
