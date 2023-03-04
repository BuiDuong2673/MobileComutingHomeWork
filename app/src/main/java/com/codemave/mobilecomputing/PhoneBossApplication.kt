package com.codemave.mobilecomputing

import android.app.Application

class PhoneBossApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}