package com.isaiahvonrundstedt.fokus

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.content.FileProvider
import net.danlew.android.joda.JodaTimeAndroid
import java.io.File

class CoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }

    companion object {
        const val PROVIDER_AUTHORITY = "com.isaiahvonrundstedt.fokus.provider"

        fun obtainUriForFile(context: Context, source: File): Uri {
            return FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, source)
        }
    }

}