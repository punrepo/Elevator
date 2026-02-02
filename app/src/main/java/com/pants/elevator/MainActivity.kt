package com.pants.elevator

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ElevatorUI()
            }
        }
    }
}

@Composable
fun ElevatorUI() {
    val context = LocalContext.current
    var currentFloor by remember { mutableIntStateOf(1) }
    var targetFloor by remember { mutableIntStateOf(1) }
    var isHeld by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    // Initialize Firebase
    val database = FirebaseDatabase.getInstance().getReference("elevator_logs")

    fun logRequest(floor: Int) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = mapOf(
            "floor" to floor,
            "time" to timestamp,
            "device" to "Samsung_Galaxy"
        )
        database.push().setValue(logEntry)
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    val speechRecognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            speechRecognizer.startListening(speechRecognizerIntent)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { isListening = false }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val command = matches[0].lowercase()
                    parseFloorCommand(command)?.let { floor ->
                        targetFloor = floor
                        logRequest(floor)
                    }
                }
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose { speechRecognizer.destroy() }
    }

    val isMoving = currentFloor != targetFloor && !isHeld

    // Simulation logic to move the elevator
    LaunchedEffect(targetFloor, isHeld) {
        while (currentFloor != targetFloor && !isHeld) {
            delay(1000) // Wait 1 second per floor
            if (!isHeld) {
                if (currentFloor < targetFloor) currentFloor++ else currentFloor--
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Skyline Elevators", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(40.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isHeld -> Color(0xFFE57373)
                    isMoving -> Color(0xFFFFB74D)
                    else -> Color(0xFF81C784)
                }
            )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "$currentFloor", fontSize = 80.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when {
                isListening -> "Listening..."
                isHeld -> "HOLD ACTIVE"
                isMoving -> "Moving to Floor $targetFloor..."
                else -> "Waiting for Request"
            },
            color = if (isListening) Color.Blue else if (isHeld) Color.Red else Color.Unspecified,
            fontWeight = if (isHeld || isListening) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isHeld = !isHeld },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHeld) Color.Red else Color.DarkGray
                )
            ) {
                Text(if (isHeld) "RELEASE DOOR" else "HOLD DOOR", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.width(56.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color.Blue else MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Command", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Single Floor Grid with Logging
        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
            items(12) { i ->
                val floor = i + 1
                Button(
                    onClick = {
                        targetFloor = floor
                        logRequest(floor)
                    },
                    modifier = Modifier.padding(4.dp).height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(targetFloor == floor) Color.DarkGray else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("$floor")
                }
            }
        }
    }
}

fun parseFloorCommand(text: String): Int? {
    val floorMap = mapOf(
        "one" to 1, "first" to 1, "1" to 1,
        "two" to 2, "second" to 2, "2" to 2,
        "three" to 3, "third" to 3, "3" to 3,
        "four" to 4, "fourth" to 4, "4" to 4,
        "five" to 5, "fifth" to 5, "5" to 5,
        "six" to 6, "sixth" to 6, "6" to 6,
        "seven" to 7, "seventh" to 7, "7" to 7,
        "eight" to 8, "eighth" to 8, "8" to 8,
        "nine" to 9, "ninth" to 9, "9" to 9,
        "ten" to 10, "tenth" to 10, "10" to 10,
        "eleven" to 11, "eleventh" to 11, "11" to 11,
        "twelve" to 12, "twelfth" to 12, "12" to 12
    )

    for ((word, floor) in floorMap) {
        if (text.contains(word)) return floor
    }
    return null
}
