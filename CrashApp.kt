package com.example.chargingscreen

import android.app.Application
import android.content.Intent
import android.util.Log
import kotlin.system.exitProcess

class CrashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                startActivity(
                    Intent(this, CrashActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("trace", Log.getStackTraceString(e))
                )
            } catch (_: Throwable) { }
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }
}
