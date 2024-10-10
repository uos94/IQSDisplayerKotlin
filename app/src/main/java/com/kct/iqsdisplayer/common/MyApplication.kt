package com.kct.iqsdisplayer.common

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
    }
}