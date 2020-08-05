package com.isaiahvonrundstedt.fokus

import android.app.Application
import com.isaiahvonrundstedt.fokus.components.json.DateTimeJSONAdapter
import com.isaiahvonrundstedt.fokus.components.json.LocalTimeJSONAdapter
import com.isaiahvonrundstedt.fokus.components.json.UriJSONAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.danlew.android.joda.JodaTimeAndroid

class CoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }

    companion object {

        val moshi: Moshi
            get() = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(DateTimeJSONAdapter())
                .add(LocalTimeJSONAdapter())
                .add(UriJSONAdapter())
                .build()
    }

}