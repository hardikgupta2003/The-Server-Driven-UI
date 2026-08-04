package com.hardik.the_server_driven_ui.perf

import android.util.Log

/**
 * Minimal timing instrumentation for PERF.md. Deliberately not a full
 * Macrobenchmark module — that needs its own Gradle module and a
 * connected device to set up; these markers plus Android's own
 * automatic "Displayed"/"Fully drawn" logcat lines get honest,
 * reproducible numbers with a single `adb logcat` command instead.
 *
 * Filter with: adb logcat -s PERF_TRACE:D ActivityTaskManager:I
 */
object PerfTrace {
    private const val TAG = "PERF_TRACE"

    fun mark(label: String) {
        Log.d(TAG, "$label t=${System.nanoTime() / 1_000_000}ms")
    }

    fun duration(label: String, startNanos: Long) {
        val ms = (System.nanoTime() - startNanos) / 1_000_000.0
        Log.d(TAG, "$label durationMs=$ms")
    }
}
