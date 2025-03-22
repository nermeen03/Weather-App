package com.example.weatherforecast.view.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavHostController
import com.example.weatherforecast.R
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.local.alerts.AlertsDataBase
import com.example.weatherforecast.data.local.alerts.AlertsLocalDataSource
import com.example.weatherforecast.data.pojo.AlertsData
import com.example.weatherforecast.data.repo.AlertsRepository
import com.example.weatherforecast.view.navigation.ScreenRoute
import com.example.weatherforecast.view.utils.checkExactAlarmPermission
import com.example.weatherforecast.view.utils.requestExactAlarmPermission
import com.example.weatherforecast.viewModel.AlertsViewModel
import java.util.Calendar

@Composable
fun AlertsScreen(navController: NavHostController) {
    val context = LocalContext.current

    val viewModel: AlertsViewModel = viewModel(
        factory = AlertsViewModel.AlertsViewModelFactory(
            AlertsRepository.getRepository(
                AlertsLocalDataSource(AlertsDataBase.getInstance(context).getAlertsDao()))))

    LaunchedEffect(Unit) {
        viewModel.getAllAlerts()
    }

    val alertsList by viewModel.alertsList.collectAsStateWithLifecycle()


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(ScreenRoute.ChoosingScreenRoute.route) {
                        launchSingleTop = true
                    }
                }
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
                Text(
                    text = "${item.start}, ${item.end}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lat: ${item.location}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = {
                    viewModel.deleteAlert(item.start,item.end,item.location)
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
@Composable
fun ChooseAlert() {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedAlertType by remember { mutableStateOf("Alarm") }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (selectedAlertType == "Alarm") {
                    scheduleAlarm(context, selectedDate, selectedTime)
                } else {
                    scheduleNotification(
                        context, selectedDate, selectedTime)
                }
            }) {
                Icon(Icons.Default.Check, contentDescription = "Confirm Alert")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select Date and Time", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showDatePicker = true }) {
                Text(if (selectedDate.isEmpty()) "Select Date" else "Date: $selectedDate")
            }
            if (showDatePicker) datePickerDialog.show()

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showTimePicker = true }) {
                Text(if (selectedTime.isEmpty()) "Select Time" else "Time: $selectedTime")
            }
            if (showTimePicker) timePickerDialog.show()

            Spacer(modifier = Modifier.height(16.dp))

            Text("Choose Alert Type", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Row {
                RadioButton(
                    selected = selectedAlertType == "Alarm",
                    onClick = { selectedAlertType = "Alarm" }
                )
                Text("Alarm", modifier = Modifier.clickable { selectedAlertType = "Alarm" })

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = selectedAlertType == "Notification",
                    onClick = { selectedAlertType = "Notification" }
                )
                Text("Notification", modifier = Modifier.clickable { selectedAlertType = "Notification" })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AlarmSchedulerScreen() {
    val context = LocalContext.current

    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var calendar = Calendar.getInstance()

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            DatePickerDialog(context, { _, year, month, day ->
                calendar.set(year, month, day)
                date = "$day/${month + 1}/$year"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }) {
            Text(text = date.ifEmpty { "Pick Date" })
        }

        Button(onClick = {
            TimePickerDialog(context, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                time = "$hour:$minute"
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }) {
            Text(text = time.ifEmpty { "Pick Time" })
        }

        Button(onClick = {
            if (checkExactAlarmPermission(context)) {
                setExactAlarm(context, calendar.timeInMillis)
                Toast.makeText(context, "Alarm Set!", Toast.LENGTH_SHORT).show()
            } else {
                requestExactAlarmPermission(context)
            }
        }) {
            Text(text = "Set Alarm")
        }
    }
}


@SuppressLint("ScheduleExactAlarm")
fun scheduleAlarm(context: Context, date: String, time: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        val (day, month, year) = date.split("/").map { it.toInt() }
        val (hour, minute) = time.split(":").map { it.toInt() }
        set(year, month - 1, day, hour, minute, 0)
    }

    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    Toast.makeText(context, "Alarm set for $date at $time", Toast.LENGTH_SHORT).show()
}
@SuppressLint("ScheduleExactAlarm")
fun scheduleNotification(context: Context, date: String, time: String) {
    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        val (day, month, year) = date.split("/").map { it.toInt() }
        val (hour, minute) = time.split(":").map { it.toInt() }
        set(year, month - 1, day, hour, minute, 0)
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

    Toast.makeText(context, "Notification scheduled for $date at $time", Toast.LENGTH_SHORT).show()
}

@SuppressLint("ScheduleExactAlarm")
fun setExactAlarm(context: Context, timeInMillis: Long) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val intent = Intent(context, AlarmReceiver::class.java).let { intent ->
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, intent)
}
