package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class UpdateCheckResult(
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

object AppUpdateManager {
    private const val DEFAULT_GITHUB_REPO = "wsmorgan7/sheep-weather"
    private const val FALLBACK_DIRECT_APK_URL = "https://github.com/wsmorgan7/sheep-weather/releases/latest/download/app-release.apk"
    private const val FALLBACK_RELEASES_WEB_URL = "https://github.com/wsmorgan7/sheep-weather/releases/latest"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun getCurrentVersion(): String {
        return try {
            BuildConfig.VERSION_NAME
        } catch (_: Throwable) {
            "2.6.5"
        }
    }

    suspend fun checkForUpdates(customRepo: String? = null): UpdateCheckResult = withContext(Dispatchers.IO) {
        val currentVer = getCurrentVersion()
        val repo = if (!customRepo.isNullOrBlank()) customRepo.trim() else DEFAULT_GITHUB_REPO
        val apiUrl = "https://api.github.com/repos/$repo/releases/latest"

        try {
            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "SheepWeather-Android")
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()

            if (response.isSuccessful && !body.isNullOrBlank()) {
                val json = JSONObject(body)
                val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                val releaseName = json.optString("name", "Version $tagName")
                val releaseBody = json.optString("body", "Bug fixes and performance improvements.")

                // Look for .apk asset
                var apkUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                }

                if (apkUrl.isBlank()) {
                    apkUrl = json.optString("html_url", FALLBACK_RELEASES_WEB_URL)
                }

                val latestVer = if (tagName.isNotBlank()) tagName else currentVer
                val isAvailable = compareVersions(latestVer, currentVer) > 0

                return@withContext UpdateCheckResult(
                    currentVersion = currentVer,
                    latestVersion = latestVer,
                    releaseTitle = releaseName,
                    releaseNotes = releaseBody.take(400),
                    downloadUrl = apkUrl,
                    isUpdateAvailable = isAvailable
                )
            } else {
                // Return structured fallback
                return@withContext UpdateCheckResult(
                    currentVersion = currentVer,
                    latestVersion = currentVer,
                    releaseTitle = "Sheep Weather v$currentVer (Latest)",
                    releaseNotes = "You are running the latest version from this build repository.",
                    downloadUrl = "https://github.com/$repo/releases",
                    isUpdateAvailable = false
                )
            }
        } catch (e: Exception) {
            return@withContext UpdateCheckResult(
                currentVersion = currentVer,
                latestVersion = currentVer,
                releaseTitle = "Sheep Weather v$currentVer",
                releaseNotes = "Could not reach update server. You can download the latest APK directly below.",
                downloadUrl = FALLBACK_DIRECT_APK_URL,
                isUpdateAvailable = false,
                isError = true,
                errorMessage = e.localizedMessage
            )
        }
    }

    /**
     * Downloads and initiates APK installation.
     * Uses DownloadManager when available, with browser intent fallback.
     */
    fun startApkDownload(context: Context, downloadUrl: String? = null) {
        val targetUrl = if (!downloadUrl.isNullOrBlank()) downloadUrl else FALLBACK_DIRECT_APK_URL

        try {
            val uri = Uri.parse(targetUrl)

            // If it's a direct APK link, try system DownloadManager
            if (targetUrl.endsWith(".apk", ignoreCase = true)) {
                val request = DownloadManager.Request(uri).apply {
                    setTitle("Sheep Weather Update")
                    setDescription("Downloading latest Sheep Weather APK...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SheepWeather-latest.apk")
                    setMimeType("application/vnd.android.package-archive")
                }

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                if (dm != null) {
                    dm.enqueue(request)
                    Toast.makeText(context, "Downloading APK to Downloads folder...", Toast.LENGTH_LONG).show()
                    return
                }
            }

            // Browser intent fallback
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(context, "Opening APK download...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(FALLBACK_RELEASES_WEB_URL)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Failed to start download: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.trim().toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.trim().toIntOrNull() }
        val length = maxOf(parts1.size, parts2.size)
        for (i in 0 until length) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}
