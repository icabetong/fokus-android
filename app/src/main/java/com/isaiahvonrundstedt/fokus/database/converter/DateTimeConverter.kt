package com.isaiahvonrundstedt.fokus.database.converter

import android.content.Context
import android.text.format.DateFormat
import androidx.room.TypeConverter
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DateTimeConverter private constructor() {

    companion object {
        private const val FORMAT_TIME_12_HOUR = "h:mm a"
        private const val FORMAT_TIME_24_HOUR = "H:mm"
        private const val FORMAT_DATE_TIME_12_HOUR = "MMMM d, h:mm a"
        private const val FORMAT_DATE_TIME_24_HOUR = "MMMM d, H:mm"
        private const val FORMAT_DATE_TIME_WITH_YEAR_12_HOUR = "MM d yyyy, h:mm a"
        private const val FORMAT_DATE_TIME_WITH_YEAR_24_HOUR = "Mm d yyyy, H:mm"

        fun getTimeFormatter(context: Context): DateTimeFormatter {
            val pattern = if (DateFormat.is24HourFormat(context))
                FORMAT_TIME_24_HOUR
            else FORMAT_TIME_12_HOUR

            return DateTimeFormatter.ofPattern(pattern)
        }

        fun getDateTimeFormatter(context: Context, withYear: Boolean = false): DateTimeFormatter {
            val pattern = if (DateFormat.is24HourFormat(context)) {
                if (withYear)
                    FORMAT_DATE_TIME_WITH_YEAR_24_HOUR
                else FORMAT_DATE_TIME_24_HOUR
            } else {
                if (withYear)
                    FORMAT_DATE_TIME_WITH_YEAR_12_HOUR
                else FORMAT_DATE_TIME_12_HOUR
            }

            return DateTimeFormatter.ofPattern(pattern)
        }

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