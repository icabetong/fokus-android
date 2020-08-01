package com.isaiahvonrundstedt.fokus.components.json

import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.time.LocalTime

class LocalTimeJSONAdapter {

    @FromJson
    fun toLocalTime(string: String): LocalTime? {
        return DateTimeConverter.toTime(string)
    }

    @ToJson
    fun fromLocalTime(time: LocalTime): String? {
        return DateTimeConverter.fromTime(time)
    }

}