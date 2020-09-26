package com.isaiahvonrundstedt.fokus.database.converter

import androidx.room.TypeConverter
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.print
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DateTimeConverter private constructor() {

    companion object {
        const val FORMAT_TIME = "h:mm a"
        const val FORMAT_DATE = "MMMM d, h:mm a"

        @JvmStatic
        @TypeConverter
        fun toZonedDateTime(time: String?): ZonedDateTime? {
            return if (time.isNullOrEmpty()) null
                else ZonedDateTime.parse(time)
        }

        @JvmStatic
        @TypeConverter
        fun fromZonedDateTime(zonedDateTime: ZonedDateTime?): String? {
            return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(zonedDateTime)
        }

        @JvmStatic
        @TypeConverter
        fun toLocalTime(time: String?): LocalTime? {
            return if (time.isNullOrEmpty()) null
                else LocalTime.parse(time)
        }

        @JvmStatic
        @TypeConverter
        fun fromLocalTime(time: LocalTime?): String? {
            return DateTimeFormatter.ISO_LOCAL_TIME.format(time)
        }
    }

}