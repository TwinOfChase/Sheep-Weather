package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object AppIconManager {
    private const val DEFAULT_ALIAS = "com.example.MainLauncherAlias"
    private const val CHRISTMAS_ALIAS = "com.example.ChristmasLauncherAlias"

    fun setChristmasIconEnabled(context: Context, enabled: Boolean): Boolean {
        return try {
            val pm = context.packageManager
            val defaultComponent = ComponentName(context, DEFAULT_ALIAS)
            val christmasComponent = ComponentName(context, CHRISTMAS_ALIAS)

            if (enabled) {
                pm.setComponentEnabledSetting(
                    christmasComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    defaultComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            } else {
                pm.setComponentEnabledSetting(
                    defaultComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    christmasComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isChristmasIconEnabled(context: Context): Boolean {
        return try {
            val state = context.packageManager.getComponentEnabledSetting(
                ComponentName(context, CHRISTMAS_ALIAS)
            )
            state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } catch (_: Exception) {
            false
        }
    }
}
