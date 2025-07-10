package biz.codefuture.intervaltaptimer

import android.os.Bundle
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import biz.codefuture.intervaltaptimer.ui.IntervalTimerScreen
import biz.codefuture.intervaltaptimer.ui.theme.IntervalTapTimerTheme
import biz.codefuture.intervaltaptimer.util.VibrationUtils

class MainActivity : ComponentActivity() {

    private val vibrator by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            IntervalTapTimerTheme {
                IntervalTimerScreen(
                    onVibrate = { VibrationUtils.vibrate(vibrator) }
                )
            }
        }
    }
}
