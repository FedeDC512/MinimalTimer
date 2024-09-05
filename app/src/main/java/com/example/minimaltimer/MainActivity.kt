package com.example.minimaltimer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.minimaltimer.ui.theme.MinimalTimerTheme
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinimalTimerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Timer()
                }
            }
        }
    }
}

@Composable
fun Timer(){
    var timeInput by remember {
        mutableStateOf(0L)
    }

    var minutesInput by remember {
        mutableStateOf("")
    }

    var secondsInput by remember {
        mutableStateOf("")
    }

    var count by remember {
        mutableStateOf(0)
    }

    var time by remember {
        mutableStateOf(timeInput)
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    var startTime by remember {
        mutableStateOf(0L)
    }

    var isDialogOpen by remember { mutableStateOf(false) }

    fun showDialog() {
        isDialogOpen = true
    }

    fun hideDialog() {
        isDialogOpen = false
    }
    
    val context = LocalContext.current

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable {
            count++
        },
    ){
        Text(text = count.toString())
        Column (
            modifier = Modifier
                .align(Alignment.Center)
                .padding(15.dp)
        ) {
            Row {
                Spacer(modifier = Modifier.weight(1f))
                Box (modifier = Modifier
                    .background(Color.Yellow)
                    .clickable { showDialog() },
                ) {
                    Text(
                        text = formatTime(timeInMillis = time),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row {
                Spacer(modifier = Modifier.weight(1f))

                Button(onClick = {
                    if (isRunning) {
                        time = 0L
                        isRunning = false
                    } else if (time != 0L) {
                        startTime = System.currentTimeMillis() - time
                        isRunning = true
                        keyboardController?.hide()
                    }
                    }) {
                        Text(text = if (isRunning) "Reset" else "Start", color = MaterialTheme.colorScheme.surface)
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning){
            delay(1000)
            time -= 1000
        }

    }

    // Finestra di dialogo
    if (isDialogOpen) {
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
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hideDialog()
                        isRunning = false
                        time = parseTimeToMillis(minutesInput, secondsInput)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { hideDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Composable
fun formatTime(timeInMillis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60

    return String.format("%02d:%02d", minutes, seconds)
}

fun parseTimeToMillis(minutes: String, seconds: String): Long {
    val minutesValue = minutes.toIntOrNull() ?: 0
    val secondsValue = seconds.toIntOrNull() ?: 0
    return TimeUnit.MINUTES.toMillis(minutesValue.toLong()) + TimeUnit.SECONDS.toMillis(secondsValue.toLong())
}
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun GreetingPreview() {
    MinimalTimerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Timer()
        }
    }
}