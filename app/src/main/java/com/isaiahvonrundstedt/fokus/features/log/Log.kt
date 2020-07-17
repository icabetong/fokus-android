package com.isaiahvonrundstedt.fokus.features.log

import android.os.Parcelable
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*

@Parcelize
@Entity(tableName = "logs")
data class Log @JvmOverloads constructor(
    @PrimaryKey
    var logID: String = UUID.randomUUID().toString(),
    var title: String? = null,
    var content: String? = null,
    var data: String? = null,
    var type: Int = TYPE_GENERIC,
    var isPersistent: Boolean = false,
    @TypeConverters(DateTimeConverter::class)
    var dateTimeTriggered: DateTime? = null
) : Parcelable {

    fun setIconToView(sourceView: ImageView) {
        with(sourceView) {
            background = ContextCompat.getDrawable(this.context,
                R.drawable.shape_background_icon)?.also {
                it.setTint(ContextCompat.getColor(this.context, getIconBackgroundColorResource()))
            }
            setImageDrawable(ContextCompat.getDrawable(this.context, getIconResource())?.also {
                it.setTint(ContextCompat.getColor(this.context, getIconColorResource()))
            })
        }
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

    @DrawableRes
    private fun getIconResource(): Int {
        return when (type) {
            TYPE_TASK -> R.drawable.ic_outline_done_24
            TYPE_EVENT -> R.drawable.ic_outline_event_24
            TYPE_CLASS -> R.drawable.ic_outline_square_foot_24
            TYPE_GENERIC -> R.drawable.ic_outline_emoji_objects_24
            else -> R.drawable.ic_outline_emoji_objects_24
        }
    }

    @ColorRes
    private fun getIconColorResource(): Int {
        return when (type) {
            TYPE_TASK -> R.color.color_theme_task
            TYPE_EVENT -> R.color.color_theme_events
            TYPE_CLASS -> R.color.color_theme_subjects
            TYPE_GENERIC -> R.color.color_theme_generic
            else -> R.color.color_theme_generic
        }
    }

    @ColorRes
    private fun getIconBackgroundColorResource(): Int {
        return when (type) {
            TYPE_TASK -> R.color.color_theme_task_variant
            TYPE_EVENT -> R.color.color_theme_events_variant
            TYPE_CLASS -> R.color.color_theme_subjects_variant
            TYPE_GENERIC -> R.color.color_theme_generic_variant
            else -> R.color.color_theme_generic_variant
        }
    }

    companion object {
        const val TYPE_GENERIC = 0
        const val TYPE_TASK = 1
        const val TYPE_EVENT = 2
        const val TYPE_CLASS = 3
    }
}