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
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            )
            Log.d(TAG, "vibrate: one shot")
        } else {
            @Suppress("DEPRECATION")
            vibrationService.vibrate(200)
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

    val maxIntervals = 20
    val intervalDuration = 30_000L

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

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        intervalCount = 0
                        job = CoroutineScope(Dispatchers.Main).launch {
                            while (intervalCount < maxIntervals && isRunning) {
                                delay(intervalDuration)
                                onVibrate()
                                intervalCount++
                            }
                            isRunning = false
                        }
                    }
                    Log.d("IntervalTimerScreen", "IntervalTimerScreen: start clicked $isRunning")
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
                    Log.d("IntervalTimerScreen", "IntervalTimerScreen: stop clicked $isRunning")
                },
                enabled = isRunning
            ) {
                Text("Stop")
            }
        }
    }
}