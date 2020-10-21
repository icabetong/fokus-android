package com.isaiahvonrundstedt.fokus.features.event

import android.content.Context
import android.os.Parcelable
import android.text.format.DateFormat
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isToday
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isTomorrow
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isYesterday
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import okio.Okio
import java.io.File
import java.io.InputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "events", foreignKeys = [
    ForeignKey(entity = Subject::class, parentColumns = arrayOf("subjectID"),
        childColumns = arrayOf("subject"), onDelete = ForeignKey.SET_NULL)
])
data class Event @JvmOverloads constructor(
    @PrimaryKey
    var eventID: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var notes: String? = null,
    var location: String? = null,
    var subject: String? = null,
    var isImportant: Boolean = false,
    @TypeConverters(DateTimeConverter::class)
    var schedule: ZonedDateTime? = null,
    @TypeConverters(DateTimeConverter::class)
    var dateAdded: ZonedDateTime = ZonedDateTime.now()
) : Parcelable, Streamable {

    fun isToday(): Boolean {
        return schedule?.isToday() == true
    }

    fun formatScheduleTime(context: Context): String? {
        return schedule?.format(DateTimeConverter.getTimeFormatter(context))
    }

    fun formatSchedule(context: Context): String? {
        val datePattern = if (DateFormat.is24HourFormat(context))
            FORMAT_DATE_WITH_WEEKDAY_24_HOUR
        else FORMAT_DATE_WITH_WEEKDAY_12_HOUR

        return if (schedule?.isToday() == true)
                String.format(context.getString(R.string.today_at),
                    schedule?.format(DateTimeConverter.getTimeFormatter(context)))
            else if (schedule?.isYesterday() == true)
                String.format(context.getString(R.string.yesterday_at),
                    schedule?.format(DateTimeConverter.getTimeFormatter(context)))
            else if (schedule?.isTomorrow() == true)
                String.format(context.getString(R.string.tomorrow_at),
                    schedule?.format(DateTimeConverter.getTimeFormatter(context)))
            else schedule?.format(DateTimeFormatter.ofPattern(datePattern))
    }

    override fun toJsonString(): String? = JsonDataStreamer.encodeToJson(this, Event::class.java)

    override fun toJsonFile(destination: File, name: String): File {
        return File(destination, name).apply {
            Okio.buffer(Okio.sink(this)).use {
                toJsonString()?.also { json -> it.write(json.toByteArray()) }
            }
        }
    }

    override fun fromInputStream(inputStream: InputStream) {
        JsonDataStreamer.decodeOnceFromJson(inputStream, Event::class.java)?.also {
            eventID = it.eventID
            name = it.name
            location = it.location
            schedule = it.schedule
            dateAdded = it.dateAdded
            isImportant = it.isImportant
            notes = it.notes
            subject = it.subject
        }
    }

    companion object {
        const val FORMAT_DATE_WITH_WEEKDAY_12_HOUR = "EEE - MMMM d, h:mm a"
        const val FORMAT_DATE_WITH_WEEKDAY_24_HOUR = "EEE - MMMM d, H:mm"

        fun fromInputStream(inputStream: InputStream): Event {
            return Event().apply {
                this.fromInputStream(inputStream)
            }
        }
    }
}