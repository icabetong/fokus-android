package com.isaiahvonrundstedt.fokus.features.task

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.room.*
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isAfterNow
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isToday
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isTomorrow
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isYesterday
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import okio.buffer
import okio.sink
import java.io.File
import java.io.InputStream
import java.time.ZonedDateTime
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "tasks", foreignKeys = [
        ForeignKey(
            entity = Subject::class, parentColumns = arrayOf("subjectID"),
            childColumns = arrayOf("subject"), onDelete = ForeignKey.SET_NULL
        )]
)
data class Task @JvmOverloads constructor(
    @PrimaryKey
    @ColumnInfo(index = true)
    var taskID: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var notes: String? = null,
    var subject: String? = null,
    var isImportant: Boolean = false,
    @TypeConverters(DateTimeConverter::class)
    var dueDate: ZonedDateTime? = null,
    var isFinished: Boolean = false,
    var isTaskArchived: Boolean = false,
    @TypeConverters(DateTimeConverter::class)
    var dateAdded: ZonedDateTime? = ZonedDateTime.now(),
) : Parcelable, Streamable {

    fun hasDueDate(): Boolean {
        return dueDate != null
    }

    fun isDueDateInFuture(): Boolean {
        return dueDate?.isAfterNow() == true
    }

    fun isDueToday(): Boolean {
        return dueDate?.isToday() == true
    }

    fun formatDueDate(context: Context): String? {
        if (dueDate == null)
            return ""

        // Check if the day on the task's due is today
        return if (dueDate?.isToday() == true)
            String.format(
                context.getString(R.string.today_at),
                dueDate?.format(DateTimeConverter.getTimeFormatter(context))
            )
        // Now check if the day is yesterday
        else if (dueDate?.isYesterday() == true)
            String.format(
                context.getString(R.string.yesterday_at),
                dueDate?.format(DateTimeConverter.getTimeFormatter(context))
            )
        // Now check if its tomorrow
        else if (dueDate?.isTomorrow() == true)
            String.format(
                context.getString(R.string.tomorrow_at),
                dueDate?.format(DateTimeConverter.getTimeFormatter(context))
            )
        // Just print the date what could go wrong?
        else dueDate?.format(DateTimeConverter.getDateTimeFormatter(context))
    }

    override fun toJsonString(): String? = JsonDataStreamer.encodeToJson(this, Task::class.java)

    override fun toJsonFile(destination: File, name: String): File {
        return File(destination, name).apply {
            this.sink().buffer().use {
                toJsonString()?.also { json -> it.write(json.toByteArray()) }
            }
        }
    }

    override fun fromInputStream(inputStream: InputStream) {
        JsonDataStreamer.decodeOnceFromJson(inputStream, Task::class.java)?.also {
            taskID = it.taskID
            name = it.name
            dueDate = it.dueDate
            notes = it.notes
            subject = it.subject
            dateAdded = it.dateAdded
            isImportant = it.isImportant
            isFinished = it.isFinished
        }
    }

    companion object {
        const val EXTRA_ID = "extra:id"
        const val EXTRA_NAME = "extra:name"
        const val EXTRA_DUE_DATE = "extra:due"
        const val EXTRA_NOTES = "extra:notes"
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_DATE_ADDED = "extra:added"
        const val EXTRA_IS_IMPORTANT = "extra:important"
        const val EXTRA_IS_FINISHED = "extra:finished"
        const val EXTRA_IS_ARCHIVED = "extra:archived"

        fun toBundle(task: Task): Bundle {
            return bundleOf(
                EXTRA_ID to task.taskID,
                EXTRA_NAME to task.name,
                EXTRA_DUE_DATE to task.dueDate,
                EXTRA_NOTES to task.notes,
                EXTRA_SUBJECT to task.subject,
                EXTRA_DATE_ADDED to task.dateAdded,
                EXTRA_IS_FINISHED to task.isFinished,
                EXTRA_IS_IMPORTANT to task.isImportant,
                EXTRA_IS_ARCHIVED to task.isTaskArchived
            )
        }

        fun fromBundle(bundle: Bundle): Task? {
            if (!bundle.containsKey(EXTRA_ID))
                return null

            return Task(
                taskID = bundle.getString(EXTRA_ID)!!,
                name = bundle.getString(EXTRA_NAME),
                dueDate = bundle.getSerializable(EXTRA_DUE_DATE) as? ZonedDateTime,
                notes = bundle.getString(EXTRA_NOTES),
                subject = bundle.getString(EXTRA_SUBJECT),
                dateAdded = bundle.getSerializable(EXTRA_DATE_ADDED) as ZonedDateTime,
                isFinished = bundle.getBoolean(EXTRA_IS_FINISHED),
                isImportant = bundle.getBoolean(EXTRA_IS_IMPORTANT),
                isTaskArchived = bundle.getBoolean(EXTRA_IS_ARCHIVED)
            )
        }

        fun fromInputStream(inputStream: InputStream): Task {
            return Task().apply {
                this.fromInputStream(inputStream)
            }
        }
    }
}