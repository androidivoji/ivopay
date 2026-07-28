package com.example.ivopay.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.view.WindowManager

object DeviceHelper {
    fun setScreenshotPermission(activity: Activity, allow: Boolean) {
        activity.runOnUiThread {
            if (allow) {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    // Ganti dari: queryApplyParams(params)
    // Menghasilkan data kelayakan (fraud detection) untuk API pengajuan pinjaman
    fun getApplyParams(context: Context): Map<String, Any> {
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        return mapOf(
            "screen_width" to screenWidth,
            "screen_height" to screenHeight,
            "battery" to getBatteryLevel(context),
            "is_charging" to isDeviceCharging(context),
            "disk_space" to getTotalDiskSpace(),
            "free_space" to getFreeDiskSpace(),
//            "phone_moved" to SensorService.isPhoneMoved // Pertahankan sensor service lama jika diperlukan
        )
    }

    private fun getBatteryLevel(context: Context): String {
        val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt().toString()
        } else {
            "100"
        }
    }

    private fun isDeviceCharging(context: Context): Boolean {
        val batteryStatus: Intent? = context.registerReceiver(null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun getTotalDiskSpace(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        return stat.totalBytes
    }

    private fun getFreeDiskSpace(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        return stat.availableBytes
    }
}