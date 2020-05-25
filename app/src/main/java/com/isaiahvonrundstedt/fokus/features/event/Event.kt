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
    
    fun formatSchedule(context: Context): String {
        val currentDateTime = LocalDate.now()

        return if (schedule!!.toLocalDate().isEqual(currentDateTime))
            String.format(context.getString(R.string.today_at), DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(schedule))
        else if (currentDateTime.minusDays(1).compareTo(schedule!!.toLocalDate()) == 0)
            String.format(context.getString(R.string.yesterday),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(schedule))
        else if (currentDateTime.plusDays(1).compareTo(schedule!!.toLocalDate()) == 0)
            String.format(context.getString(R.string.tomorrow),
                DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(schedule))
        else
            DateTimeFormat.forPattern("EEE - MMMM d, h:mm a").print(schedule)
    }
    
}