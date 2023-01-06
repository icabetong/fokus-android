package com.isaiahvonrundstedt.fokus

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class Fokus : Application(), Configuration.Provider {

    private var engine: FlutterEngine? = null

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        engine = FlutterEngine(this)

        engine?.also {
            it.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
        }
        FlutterEngineCache.getInstance().put("core", engine)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    companion object {
        fun obtainUriForFile(context: Context, source: File): Uri {
            return FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.provider", source
            )
        }
    }

}