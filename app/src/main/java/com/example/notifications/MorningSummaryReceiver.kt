package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MorningSummaryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val appContext = context.applicationContext

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (MorningWeatherNotificationManager.isEnabled(appContext)) {
                    MorningWeatherNotificationManager.scheduleMorningSummary(appContext)
                }
            }
            MorningWeatherNotificationManager.ACTION_MORNING_SUMMARY -> {
                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        if (MorningWeatherNotificationManager.isEnabled(appContext)) {
                            MorningWeatherNotificationManager.showMorningSummaryNotification(appContext)
                            MorningWeatherNotificationManager.scheduleMorningSummary(appContext)
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
