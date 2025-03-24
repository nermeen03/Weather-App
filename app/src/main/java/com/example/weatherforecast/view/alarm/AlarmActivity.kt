package com.example.weatherforecast.view.alarm

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
        val time = intent.getLongExtra("DURATION",30_000)
        Log.i("TAG", "onCreate: duration is $time")
        setContent {
            AlarmScreen(
                context = this,
                onDismiss = { finish() },
                message = message,
                durationMillis = time
            )
        }
    }


    @Composable
    fun AlarmScreen(context: Context, onDismiss: () -> Unit, message: String, durationMillis: Long) {
        val mediaPlayer = remember { MediaPlayer.create(context, R.raw.alarm_sound).apply { isLooping = true } }

        LaunchedEffect(mediaPlayer) {
            mediaPlayer?.start()
        }

        LaunchedEffect(Unit) {
            delay(durationMillis)
            mediaPlayer?.stop()
            mediaPlayer?.release()
            onDismiss()
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isSystemInDarkTheme()) {
                            listOf(
                                colorResource(id = R.color.primaryDark),
                                colorResource(id = R.color.secondaryDark)
                            )
                        } else {
                            listOf(
                                colorResource(id = R.color.primaryLight),
                                colorResource(id = R.color.secondaryLight)
                            )
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        )
        {
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

                Button(
                    onClick = {
                        mediaPlayer.stop()
                        mediaPlayer.release()
                        onDismiss()
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        }
    }
}

