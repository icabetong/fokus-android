package com.isaiahvonrundstedt.fokus.components.utils

import com.isaiahvonrundstedt.fokus.components.json.DateTimeJSONAdapter
import com.isaiahvonrundstedt.fokus.components.json.LocalTimeJSONAdapter
import com.isaiahvonrundstedt.fokus.components.json.UriJSONAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.Okio
import java.io.InputStream

class JsonDataStreamer {

    companion object {
        val moshi: Moshi
            get() = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(DateTimeJSONAdapter())
                .add(LocalTimeJSONAdapter())
                .add(UriJSONAdapter())
                .build()

        fun <T> encodeToJson(data: T?, dataType: Class<T>): String? {
            if (data == null) return null

            val adapter: JsonAdapter<T> = moshi.adapter(dataType)
            return adapter.toJson(data)
        }

        fun <T> encodeToJson(dataItems: List<T>?, dataType: Class<T>): String? {
            if (dataItems == null || dataItems.isEmpty()) return null

            val type = Types.newParameterizedType(List::class.java, dataType)
            val adapter: JsonAdapter<List<T>> = moshi.adapter(type)
            return adapter.toJson(dataItems)
        }

        fun <T> decodeFromJson(stream: InputStream, dataType: Class<T>): List<T>? {
            if (stream.isEmpty()) return emptyList()

            val type = Types.newParameterizedType(List::class.java, dataType)
            val adapter: JsonAdapter<List<T>> = moshi.adapter(type)
            return adapter.fromJson(Okio.buffer(Okio.source(stream)))
        }

        private fun InputStream.isEmpty(): Boolean {
            return this.available() < 1
        }
    }
}