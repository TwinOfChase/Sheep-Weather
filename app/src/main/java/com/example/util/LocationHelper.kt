package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import java.util.function.Consumer
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        // Check if location services are enabled
        val isGpsEnabled = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) { false }

        val isNetworkEnabled = try {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) { false }

        if (!isGpsEnabled && !isNetworkEnabled) {
            return null
        }

        // Try getting last known location first (immediate, zero battery / zero network)
        val lastGps = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) { null }

        val lastNetwork = try {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) { null }

        val bestLast = when {
            lastGps != null && lastNetwork != null -> {
                if (lastGps.time > lastNetwork.time) lastGps else lastNetwork
            }
            lastGps != null -> lastGps
            else -> lastNetwork
        }

        // If last known location is reasonably fresh (within last 2 hours), use it directly
        if (bestLast != null && (System.currentTimeMillis() - bestLast.time < 2 * 60 * 60 * 1000L)) {
            return bestLast
        }

        // If on Android 30+, try one-shot getCurrentLocation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val provider = if (isNetworkEnabled) LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER
                val loc = suspendCancellableCoroutine<Location?> { cont ->
                    val signal = CancellationSignal()
                    cont.invokeOnCancellation { signal.cancel() }
                    try {
                        locationManager.getCurrentLocation(
                            provider,
                            signal,
                            context.mainExecutor,
                            Consumer { location ->
                                if (cont.isActive) cont.resume(location)
                            }
                        )
                    } catch (e: Exception) {
                        if (cont.isActive) cont.resume(bestLast)
                    }
                }
                if (loc != null) return loc
            } catch (_: Exception) {}
        }

        return bestLast
    }
}
