package com.example.minimaltimer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.minimaltimer.ui.theme.MinimalTimerTheme
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                /* Directs the user to the app settings to enable permission
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)*/
            }
        }

    private val requestExactAlarmPermissionLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            /*if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Exact alarm permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Exact alarm permission denied", Toast.LENGTH_SHORT).show()
            }*/
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            val darkThemeState = remember { mutableStateOf(isDarkTheme) }

            MinimalTimerTheme(darkTheme = darkThemeState.value) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Timer(darkThemeState)
                }
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                requestExactAlarmPermissionLauncher.launch(intent)
            }
        }
    }
}

@Composable
fun Timer(darkThemeState: MutableState<Boolean>){
    var minutesInput by remember { mutableStateOf("") }
    var secondsInput by remember { mutableStateOf("") }
    val focusRequesterMinutes = remember { FocusRequester() }
    val focusRequesterSeconds = remember { FocusRequester() }
    var minutesClicked by remember { mutableStateOf(false) }

    var startTime by remember { mutableStateOf(0L) }
    var time by remember { mutableStateOf(startTime) }


    var isRunning by remember { mutableStateOf(false) }
    var isTimerDialogOpen by remember { mutableStateOf(false) }
    var isSettingsDialogOpen by remember { mutableStateOf(false) }

    fun showDialog() { isTimerDialogOpen = true }
    fun hideDialog() { isTimerDialogOpen = false }
    fun showSettingsDialog() { isSettingsDialogOpen = true }
    fun hideSettingsDialog() { isSettingsDialogOpen = false }

    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                if (isRunning) {
                    time = startTime
                    isRunning = false
                    cancelAlarm(context)
                    removeNotification(context)
                } else if (time != 0L) {
                    isRunning = true
                    keyboardController?.hide()
                    setAlarm(context, time)
                }
            },
    ){
        Column (
            modifier = Modifier
                .align(Alignment.Center)
                .padding(15.dp)
        ) {
            Row {
                Spacer(modifier = Modifier.weight(1f))
                Box (modifier = Modifier
                    //.background(Color.Yellow)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val boxWidth = size.width
                            if (offset.x < boxWidth / 2) {
                                minutesClicked = true
                                showDialog()
                            } else {
                                minutesClicked = false
                                showDialog()
                            }
                        }
                    }
                ) {
                    Text(
                        text = formatTime(timeInMillis = time),
                        //style = MaterialTheme.typography.headlineLarge,
                        fontSize = 90.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(100.dp))

            Row {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (time == 0L && !isRunning) "Tap the clock\nto set your timer"
                    else if (isRunning) "Tap anywhere\nto reset the timer"
                    else "Tap anywhere\nto start the timer",
                    /* buildAnnotatedString {
                        if (time == 0L) {
                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                append("Click the clock to\n")
                            }
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append("set the timer")
                            }
                        } else {
                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                append("Click anywhere to\n")
                            }
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append( if (isRunning) "reset" else "start")
                            }
                        }
                    }, */

                    color = Color.LightGray,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        lineHeight = 35.sp
                    ),

                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                modifier = Modifier
                    .size(45.dp)
                    .align(Alignment.TopEnd)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            showSettingsDialog()
                        }
                    },
                tint = Color.LightGray
            )
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning){
            delay(1000)
            time -= 1000
            updateCountdownNotification(context, time)

            if(time == 0L) //setAlarm(context) //sendNotification(context)
            if(time < -3_600_000){ //If the timer is running for more than an hour, it resets to 0
                isRunning = false
                time = startTime
            }
        }

    }

    if (isTimerDialogOpen) {
        minutesInput = ""
        secondsInput = ""

        AlertDialog(
            onDismissRequest = { hideDialog() },
            title = { Text("Enter Time") },
            text = {
                Row {
                    OutlinedTextField(
                        value = minutesInput,
                        onValueChange = { newValue ->
                            minutesInput = newValue
                        },
                        label = { Text("mm") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(80.dp)
                            .padding(8.dp)
                            .focusRequester(focusRequesterMinutes)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = secondsInput,
                        onValueChange = { newValue ->
                            secondsInput = newValue
                        },
                        label = { Text("ss") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(80.dp)
                            .padding(8.dp)
                            .focusRequester(focusRequesterSeconds)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hideDialog()
                        isRunning = false
                        startTime = 0
                        time = parseTimeToMillis(minutesInput, secondsInput)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        hideDialog()
                        if (time == 0L) { isRunning = false }
                    }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(isTimerDialogOpen) {
        if (isTimerDialogOpen) {
            if (minutesClicked) {
                focusRequesterMinutes.requestFocus()
                keyboardController?.show()
            } else {
                focusRequesterSeconds.requestFocus()
                keyboardController?.show()
            }
        }
    }

    if (isSettingsDialogOpen) {
        minutesInput = ""
        secondsInput = ""

        AlertDialog(
            onDismissRequest = { isSettingsDialogOpen = false },
            title = { Text("Settings") },
            text = {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "Custom Time",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Set a personalized time, and the timer will automatically reset to this value when restarted.\nCurrent custom time: ${formatTime(startTime)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row {
                        OutlinedTextField(
                            value = minutesInput,
                            onValueChange = { newValue ->
                                minutesInput = newValue
                            },
                            label = { Text("mm") },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(80.dp)
                                .padding(4.dp)
                                .focusRequester(focusRequesterMinutes)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = secondsInput,
                            onValueChange = { newValue ->
                                secondsInput = newValue
                            },
                            label = { Text("ss") },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(80.dp)
                                .padding(4.dp)
                                .focusRequester(focusRequesterSeconds)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            startTime = parseTimeToMillis(minutesInput, secondsInput)
                            time = startTime
                        },
                    ) {
                        Text(
                            text = "Set Custom Time",
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Switch(
                            checked = darkThemeState.value,
                            onCheckedChange = { isChecked ->
                                darkThemeState.value = isChecked
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Light / Dark",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hideSettingsDialog()
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

}

fun createNotificationChannel(context: Context) {
    val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build()

    val sound = Uri.parse("android.resource://${context.packageName}/raw/${R.raw.notification_sound}")


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Timer Channel"
        val descriptionText = "Channel for Timer Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("TIMER_CHANNEL_ID", name, importance).apply {
            description = descriptionText
            setSound((sound),audioAttributes)
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@SuppressLint("MissingPermission")
fun updateCountdownNotification(context: Context, timeRemaining: Long) {
    val timeFormatted = formatTime(timeRemaining)

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP // Does not recreate the activity if it is already running
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE
    )

    val notificationBuilder = NotificationCompat.Builder(context, "TIMER_CHANNEL_ID")
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle("Timer")
        .setContentText("Remaining time: $timeFormatted")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setContentIntent(pendingIntent)
        .setOngoing(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    with(NotificationManagerCompat.from(context)) {
        notify(1, notificationBuilder.build())
    }
}

fun removeNotification(context: Context) {
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancel(1)
}

@SuppressLint("DefaultLocale")
fun formatTime(timeInMillis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60

    var formattedMinutes = String.format("%02d", minutes)
    var formattedSeconds = String.format("%02d", seconds)
    if (seconds > -10 && seconds < 0) formattedSeconds = "0$formattedSeconds"
    if (minutes > -10 && minutes < 0) formattedMinutes = "0$formattedMinutes"

    val formattedTime = "$formattedMinutes:$formattedSeconds"

    return if (formattedTime.contains("-")) {
        val cleanedTime = formattedTime.replace("-", "")
        "-$cleanedTime"
    } else {
        formattedTime
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Your timer has finished!", Toast.LENGTH_SHORT).show()

        val soundUri = Uri.parse("android.resource://${context.packageName}/raw/${R.raw.notification_sound}")

        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            setDataSource(context, soundUri)
            prepare()
            start()
        }

        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }
}

fun setAlarm(context: Context, timeInMillis: Long) {
    cancelAlarm(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val triggerTime = System.currentTimeMillis() + timeInMillis

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            Toast.makeText(context, "Permission to set exact alarms is required", Toast.LENGTH_SHORT).show()
        }
    } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }
}

fun cancelAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    alarmManager.cancel(pendingIntent)
}


fun parseTimeToMillis(minutes: String, seconds: String): Long {
    val minutesValue = minutes.toIntOrNull() ?: 0
    val secondsValue = seconds.toIntOrNull() ?: 0
    return TimeUnit.MINUTES.toMillis(minutesValue.toLong()) + TimeUnit.SECONDS.toMillis(secondsValue.toLong())
}

@SuppressLint("UnrememberedMutableState")
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun GreetingPreview() {
    MinimalTimerTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Timer(mutableStateOf(true))
        }
    }
}