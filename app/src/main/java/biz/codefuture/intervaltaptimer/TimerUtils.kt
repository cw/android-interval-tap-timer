package biz.codefuture.intervaltaptimer.util

object TimerUtils {

    /** Format a time duration in milliseconds to mm:ss */
    fun formatElapsedTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    const val MAX_INTERVALS = 20
    const val INTERVAL_DURATION_MS = 30_000L // 30 seconds
}