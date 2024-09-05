package com.example.minimaltimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.minimaltimer.ui.theme.MinimalTimerTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import android.content.res.Configuration
//import android.icu.util.TimeUnit
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    var time by remember {
        mutableStateOf(0L)
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    var startTime by remember {
        mutableStateOf(0L)
    }
    
    val context = LocalContext.current

    val keyboardController = LocalSoftwareKeyboardController.current

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        Text(
            text = formatTime(timeInMillis = time),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(9.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row {
            Button(onClick = {
                if (isRunning) {
                    isRunning = false
                } else {
                    startTime = System.currentTimeMillis() - time
                    isRunning = true
                    keyboardController?.hide()
                }
                }) {
                    Text(text = if (isRunning) "Pause" else "Start", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = {
                time = 0
                isRunning = false
            }) {
                Text(text = "Reset", color = Color.White)
            }
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning){
            delay(1000)
            time = System.currentTimeMillis()-startTime
        }

    }

}

@Composable
fun formatTime(timeInMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
