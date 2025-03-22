package com.example.weatherforecast.view.utils

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
fun checkExactAlarmPermission(context: Context): Boolean {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    return alarmManager.canScheduleExactAlarms()
}

fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

