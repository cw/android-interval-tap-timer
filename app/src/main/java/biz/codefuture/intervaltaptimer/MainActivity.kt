package biz.codefuture.intervaltaptimer

import android.Manifest
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import biz.codefuture.intervaltaptimer.ui.theme.IntervalTapTimerTheme
import kotlinx.coroutines.*
import android.util.Log

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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    IntervalTimerScreen(
                        modifier = Modifier.padding(innerPadding),
                        onVibrate = { vibrate() }
                    )
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrationService.vibrate(
                VibrationEffect.createOneShot(1000,  255) // Maximum amplitude
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

    val maxIntervals = 20
    val intervalDuration = 30_000L

    // Launch a coroutine to update elapsed time every second
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Interval: $intervalCount / $maxIntervals",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Elapsed: ${formatElapsedTime(elapsedTimeMillis)}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        intervalCount = 0
                        startTimeMillis = System.currentTimeMillis()
                        elapsedTimeMillis = 0
                        job = CoroutineScope(Dispatchers.Main).launch {
                            while (intervalCount < maxIntervals && isRunning) {
                                delay(intervalDuration)
                                onVibrate()
                                intervalCount++
                            }
                            isRunning = false
                        }
                        onVibrate() // Vibrate on start
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
                    job?.cancel()
                    job = null
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
