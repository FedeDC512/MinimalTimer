package com.example.minimaltimer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var minutesInput by remember { mutableStateOf("") }
    var secondsInput by remember { mutableStateOf("") }
    val focusRequesterMinutes = remember { FocusRequester() }
    val focusRequesterSeconds = remember { FocusRequester() }
    var minutesClicked by remember { mutableStateOf(false) }

    var time by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }

    fun showDialog() { isDialogOpen = true }
    fun hideDialog() { isDialogOpen = false }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable {
            if (isRunning) {
                time = 0L
                isRunning = false
            } else if (time != 0L) {
                isRunning = true
                keyboardController?.hide()
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
    }

    LaunchedEffect(isRunning) {
        while (isRunning){
            delay(1000)
            time -= 1000
        }

    }

    // Finestra di dialogo
    if (isDialogOpen) {

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

    LaunchedEffect(isDialogOpen) {
        if (isDialogOpen) {
            if (minutesClicked) {
                focusRequesterMinutes.requestFocus()
                keyboardController?.show()
            } else {
                focusRequesterSeconds.requestFocus()
                keyboardController?.show()
            }
        }
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