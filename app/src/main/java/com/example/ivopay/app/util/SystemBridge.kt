package com.example.ivopay.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.ivopay.app.data.api.ApiService
import com.google.gson.JsonObject

class SystemBridge(private val context: Context) {

    // Ganti dari: _getAppVersion()
    fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    // Ganti dari: _getAppVersionInt()
    fun getAppVersionInt(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            10000
        }
    }

    // Ganti dari: _getLocalLanguage()
    fun getLocalLanguage(): String {
        return context.resources.configuration.locales[0].language ?: "in"
    }

    // Ganti dari: _getCommonParams()
    fun getCommonParams(): Map<String, String> {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return mapOf(
            "a" to "ivoji", 
            "app_version" to getAppVersion(),
            "device_id" to deviceId,
            "platform" to "android",
            "os_version" to Build.VERSION.RELEASE
        )
    }

    fun getCommonParamsJson(): JsonObject {
        val params = getCommonParams()
        val json = JsonObject()
        params.forEach { (k, v) -> json.addProperty(k, v) }
        return json
    }

    // Ganti dari: _requestCameraPermission
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
