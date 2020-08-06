package com.isaiahvonrundstedt.fokus

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid

class CoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }

}