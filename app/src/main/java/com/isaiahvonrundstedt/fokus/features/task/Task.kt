package com.isaiahvonrundstedt.fokus.features.task

import android.content.Context
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.android.parcel.Parcelize
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

@Parcelize
@Entity(tableName = "tasks", foreignKeys = [
    ForeignKey(entity = Subject::class, parentColumns = arrayOf("id"), childColumns = arrayOf("subjectID"),
        onDelete = ForeignKey.CASCADE)])
data class Task @JvmOverloads constructor (
    @PrimaryKey
    var taskID: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var notes: String? = null,
    var subjectID: String? = null,
    @TypeConverters(DateTimeConverter::class)
    var dateAdded: LocalDateTime? = LocalDateTime.now(),
    @TypeConverters(DateTimeConverter::class)
    var dueDate: LocalDateTime? = null,
    var isFinished: Boolean = false,
    var isArchived: Boolean = false
): Parcelable {

    fun isDueToday(): Boolean {
        return dueDate!!.toLocalDate().isEqual(LocalDate.now())
    }

    companion object {
        fun formatDueDate(context: Context, time: LocalDateTime): String {
            val currentDateTime = LocalDate.now()

            return if (time.toLocalDate().isEqual(currentDateTime))
                String.format(context.getString(R.string.today_at), DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(time))
            else if (currentDateTime.minusDays(1).compareTo(time.toLocalDate()) == 0)
                String.format(context.getString(R.string.yesterday),
                    DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(time))
            else if (currentDateTime.plusDays(1).compareTo(time.toLocalDate()) == 0)
                String.format(context.getString(R.string.tomorrow),
                    DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(time))
            else
                DateTimeFormat.forPattern("EE - MMMM d, h:mm a").print(time)
        }

    }
}