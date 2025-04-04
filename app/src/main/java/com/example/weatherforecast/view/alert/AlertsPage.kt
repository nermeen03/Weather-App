package com.example.weatherforecast.view.alert

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.MyApplication
import com.example.weatherforecast.R
import com.example.weatherforecast.data.Response
import com.example.weatherforecast.data.local.alerts.AlertsDataBase
import com.example.weatherforecast.data.local.alerts.AlertsLocalDataSource
import com.example.weatherforecast.data.pojo.AlertsData
import com.example.weatherforecast.data.repo.AlertsRepository
import com.example.weatherforecast.view.utils.internet
import com.example.weatherforecast.viewModel.AlertsViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.absoluteValue

@Composable
fun AlertsScreen() {
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }

    val application = context.applicationContext as MyApplication
    val currentLocation = application.currentLocation.collectAsStateWithLifecycle()

    val lat = currentLocation.value.first
    val long = currentLocation.value.second

    val loc = application.currentLocationName.collectAsStateWithLifecycle().value
    val locArabic = application.currentLocationArabicName.collectAsStateWithLifecycle().value

    val viewModel: AlertsViewModel = viewModel(
        factory = AlertsViewModel.AlertsViewModelFactory(
            AlertsRepository.getRepository(
                AlertsLocalDataSource(AlertsDataBase.getInstance(context).getAlertsDao()))))

    viewModel.getAllAlerts()
    val alertsList by viewModel.alertsList.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (internet.value) {
                        showBottomSheet = true
                    } else {
                        coroutineScope.launch {
                            val result = snackBarHostState.showSnackbar(
                                message = context.getString(R.string.no_internet_connection),
                                actionLabel = context.getString(R.string.retry)
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                if (internet.value) {
                                    showBottomSheet = true
                                }
                            }
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_alert))
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
                    lat,long,loc,locArabic
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
        Text(color = Color.White, text = stringResource(R.string.you_don_t_have_alerts), fontSize = 20.sp)
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
    val application = context.applicationContext as MyApplication
    val date = application.convertDateToArabic(item.date)
    val textValue = if (application.getCurrentLanguage(context) == "en")
        item.location
    else
        item.arabicLocation

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
                        text = "${date}, ${application.convertTimeToArabic(item.time)}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (item.type) Icons.Default.Alarm else Icons.Default.Notifications,
                        contentDescription = if (item.type) stringResource(R.string.alarm) else stringResource(R.string.notification)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = textValue,
                    style = MaterialTheme.typography.bodyLarge
                )
            }


            Button(
                onClick = {
                    viewModel.deleteAlert(item,context,item.type)
                    when (viewModel.response.value) {
                        is Response.Success -> {
                            Toast.makeText(context, context.getString(R.string.removed_successfully), Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(context, context.getString(R.string.couldn_t_be_removed), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.secondaryLight),
                    contentColor = Color.White
                ),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(stringResource(R.string.remove))
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
    lat:Double,lon:Double,location: String,locArabic:String
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedAlertType by remember { mutableStateOf("Alarm") }
    var selectedMinutes by remember { mutableIntStateOf(0) }
    var selectedSeconds by remember { mutableIntStateOf(0) }
    val totalMillis = (selectedMinutes * 60 + selectedSeconds) * 1000L
    val application = context.applicationContext as MyApplication



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
                Text(stringResource(R.string.select_date_and_time), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { datePickerDialog.show() }) {
                    Text(if (selectedDate.isEmpty()) stringResource(R.string.select_date) else stringResource(R.string.date) + application.convertDateToArabic(selectedDate))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { timePickerDialog.show() }) {
                    Text(if (selectedTime.isEmpty()) stringResource(R.string.select_time) else stringResource(R.string.time) + selectedTime)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.choose_alert_type), fontSize = 18.sp, fontWeight = FontWeight.Medium)
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
                        Text(stringResource(R.string.alarm))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedAlertType = "Notification" }
                    ) {
                        RadioButton(
                            selected = selectedAlertType == "Notification",
                            onClick = { selectedAlertType = "Notification" }
                        )
                        Text(stringResource(R.string.notification))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DropdownSelector(
                        label = stringResource(R.string.minutes),
                        options = (0..10).toList(),
                        selectedOption = selectedMinutes,
                        onOptionSelected = { selectedMinutes = it }
                    )

                    DropdownSelector(
                        label = stringResource(R.string.seconds),
                        options = (0..59).toList(),
                        selectedOption = selectedSeconds,
                        onOptionSelected = { selectedSeconds = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.total_duration) + selectedMinutes + stringResource(R.string.min) + selectedSeconds + stringResource(R.string.sec))


                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                        if (calendar.timeInMillis > System.currentTimeMillis()) {
                            val alertData = AlertsData(selectedDate, selectedTime, location, lat, lon, selectedAlertType == "Alarm", locArabic)
                            onConfirm(calendar.timeInMillis, selectedAlertType == "Alarm", alertData, totalMillis)
                            onDismiss()
                        } else {
                            Toast.makeText(context,
                                context.getString(R.string.select_a_future_time), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context,
                            context.getString(R.string.select_date_and_time_first), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = stringResource(R.string.set_alert))
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
        putExtra("LocationArabic",data.arabicLocation)
    }

    val requestCode = getUniqueRequestCode(data)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (timeInMillis > System.currentTimeMillis()) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
        viewModel.insertAlert(data)
        Toast.makeText(context,
            context.getString(R.string.alarm_set_successfully), Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context,
            context.getString(R.string.cannot_set_alarm_in_the_past), Toast.LENGTH_SHORT).show()
    }
}

fun getUniqueRequestCode(data: AlertsData): Int {
    return (data.date.hashCode() + data.time.hashCode() + data.location.hashCode()).absoluteValue
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

