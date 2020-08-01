package com.isaiahvonrundstedt.fokus.components.json

import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.time.DateTime

class DateTimeJSONAdapter {

    @FromJson
    fun toDateTime(string: String): DateTime? {
        return DateTimeConverter.toDateTime(string)
    }

    @ToJson
    fun fromDateTime(dateTime: DateTime): String? {
        return DateTimeConverter.fromDateTime(dateTime)
    }

}