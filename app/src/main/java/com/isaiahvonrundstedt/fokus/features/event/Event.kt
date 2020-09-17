package com.isaiahvonrundstedt.fokus.features.event

import android.content.Context
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.jodatime.isToday
import com.isaiahvonrundstedt.fokus.components.extensions.jodatime.isTomorrow
import com.isaiahvonrundstedt.fokus.components.extensions.jodatime.isYesterday
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import okio.Okio
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.io.File
import java.io.InputStream
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
    var schedule: DateTime? = null,
    @TypeConverters(DateTimeConverter::class)
    var dateAdded: DateTime = DateTime.now()
) : Parcelable, Streamable {

    fun isToday(): Boolean {
        return schedule?.toLocalDate()?.compareTo(LocalDate.now()) == 0
    }

    fun formatScheduleDate(context: Context): String {
        return if (schedule?.isToday() == true)
            context.getString(R.string.today)
        else if (schedule?.isYesterday() == true)
            context.getString(R.string.yesterday)
        else if (schedule?.isTomorrow() == true)
            context.getString(R.string.tomorrow)
        else DateTimeFormat.forPattern("MMM d").print(schedule)
    }

    fun formatScheduleTime(): String {
        return DateTimeFormat.forPattern("h:mm a").print(schedule)
    }

    fun formatSchedule(context: Context): String {
        return if (schedule?.isToday() == true)
                String.format(context.getString(R.string.today_at), DateTimeFormat.forPattern(DateTimeConverter.FORMAT_TIME).print(schedule))
            else if (schedule?.isYesterday() == true)
                String.format(context.getString(R.string.yesterday_at),
                    DateTimeFormat.forPattern(DateTimeConverter.FORMAT_TIME).print(schedule))
            else if (schedule?.isTomorrow() == true)
                String.format(context.getString(R.string.tomorrow_at),
                    DateTimeFormat.forPattern(DateTimeConverter.FORMAT_TIME).print(schedule))
            else
                DateTimeFormat.forPattern("EEE - MMMM d, h:mm a").print(schedule)

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
        const val FIELD_EVENT_NAME = "event:name"
        const val FIELD_EVENT_SCHEDULE = "event:schedule"
        const val FIELD_EVENT_LOCATION = "event:location"

        fun fromInputStream(inputStream: InputStream): Event {
            return Event().apply {
                this.fromInputStream(inputStream)
            }
        }
    }
}