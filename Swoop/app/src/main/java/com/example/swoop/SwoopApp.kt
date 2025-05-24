package com.example.swoop

import android.app.Application
import android.util.Log
import com.example.swoop.ui.utils.PreferencesHelper

class SwoopApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, ex ->
            if (PreferencesHelper.isDeveloperMode(this)) {
                Log.e("Uncaught", Log.getStackTraceString(ex))
            }
        }
    }
}