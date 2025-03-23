package com.example.weatherforecast.view.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.R
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.local.alerts.AlertsDataBase
import com.example.weatherforecast.data.local.alerts.AlertsLocalDataSource
import com.example.weatherforecast.data.pojo.AlertsData
import com.example.weatherforecast.data.repo.AlertsRepository
import com.example.weatherforecast.view.utils.AppLocationHelper
import com.example.weatherforecast.viewModel.AlertsViewModel
import java.util.Calendar
import java.util.Date

@Composable
fun AlertsScreen() {
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }

    val currentLocation =  AppLocationHelper.locationState.observeAsState()

    val lat = currentLocation.value?.first ?: -1.0
    val long = currentLocation.value?.second ?: -1.0

    val loc = ""

    val viewModel: AlertsViewModel = viewModel(
        factory = AlertsViewModel.AlertsViewModelFactory(
            AlertsRepository.getRepository(
                AlertsLocalDataSource(AlertsDataBase.getInstance(context).getAlertsDao()))))

    viewModel.getAllAlerts()
    val alertsList by viewModel.alertsList.collectAsStateWithLifecycle()


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Go to Map")
            }
        }
    ) { paddingValues ->
        Column(
            Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
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
            )
            .padding(paddingValues)
        ) {
            if (alertsList.isEmpty()) {
                NoAlerts()
            } else {
                AlertsList(alertsList,viewModel)
            }
            if (showBottomSheet) {
                ChooseAlertDialog(
                    onDismiss = { showBottomSheet = false },
                    onConfirm = { time, isAlarm,data,duration ->
                        setExactAlarm(context, time, isAlarm,viewModel,data,duration)
                    },
                    lat,long,loc
                )
            }
        }
    }
}

@Composable
fun NoAlerts(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(color = Color.White, text = "You don't have alerts", fontSize = 20.sp)
    }
}

@Composable
fun AlertsList(list: List<AlertsData>,
               viewModel: AlertsViewModel, ) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list.size) { index ->
            AlertRow(list[index], viewModel)
        }
    }
}


@Composable
fun AlertRow(item: AlertsData, viewModel: AlertsViewModel) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.date}, ${item.time}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (item.type) Icons.Default.Alarm else Icons.Default.Notifications,
                        contentDescription = if (item.type) "Alarm" else "Notification"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.location,
                    style = MaterialTheme.typography.bodyLarge
                )
            }


            Button(
                onClick = {
                    viewModel.deleteAlert(item.date,item.time,item.location)
                    when (viewModel.response.value) {
                        is Response.Success -> {
                            Toast.makeText(context, "Removed successfully", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(context, "Couldn't be removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.secondaryLight),
                    contentColor = Color.White
                ),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text("Remove")
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, Boolean,AlertsData,Long) -> Unit,
    lat:Double,lon:Double,location: String
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedAlertType by remember { mutableStateOf("Alarm") }
    var selectedMinutes by remember { mutableIntStateOf(0) }
    var selectedSeconds by remember { mutableIntStateOf(0) }
    val totalMillis = (selectedMinutes * 60 + selectedSeconds) * 1000L


    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Date and Time", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { datePickerDialog.show() }) {
                    Text(if (selectedDate.isEmpty()) "Select Date" else "Date: $selectedDate")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { timePickerDialog.show() }) {
                    Text(if (selectedTime.isEmpty()) "Select Time" else "Time: $selectedTime")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Choose Alert Type", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedAlertType = "Alarm" }
                    ) {
                        RadioButton(
                            selected = selectedAlertType == "Alarm",
                            onClick = { selectedAlertType = "Alarm" }
                        )
                        Text("Alarm")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedAlertType = "Notification" }
                    ) {
                        RadioButton(
                            selected = selectedAlertType == "Notification",
                            onClick = { selectedAlertType = "Notification" }
                        )
                        Text("Notification")
                    }
                }
                if (selectedAlertType == "Alarm") {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DropdownSelector(
                            label = "Minutes",
                            options = (0..10).toList(),
                            selectedOption = selectedMinutes,
                            onOptionSelected = { selectedMinutes = it }
                        )

                        DropdownSelector(
                            label = "Seconds",
                            options = (0..59).toList(),
                            selectedOption = selectedSeconds,
                            onOptionSelected = { selectedSeconds = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Total Duration: $selectedMinutes min $selectedSeconds sec ($totalMillis ms)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                        if (calendar.timeInMillis > System.currentTimeMillis()) {
                            val alertData = AlertsData(selectedDate,selectedTime,location,lat,lon,selectedAlertType == "Alarm")
                            onConfirm(calendar.timeInMillis, selectedAlertType == "Alarm",alertData,totalMillis)
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Select a future time!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Select date and time first!", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = "Set Alert")
                }
            }
        }
    )
}



@SuppressLint("ScheduleExactAlarm")
fun setExactAlarm(context: Context, timeInMillis: Long, isAlarm: Boolean,viewModel: AlertsViewModel,data: AlertsData,duration:Long) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("DATE", data.date)
        putExtra("TIME", data.time)
        putExtra("LOC", data.location)
        putExtra("LAT", data.lat)
        putExtra("LONG", data.lon)
        putExtra("IS_ALARM", isAlarm)
        putExtra("DURATION", duration)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        System.currentTimeMillis().toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (timeInMillis > System.currentTimeMillis()) {
        Log.d("Alarm", "Setting alarm for: ${Date(timeInMillis)}")
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
        viewModel.insertAlert(data)
        Toast.makeText(context, "Alarm set successfully!", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Cannot set alarm in the past!", Toast.LENGTH_SHORT).show()
    }
}
@Composable
fun DropdownSelector(label: String, options: List<Int>, selectedOption: Int, onOptionSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text("$label: $selectedOption")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

