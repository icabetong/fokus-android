package com.isaiahvonrundstedt.fokus.features.notifications

import android.os.Parcelable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*

@Parcelize
@Entity(tableName = "notifications")
data class Notification @JvmOverloads constructor (
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var title: String? = null,
    var content: String? = null,
    var data: String? = null,
    var type: Int = typeGeneric,
    var isPersistent: Boolean = false,
    @TypeConverters(DateTimeConverter::class)
    var dateTimeTriggered: DateTime? = null
): Parcelable {

    fun tintDrawable(sourceView: ImageView) {
        val colorID = if (type == typeGeneric) R.color.colorIconReminder else R.color.colorIconWarning
        sourceView.setImageDrawable(sourceView.drawable.mutate().apply {
            colorFilter = BlendModeColorFilterCompat
                .createBlendModeColorFilterCompat(ContextCompat.getColor(sourceView.context, colorID),
                    BlendModeCompat.SRC_ATOP)
        })
    }

    fun formatDateTime(): String {
        val currentDateTime = LocalDate.now()

        // Formats the dateTime object for human reading
        return if (dateTimeTriggered!!.toLocalDate().isEqual(currentDateTime))
            DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(dateTimeTriggered)
        else if (dateTimeTriggered!!.toLocalDate().year == currentDateTime.year)
            DateTimeFormat.forPattern("MMMM d").print(dateTimeTriggered!!)
        else DateTimeFormat.forPattern("MMMM d yyyy").print(dateTimeTriggered!!)
    }

    companion object {
        const val typeGeneric = 0
        const val typeTaskReminder = 1
        const val typeEventReminder = 2
    }
}