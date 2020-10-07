package com.isaiahvonrundstedt.fokus.features.schedule

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.print
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import okio.Okio
import java.io.File
import java.io.InputStream
import java.time.*
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
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
) : Parcelable, Streamable {

    fun isToday(): Boolean {
        val currentDate = LocalDate.now()

        getDaysAsList().forEach {
            if (it == currentDate.dayOfWeek.value)
                return@isToday true
        }
        return false
    }

    fun isTomorrow(): Boolean {
        val currentDate = LocalDate.now()

        getDaysAsList().forEach {
            if (it == currentDate.plusDays(1).dayOfWeek.value)
                return@isTomorrow true
        }
        return false
    }

    fun format(context: Context, isAbbreviated: Boolean = false): String {
        return StringBuilder().apply {
            append(formatDaysOfWeek(context, isAbbreviated))
            append(", ")
            append(formatBothTime())
        }.toString()
    }

    fun formatBothTime(): String {
        return "${formatStartTime()} - ${formatEndTime()}"
    }

    fun formatStartTime(): String? {
        return formatTime(startTime)
    }

    fun formatEndTime(): String? {
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
    fun formatDaysOfWeek(context: Context, isAbbreviated: Boolean): String {
        val builder = StringBuilder()
        val list = getDaysAsList()
        list.forEachIndexed { index, i ->
            // Append the appropriate day name string from string resource
            val resID = if (isAbbreviated)
                getStringResourceForDayAbbreviated(i)
            else getStringResourceForDay(i)

            builder.append(context.getString(resID))

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
    fun getStringResourceForDayAbbreviated(day: Int): Int {
        return when (day) {
            DayOfWeek.SUNDAY.value -> R.string.days_of_week_item_sunday_short
            DayOfWeek.MONDAY.value -> R.string.days_of_week_item_monday_short
            DayOfWeek.TUESDAY.value -> R.string.days_of_week_item_tuesday_short
            DayOfWeek.WEDNESDAY.value -> R.string.days_of_week_item_wednesday_short
            DayOfWeek.THURSDAY.value -> R.string.days_of_week_item_thursday_short
            DayOfWeek.FRIDAY.value -> R.string.days_of_week_item_friday_short
            DayOfWeek.SATURDAY.value -> R.string.days_of_week_item_saturday_short
            else -> 0
        }
    }

    @StringRes
    fun getStringResourceForDay(day: Int): Int {
        return when (day) {
            DayOfWeek.SUNDAY.value -> R.string.days_of_week_item_sunday
            DayOfWeek.MONDAY.value -> R.string.days_of_week_item_monday
            DayOfWeek.TUESDAY.value -> R.string.days_of_week_item_tuesday
            DayOfWeek.WEDNESDAY.value -> R.string.days_of_week_item_wednesday
            DayOfWeek.THURSDAY.value -> R.string.days_of_week_item_thursday
            DayOfWeek.FRIDAY.value -> R.string.days_of_week_item_friday
            DayOfWeek.SATURDAY.value -> R.string.days_of_week_item_saturday
            else -> 0
        }
    }

    fun getDaysAsList(): List<Int> {
        val days = mutableListOf<Int>()

        if (daysOfWeek and 1 == BIT_VALUE_SUNDAY) days.add(DayOfWeek.SUNDAY.value)
        if (daysOfWeek and 2 == BIT_VALUE_MONDAY) days.add(DayOfWeek.MONDAY.value)
        if (daysOfWeek and 4 == BIT_VALUE_TUESDAY) days.add(DayOfWeek.TUESDAY.value)
        if (daysOfWeek and 8 == BIT_VALUE_WEDNESDAY) days.add(DayOfWeek.WEDNESDAY.value)
        if (daysOfWeek and 16 == BIT_VALUE_THURSDAY) days.add(DayOfWeek.THURSDAY.value)
        if (daysOfWeek and 32 == BIT_VALUE_FRIDAY) days.add(DayOfWeek.FRIDAY.value)
        if (daysOfWeek and 64 == BIT_VALUE_SATURDAY) days.add(DayOfWeek.SATURDAY.value)

        return days
    }

    override fun toJsonString(): String? = JsonDataStreamer.encodeToJson(this, Schedule::class.java)

    override fun toJsonFile(destination: File, name: String): File {
        return File(destination, name).apply {
            Okio.buffer(Okio.sink(this)).use {
                toJsonString()?.also { json -> it.write(json.toByteArray()) }
            }
        }
    }

    override fun fromInputStream(inputStream: InputStream) {
        JsonDataStreamer.decodeOnceFromJson(inputStream, Schedule::class.java)?.also {
            scheduleID = it.scheduleID
            daysOfWeek = it.daysOfWeek
            startTime = it.startTime
            endTime = it.endTime
            subject = it.subject
        }
    }

    companion object {
        const val BIT_VALUE_SUNDAY = 1
        const val BIT_VALUE_MONDAY = 2
        const val BIT_VALUE_TUESDAY = 4
        const val BIT_VALUE_WEDNESDAY = 8
        const val BIT_VALUE_THURSDAY = 16
        const val BIT_VALUE_FRIDAY = 32
        const val BIT_VALUE_SATURDAY = 64

        fun toJsonFile(items: List<Schedule>, destination: File,
                       name: String = Streamable.FILE_NAME_SCHEDULE): File {
            return File(destination, name).apply {
                Okio.buffer(Okio.sink(this)).use {
                    JsonDataStreamer.encodeToJson(items, Schedule::class.java)?.also { json ->
                        it.write(json.toByteArray())
                    }
                }
            }
        }

        fun getNearestDateTime(day: Int, time: LocalTime): ZonedDateTime {
            val currentDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            val currentDayOfWeek = currentDate.dayOfWeek.value
            var targetDay: Long = day.toLong()

            if (day <= currentDayOfWeek)
                targetDay += 7

            return currentDate.plusDays(targetDay - currentDayOfWeek)
                .withHour(time.hour)
                .withMinute(time.minute)
                .withSecond(time.second)
        }

        fun getNextWeekDay(day: Int, time: LocalTime): ZonedDateTime? {
            var currentDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                .plusHours(time.hour.toLong())
                .plusMinutes(time.minute.toLong())
            if (currentDate.dayOfWeek.value >= day)
                currentDate = currentDate.plusWeeks(1)
            return currentDate.with(DayOfWeek.of(day))
        }

        fun formatTime(time: LocalTime?): String? {
            return time?.print(DateTimeConverter.FORMAT_TIME)
        }
    }
}