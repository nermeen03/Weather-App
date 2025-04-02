package com.example.weatherforecast.view.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.weatherforecast.MainActivity
import com.example.weatherforecast.MyApplication
import com.example.weatherforecast.R
import com.example.weatherforecast.view.navigation.ScreenRoute

@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    val location by application.location.collectAsState()
    val temp by application.temp.collectAsState()
    val wind by application.wind.collectAsState()
    val language by application.language.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.choose_location),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = location == "GPS",
                    onClick = { application.setLocation("GPS") }
                )
                Text(
                    text = stringResource(R.string.use_gps),
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 16.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = location == "Map",
                    onClick = {
                        application.setLocation("Map")
                        navController.navigate(ScreenRoute.MapScreenRoute.route) {
                            launchSingleTop = true
                        }
                    }
                )
                Text(
                    text = stringResource(R.string.select_from_map),
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.temperature_unit),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                stringResource(R.string.kelvin) to "°K",
                stringResource(R.string.celsius) to "°C",
                stringResource(R.string.fahrenheit) to "°F"
            ).forEach { (name, unit) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = temp == unit,
                        onClick = { application.setTemp(unit) }
                    )
                    Text(
                        text = name,
                        modifier = Modifier.padding(start = 1.dp),
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.wind_speed_unit),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                stringResource(R.string.m_s) to "mps",
                stringResource(R.string.mph) to "mph"
            ).forEach { (name, unit) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = wind == unit,
                        onClick = { application.setWind(unit) }
                    )
                    Text(
                        text = name,
                        modifier = Modifier.padding(start = 4.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.language),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                stringResource(R.string.system_default) to "def",
                stringResource(R.string.english) to "en",
                stringResource(R.string.arabic) to "ar"
            ).forEach { (langName, langCode) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = language == langCode,
                        onClick = {
                            application.setLanguage(context, langCode)
                            application.reStarted = true
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    )
                    Text(
                        text = langName,
                        modifier = Modifier.padding(start = 4.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }

    }

}
