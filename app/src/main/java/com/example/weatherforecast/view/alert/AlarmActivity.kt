package com.example.weatherforecast.view.alert

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.R
import kotlinx.coroutines.delay

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val message = intent.getStringExtra("MESSAGE") ?: getString(R.string.weather_alert_no_data_available)
        val time = intent.getLongExtra("DURATION", 30_000)
        val requestCode = intent.getIntExtra("REQUEST_CODE", 0)

        setContent {
            AlarmScreen(
                context = this,
                onDismiss = { finish() },
                onSnooze = { snoozeAlarm(intent, time, requestCode) },
                message = message,
                durationMillis = time
            )
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun snoozeAlarm(originalIntent: Intent, duration: Long, requestCode: Int) {
        val snoozeTimeMillis = System.currentTimeMillis() + 60 * 1000

        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtras(originalIntent.extras ?: Bundle())
            putExtra("IS_ALARM", true)
            putExtra("IS_SNOOZE", true)
            putExtra("MESSAGE", getString(R.string.snoozed_alarm_message))
            putExtra("DURATION", duration)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent)

        finish()
    }

    @Composable
    fun AlarmScreen(
        context: Context,
        onDismiss: () -> Unit,
        onSnooze: () -> Unit,
        message: String,
        durationMillis: Long
    ) {
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

        DisposableEffect(Unit) {
            val player = MediaPlayer.create(context, R.raw.alarm_sound).apply { isLooping = true }
            player.start()
            mediaPlayer = player

            onDispose {
                try {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                } catch (e: Exception) {
                    Log.e("TAG", "Error releasing MediaPlayer", e)
                }
            }
        }

        var isCancelled by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(durationMillis)
            if (!isCancelled) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                onDismiss()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorResource(id = R.color.primaryLight),
                            colorResource(id = R.color.secondaryLight)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.launch_icon),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    fontSize = 25.sp,
                    color = Color.White,
                    lineHeight = 28.sp,
                    modifier = Modifier.padding(25.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(onClick = {
                        isCancelled = true
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.dismiss))
                    }

                    Button(
                        onClick = {
                            try {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = null
                            } catch (e: Exception) {
                                Log.e("TAG", "Error releasing MediaPlayer", e)
                            }
                            onSnooze()
                        }
                    ) {
                        Text(stringResource(R.string.snooze))
                    }
                }
            }
        }
    }

}
