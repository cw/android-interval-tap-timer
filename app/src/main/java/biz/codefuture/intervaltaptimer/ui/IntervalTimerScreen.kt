package biz.codefuture.intervaltaptimer.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import biz.codefuture.intervaltaptimer.util.TimerUtils
import kotlinx.coroutines.*

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
    var flashActive by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val maxIntervals = TimerUtils.MAX_INTERVALS
    val intervalDuration = TimerUtils.INTERVAL_DURATION_MS

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isActive) {
                delay(1000)
                if (isRunning) {
                    elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
                }
            }
        }
    }

    val isDarkTheme = isSystemInDarkTheme()
    val defaultBackgroundColor = MaterialTheme.colorScheme.background
    val flashColor = if (isDarkTheme) Color.White else Color.Black
    val currentBackgroundColor = if (flashActive) flashColor else defaultBackgroundColor

    fun triggerFlash() {
        coroutineScope.launch {
            flashActive = true
            delay(200)
            flashActive = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(currentBackgroundColor)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Interval: $intervalCount / $maxIntervals",
            style = MaterialTheme.typography.headlineMedium,
            color = if (flashActive) defaultBackgroundColor else MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Elapsed: ${TimerUtils.formatElapsedTime(elapsedTimeMillis)}",
            style = MaterialTheme.typography.bodyLarge,
            color = if (flashActive) defaultBackgroundColor else MaterialTheme.colorScheme.onBackground
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
                            onVibrate()
                            triggerFlash()
                            while (intervalCount < maxIntervals && isRunning && isActive) {
                                delay(intervalDuration)
                                if (isRunning) {
                                    onVibrate()
                                    triggerFlash()
                                    intervalCount++
                                }
                            }
                            isRunning = false
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
                    job?.cancel()
                    job = null
                    flashActive = false
                    Log.d("IntervalTimerScreen", "Stop clicked. isRunning = $isRunning")
                },
                enabled = isRunning
            ) {
                Text("Stop")
            }
        }
    }
}
