package com.isaiahvonrundstedt.fokus.features.event

import android.content.Context
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*

@Parcelize
@Entity(tableName = "events")
data class Event @JvmOverloads constructor(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var notes: String? = null,
    var location: String? = null,
    @TypeConverters(DateTimeConverter::class)
    var schedule: DateTime? = null
): Parcelable {

    fun formatScheduleDate(context: Context): String {
        return if (schedule!!.isEqualNow)
            context.getString(R.string.today)
        else if (DateTime.now().minusDays(1).compareTo(schedule!!) == 0)
            context.getString(R.string.yesterday)
        else if (DateTime.now().plusDays(1).compareTo(schedule!!) == 0)
            context.getString(R.string.tomorrow)
        else DateTimeFormat.forPattern("MMMM d").print(schedule)
    }

    fun formatScheduleTime(): String {
        return DateTimeFormat.forPattern("h:mm a").print(schedule)
    }

    fun formatSchedule(context: Context): String {
        val currentDateTime = LocalDate.now()

        return if (schedule!!.toLocalDate().isEqual(currentDateTime))
            String.format(context.getString(R.string.today_at), DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(schedule))
        else if (currentDateTime.minusDays(1).compareTo(schedule!!.toLocalDate()) == 0)
            String.format(context.getString(R.string.yesterday_at),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(schedule))
        else if (currentDateTime.plusDays(1).compareTo(schedule!!.toLocalDate()) == 0)
            String.format(context.getString(R.string.tomorrow_at),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(schedule))
        else
            DateTimeFormat.forPattern("EEE - MMMM d, h:mm a").print(schedule)
    }

}