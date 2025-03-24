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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.weatherforecast.MyApplication
import java.util.Locale

@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    val language by application.language.collectAsState()
    val location by application.language.collectAsState()
    val temp by application.temp.collectAsState()
    val wind by application.wind.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Choose Location", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row {
            RadioButton(
                selected = location == "GPS",
                onClick = { application.setLocation("GPS") }
            )
            Text("Use GPS", modifier = Modifier.padding(start = 8.dp))

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = location == "Map",
                onClick = {
                    application.setLocation("Map")
                    navController.navigate("map_screen")
                }
            )
            Text("Select from Map", modifier = Modifier.padding(start = 8.dp))
        }

        Text(text = "Temperature Unit", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row {
            listOf("Kelvin", "Celsius", "Fahrenheit").forEach { unit ->
                Row(modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(
                        selected = temp == unit,
                        onClick = { application.setTemp(unit) }
                    )
                    Text(unit, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Text(text = "Wind Speed Unit", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row {
            listOf("m/s", "mph").forEach { unit ->
                Row(modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(
                        selected = wind == unit,
                        onClick = { application.setWind(unit) }
                    )
                    Text(unit, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Text(text = "Language", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row {
            listOf("English" to "en", "Arabic" to "ar").forEach { (langName, langCode) ->
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
