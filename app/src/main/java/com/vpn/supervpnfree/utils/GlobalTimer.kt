package com.vpn.supervpnfree.utils

import android.os.Handler
import android.os.Looper

class GlobalTimer {

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0
    private var isRunning = false
    private var elapsedTime: Long = 0

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val currentTime = System.currentTimeMillis()
                elapsedTime = currentTime - startTime
                val formattedTime = formatTime(elapsedTime)
                // 这里可以更新UI或其他操作
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            handler.post(runnable)
            isRunning = true
        }
    }

    fun stop() {
        if (isRunning) {
            handler.removeCallbacks(runnable)
            isRunning = false
        }
    }

    fun reset() {
        stop()
        elapsedTime = 0
    }

    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return String.format("%02d:%02d:%02d", hours % 24, minutes % 60, seconds % 60)
    }

    fun getFormattedTime(): String {
        return formatTime(elapsedTime)
    }
}
