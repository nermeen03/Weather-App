package com.example.weatherforecast.view.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.weatherforecast.MyApplication
import com.example.weatherforecast.R
import com.example.weatherforecast.view.navigation.ScreenRoute
import java.util.Locale

@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    val location by application.location.collectAsState()
    val temp by application.temp.collectAsState()
    val wind by application.wind.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.choose_location), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row {
            RadioButton(
                selected = location == "GPS",
                onClick = { application.setLocation("GPS") }
            )
            Text(stringResource(R.string.use_gps), modifier = Modifier.padding(start = 8.dp))

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = location == "Map",
                onClick = {
                    application.setLocation("Map")
                    navController.navigate(ScreenRoute.MapScreenRoute.route) {
                        launchSingleTop = true
                    }
                }
            )
            Text(stringResource(R.string.select_from_map), modifier = Modifier.padding(start = 8.dp))
        }

        Text(text = stringResource(R.string.temperature_unit), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row {
            listOf(stringResource(R.string.kelvin) to "K", stringResource(R.string.celsius) to "C",
                stringResource(R.string.fahrenheit) to "F").forEach { (name,unit) ->
                Row(modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(
                        selected = temp == unit,
                        onClick = { application.setTemp(unit) }
                    )
                    Text(name, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Text(text = stringResource(R.string.wind_speed_unit), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row {
            listOf(stringResource(R.string.m_s) to "mps", stringResource(R.string.mph) to "mph").forEach { (name,unit) ->
                Row(modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(
                        selected = wind == unit,
                        onClick = { application.setWind(unit) }
                    )
                    Text(name, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Text(text = stringResource(R.string.language), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row {
            listOf(stringResource(R.string.english) to "en", stringResource(R.string.arabic) to "ar").forEach { (langName, langCode) ->
                Row(modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(
                        selected = Locale.getDefault().language == langCode,
                        onClick = {
                            application.setLanguage(context, langCode)
                            (context as? Activity)?.recreate()
                        }
                    )
                    Text(langName, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
