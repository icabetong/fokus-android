package com.isaiahvonrundstedt.fokus.features.schedule

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.*

@Parcelize
@Entity(tableName = "schedules", foreignKeys = [ForeignKey(entity = Subject::class,
    parentColumns = arrayOf("subjectID"), childColumns = arrayOf("subject"),
    onDelete = ForeignKey.CASCADE)])
data class Schedule @JvmOverloads constructor(
    @PrimaryKey
    var scheduleID: String = UUID.randomUUID().toString(),
    var daysOfWeek: Int = 0,
    @TypeConverters(DateTimeConverter::class)
    var startTime: LocalTime? = null,
    @TypeConverters(DateTimeConverter::class)
    var endTime: LocalTime? = null,
    var subject: String? = null
) : Parcelable {

    fun isToday(): Boolean {
        getDaysAsList().forEach {
            if (it == DateTime.now().dayOfWeek)
                return@isToday true
        }
        return false
    }

    fun format(context: Context): String {
        return StringBuilder().apply {
            append(formatDaysOfWeek(context))
            append(" ")
            append(formatBothTime())
        }.toString()
    }

    fun formatBothTime(): String {
        return "${formatStartTime()} - ${formatEndTime()}"
    }

    fun formatStartTime(): String {
        return formatTime(startTime)
    }

    fun formatEndTime(): String {
        return formatTime(endTime)
    }

    /**
     *   Function to format the daysOfWeek attribute
     *   to human readable form
     *   @param context used to fetch the appropriate string localization
     *                  for the string resource id
     *   @return the formatted days of week
     *           (e.g. "Sunday, Monday and Thursday")
     */
    fun formatDaysOfWeek(context: Context): String {
        val builder = StringBuilder()
        val list = getDaysAsList()
        list.forEachIndexed { index, i ->
            // Append the appropriate day name string from string resource
            builder.append(context.getString(getStringResourceForDay(i)))

            // Check if the item's index is second to last,
            // if it is, then add an "and" from string resource
            // and if not, just append a comma
            if (index == list.size - 2)
                builder.append(context.getString(R.string.and))
            else if (index < list.size - 2)
                builder.append(", ")
        }
        return builder.toString()
    }

    @StringRes
    fun getStringResourceForDay(day: Int): Int {
        return when (day) {
            DateTimeConstants.SUNDAY -> R.string.days_of_week_item_sunday
            DateTimeConstants.MONDAY -> R.string.days_of_week_item_monday
            DateTimeConstants.TUESDAY -> R.string.days_of_week_item_tuesday
            DateTimeConstants.WEDNESDAY -> R.string.days_of_week_item_wednesday
            DateTimeConstants.THURSDAY -> R.string.days_of_week_item_thursday
            DateTimeConstants.FRIDAY -> R.string.days_of_week_item_friday
            DateTimeConstants.SATURDAY -> R.string.days_of_week_item_saturday
            else -> 0
        }
    }

    fun getDaysAsList(): List<Int> {
        val days = mutableListOf<Int>()

        if (daysOfWeek and 1 == BIT_VALUE_SUNDAY) days.add(DateTimeConstants.SUNDAY)
        if (daysOfWeek and 2 == BIT_VALUE_MONDAY) days.add(DateTimeConstants.MONDAY)
        if (daysOfWeek and 4 == BIT_VALUE_TUESDAY) days.add(DateTimeConstants.TUESDAY)
        if (daysOfWeek and 8 == BIT_VALUE_WEDNESDAY) days.add(DateTimeConstants.WEDNESDAY)
        if (daysOfWeek and 16 == BIT_VALUE_THURSDAY) days.add(DateTimeConstants.THURSDAY)
        if (daysOfWeek and 32 == BIT_VALUE_FRIDAY) days.add(DateTimeConstants.FRIDAY)
        if (daysOfWeek and 64 == BIT_VALUE_SATURDAY) days.add(DateTimeConstants.SATURDAY)

        return days
    }

    companion object {
        const val BIT_VALUE_SUNDAY = 1
        const val BIT_VALUE_MONDAY = 2
        const val BIT_VALUE_TUESDAY = 4
        const val BIT_VALUE_WEDNESDAY = 8
        const val BIT_VALUE_THURSDAY = 16
        const val BIT_VALUE_FRIDAY = 32
        const val BIT_VALUE_SATURDAY = 64

        fun getNextWeekDay(day: Int, time: LocalTime?): DateTime {
            var currentDate = DateTime.now().withTimeAtStartOfDay()
                .plusHours(time?.hourOfDay ?: 0)
                .plusMinutes(time?.minuteOfHour ?: 0)
            if (currentDate.dayOfWeek >= day)
                currentDate = currentDate.plusWeeks(1)
            return currentDate.withDayOfWeek(day)
        }

        fun formatTime(time: LocalTime?): String {
            return DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(time)
        }
    }
}