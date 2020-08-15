package com.isaiahvonrundstedt.fokus

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.PowerManager
import net.danlew.android.joda.JodaTimeAndroid

class CoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }

}