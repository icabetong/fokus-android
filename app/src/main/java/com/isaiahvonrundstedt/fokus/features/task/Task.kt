package com.isaiahvonrundstedt.fokus.features.task

import android.content.Context
import android.os.Parcelable
import androidx.room.*
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*

@Parcelize
@Entity(tableName = "tasks", foreignKeys = [
    ForeignKey(entity = Subject::class, parentColumns = arrayOf("id"),
        childColumns = arrayOf("subjectID"), onDelete = ForeignKey.CASCADE)])
data class Task @JvmOverloads constructor (
    @PrimaryKey
    @ColumnInfo(index = true)
    var taskID: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var notes: String? = null,
    var subjectID: String? = null,
    @TypeConverters(DateTimeConverter::class)
    var dateAdded: DateTime? = DateTime.now(),
    @TypeConverters(DateTimeConverter::class)
    var dueDate: DateTime? = null,
    var isFinished: Boolean = false
): Parcelable {

    fun isDueToday(): Boolean {
        return dueDate!!.toLocalDate().isEqual(LocalDate.now())
    }

    fun formatDueDate(context: Context): String {
        val currentDateTime = LocalDate.now()

        // Check if the day on the task's due is today
        return if (dueDate!!.toLocalDate().isEqual(currentDateTime))
            String.format(context.getString(R.string.today_at), DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(dueDate))
        // Now check if the day is yesterday
        else if (currentDateTime.minusDays(1).compareTo(dueDate!!.toLocalDate()) == 0)
            String.format(context.getString(R.string.yesterday_at),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(dueDate))
        // Now check if its tomorrow
        else if (currentDateTime.plusDays(1).compareTo(dueDate!!.toLocalDate()) == 0)
            String.format(context.getString(R.string.tomorrow_at),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(dueDate))
        // Just print the date what could go wrong?
        else
            DateTimeFormat.forPattern("MMMM d, h:mm a").print(dueDate)
    }
}