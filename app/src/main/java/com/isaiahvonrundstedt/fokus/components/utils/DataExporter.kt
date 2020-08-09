package com.isaiahvonrundstedt.fokus.components.utils

import android.content.Context
import android.net.Uri
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import okio.Okio
import java.io.File
import java.io.FileOutputStream

class DataExporter<T: Any> private constructor(private var context: Context) {

    private var destination: Uri? = null
    private var source: T? = null
    private var type: Class<T>? = null
    private var isCache: Boolean = false
    private var name: String = FILE_NAME_TEMP

    fun export(): File? {
        var cache: File? = null

        JsonDataStreamer.encodeToJson(source, type!!)?.toByteArray()?.run {
            if (isCache) {
                cache = File(context.cacheDir, name).also {
                    Okio.buffer(Okio.sink(it)).use { bufferedSink ->
                        bufferedSink.write(this)
                        bufferedSink.flush()
                    }
                }
            } else {
                context.contentResolver.openOutputStream(destination!!)?.use {
                    it.write(this)
                    it.flush()
                }
            }
        }
        return cache
    }

    class Builder<T: Any>(context: Context) {
        private var exporter = DataExporter<T>(context)

        fun writeAsCache(name: String = FILE_NAME_TEMP): Builder<T> {
            exporter.isCache = true
            exporter.destination = null
            exporter.name
            return this
        }

        @Suppress("UNCHECKED_CAST")
        fun fromSource(source: T): Builder<T> {
            exporter.source = source
            exporter.type = source::class.java as Class<T>
            return this
        }

        fun toDestination(destination: Uri): Builder<T> {
            exporter.isCache = false
            exporter.destination = destination
            return this
        }

        fun export(): File? = exporter.export()

    }

    companion object {
        const val FILE_NAME_TEMP = "TEMP"
    }
}