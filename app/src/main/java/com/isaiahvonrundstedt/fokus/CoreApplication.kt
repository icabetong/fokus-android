package com.isaiahvonrundstedt.fokus

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid
import java.util.*

class CoreApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }

    companion object {
        fun generateUID(): Long {
            return UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
        }
    }
}