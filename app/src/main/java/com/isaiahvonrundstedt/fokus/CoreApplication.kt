package com.isaiahvonrundstedt.fokus

import android.app.Application
import androidx.appcompat.widget.AppCompatTextView
import net.danlew.android.joda.JodaTimeAndroid

class CoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }
}