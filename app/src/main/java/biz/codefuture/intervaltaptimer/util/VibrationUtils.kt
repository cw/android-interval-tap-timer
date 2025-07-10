package biz.codefuture.intervaltaptimer.util

import android.Manifest
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresPermission

object VibrationUtils {

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(vibrator: Vibrator, durationMs: Long = 1000L) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
}
