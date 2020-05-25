package com.isaiahvonrundstedt.fokus.features.shared.components.converter

import androidx.room.TypeConverter
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

class DateTimeConverter {

    companion object {
        @JvmStatic
        @TypeConverter
        fun toDateTime(time: String?): DateTime? {
            return DateTime.parse(time)
        }

        @JvmStatic
        @TypeConverter
        fun fromDateTime(dateTime: DateTime?): String? {
            return dateTime.toString()
        }

        @JvmStatic
        @TypeConverter
        fun fromTime(time: LocalTime?): String? {
            return DateTimeFormat.forPattern("HH:mm:ss").print(time)
        }

        @JvmStatic
        @TypeConverter
        fun toTime(time: String?): LocalTime? {
            return LocalTime.parse(time)
        }

        const val timeFormat = "h:mm a"
    }

}