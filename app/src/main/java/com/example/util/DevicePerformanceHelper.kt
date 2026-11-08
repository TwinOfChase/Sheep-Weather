package com.example.util

import android.app.ActivityManager
import android.content.Context

object DevicePerformanceHelper {
    private var isLowEndCached: Boolean? = null

    /**
     * Identifies whether the current device is a memory- or processor-constrained device
     * that would benefit from throttled background canvas redraws and reduced graphics overhead.
     */
    fun isLowEndDevice(context: Context): Boolean {
        isLowEndCached?.let { return it }

        val isLowEnd = try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)

            val isLowRam = actManager?.isLowRamDevice ?: false
            val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val cores = Runtime.getRuntime().availableProcessors()

            // Thresholds: isLowRamDevice flag, RAM <= 3.2 GB, or <= 4 CPU cores
            isLowRam || totalRamGb <= 3.2 || cores <= 4
        } catch (_: Exception) {
            false
        }

        isLowEndCached = isLowEnd
        return isLowEnd
    }
}
