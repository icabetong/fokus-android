package com.isaiahvonrundstedt.fokus.features.task

import android.content.Context
import android.os.Parcelable
import androidx.room.*
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*

@Parcelize
@Entity(tableName = "tasks", foreignKeys = [
    ForeignKey(entity = Subject::class, parentColumns = arrayOf("subjectID"),
        childColumns = arrayOf("subject"), onDelete = ForeignKey.SET_NULL)])
data class Task @JvmOverloads constructor(
    @PrimaryKey
    @ColumnInfo(index = true)
    var taskID: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var notes: String? = null,
    var subject: String? = null,
    var isImportant: Boolean = false,
    @TypeConverters(DateTimeConverter::class)
    var dateAdded: DateTime? = DateTime.now(),
    @TypeConverters(DateTimeConverter::class)
    var dueDate: DateTime? = null,
    var isFinished: Boolean = false
) : Parcelable {

    fun hasDueDate(): Boolean {
        return dueDate != null
    }

    fun isDueDateInFuture(): Boolean {
        return if (hasDueDate())
            dueDate?.isAfterNow == true
        else false
    }

    fun isDueToday(): Boolean {
        return dueDate?.isEqualNow ?: false
    }

    fun formatDueDate(context: Context): String? {
        val currentDateTime = DateTime.now()

        if (!hasDueDate())
            return null

        // Check if the day on the task's due is today
        return if (dueDate?.isEqualNow == true)
            String.format(context.getString(R.string.today_at), DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(dueDate))
        // Now check if the day is yesterday
        else if (currentDateTime.minusDays(1).compareTo(dueDate) == 0)
            String.format(context.getString(R.string.yesterday_at),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(dueDate))
        // Now check if its tomorrow
        else if (currentDateTime.plusDays(1).compareTo(dueDate) == 0)
            String.format(context.getString(R.string.tomorrow_at),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(dueDate))
        // Just print the date what could go wrong?
        else
            DateTimeFormat.forPattern("MMMM d, h:mm a").print(dueDate)
    }
}