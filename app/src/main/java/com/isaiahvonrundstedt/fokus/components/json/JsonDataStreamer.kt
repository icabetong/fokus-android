package com.isaiahvonrundstedt.fokus.components.json

import android.net.Uri
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.buffer
import okio.source
import java.io.InputStream
import java.time.LocalTime
import java.time.ZonedDateTime

class JsonDataStreamer private constructor() {

    class DateTimeAdapter {

        @FromJson
        fun toDateTime(string: String): ZonedDateTime? = DateTimeConverter.toZonedDateTime(string)

        @ToJson
        fun fromDateTime(dateTime: ZonedDateTime): String? =
            DateTimeConverter.fromZonedDateTime(dateTime)
    }

    class LocalTimeAdapter {

        @FromJson
        fun toLocalTime(string: String): LocalTime? = DateTimeConverter.toLocalTime(string)

        @ToJson
        fun fromLocalTime(time: LocalTime): String? = DateTimeConverter.fromLocalTime(time)
    }

    class UriAdapter {

        @FromJson
        fun toUri(data: String): Uri = Uri.parse(data)

        @ToJson
        fun fromUri(uri: Uri): String = uri.toString()
    }

    companion object {
        val moshi: Moshi
            get() = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(DateTimeAdapter())
                .add(LocalTimeAdapter())
                .add(UriAdapter())
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

        fun <T> decodeOnceFromJson(stream: InputStream, dataType: Class<T>): T? {
            if (stream.isEmpty()) return null

            val adapter: JsonAdapter<T> = moshi.adapter(dataType)
            return adapter.fromJson(stream.source().buffer())
        }

        fun <T> decodeFromJson(stream: InputStream, dataType: Class<T>): List<T>? {
            if (stream.isEmpty()) return emptyList()

            val type = Types.newParameterizedType(List::class.java, dataType)
            val adapter: JsonAdapter<List<T>> = moshi.adapter(type)
            return adapter.fromJson(stream.source().buffer())
        }

        private fun InputStream.isEmpty(): Boolean {
            return this.available() < 1
        }
    }
}