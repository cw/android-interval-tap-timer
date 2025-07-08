package biz.codefuture.intervaltaptimer

import android.Manifest
import android.os.*
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import biz.codefuture.intervaltaptimer.ui.theme.IntervalTapTimerTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val vibrationService by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            IntervalTapTimerTheme {
                // IntervalTimerScreen will now control its own background for flashing
                IntervalTimerScreen(
                    modifier = Modifier.fillMaxSize(), // Apply fillMaxSize here
                    onVibrate = { vibrate() }
                )
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrationService.vibrate(
                VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
            )
            Log.d(TAG, "vibrate: one shot")
        } else {
            @Suppress("DEPRECATION")
            vibrationService.vibrate(1000)
            Log.d(TAG, "vibrate: straight call")
        }
    }
}

@Composable
fun IntervalTimerScreen(
    modifier: Modifier = Modifier,
    onVibrate: () -> Unit
) {
    var intervalCount by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    var elapsedTimeMillis by remember { mutableLongStateOf(0L) }
    var startTimeMillis by remember { mutableLongStateOf(0L) }

    // State to control the flash effect
    var flashActive by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val maxIntervals = 20
    val intervalDuration = 30_000L // 30 seconds

    // Launch a coroutine to update elapsed time every second
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isActive) { // Use isActive to ensure coroutine stops when scope is cancelled
                delay(1000)
                if (isRunning) { // Check isRunning again before updating time
                    elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
                }
            }
        }
    }

    val isDarkTheme = isSystemInDarkTheme()
    val defaultBackgroundColor = MaterialTheme.colorScheme.background
    val flashColor = if (isDarkTheme) Color.White else Color.Black

    val currentBackgroundColor = if (flashActive) flashColor else defaultBackgroundColor

    // Function to trigger the flash
    fun triggerFlash() {
        coroutineScope.launch {
            flashActive = true
            delay(200) // Duration of the flash
            flashActive = false
        }
    }

    Column(
        modifier = modifier
            .background(currentBackgroundColor) // Apply background color here
            .padding(32.dp), // Padding inside the background
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Interval: $intervalCount / $maxIntervals",
            style = MaterialTheme.typography.headlineMedium,
            color = if (flashActive) defaultBackgroundColor else MaterialTheme.colorScheme.onBackground // Adjust text color during flash
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Elapsed: ${formatElapsedTime(elapsedTimeMillis)}",
            style = MaterialTheme.typography.bodyLarge,
            color = if (flashActive) defaultBackgroundColor else MaterialTheme.colorScheme.onBackground // Adjust text color
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        intervalCount = 1
                        startTimeMillis = System.currentTimeMillis()
                        elapsedTimeMillis = 0
                        job = CoroutineScope(Dispatchers.Main).launch {
                            // Initial flash and vibration on start
                            onVibrate()
                            triggerFlash()
                            while (intervalCount < maxIntervals && isRunning && isActive) {
                                delay(intervalDuration)
                                if (isRunning) { // Check if still running before vibrating and incrementing
                                    onVibrate()
                                    triggerFlash()
                                    intervalCount++
                                }
                            }
                            isRunning =
                                false // Ensure isRunning is set to false when loop finishes or is cancelled
                        }
                    }
                    Log.d("IntervalTimerScreen", "Start clicked. isRunning = $isRunning")
                },
                enabled = !isRunning
            ) {
                Text("Start")
            }

            Button(
                onClick = {
                    isRunning = false
                    job?.cancel() // Cancel the coroutine
                    job = null
                    flashActive = false // Reset flash state on stop
                    Log.d("IntervalTimerScreen", "Stop clicked. isRunning = $isRunning")
                },
                enabled = isRunning
            ) {
                Text("Stop")
            }
        }
    }
}

// Utility function to format time in mm:ss
@Composable
fun formatElapsedTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}