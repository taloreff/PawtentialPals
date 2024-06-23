package com.example.pawtentialpals

import android.app.Application
import com.google.android.libraries.places.api.Places

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the Places SDK
        Places.initialize(applicationContext, "AIzaSyDZHE0jtrWArab41ZbQN5YPTJqYeJC-jrU")
    }
}
