package com.zapposandroid.rafaep.androidvillekotlin

import android.app.Application

class MainApp : Application() {
    var storedMainActivity: MainActivity? = null

    override fun onCreate() {
        super.onCreate()
    }
}
