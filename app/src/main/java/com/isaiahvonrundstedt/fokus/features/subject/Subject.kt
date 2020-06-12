package com.isaiahvonrundstedt.fokus.features.subject

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.converter.ColorConverter
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
@Entity(tableName = "subjects")
data class Subject @JvmOverloads constructor (
    @PrimaryKey
    @ColumnInfo(index = true)
    var id: String = UUID.randomUUID().toString(),
    var code: String? = null,
    var description: String? = null,
    var daysOfWeek: Int = 0,
    @TypeConverters(DateTimeConverter::class)
    var startTime: LocalTime? = null,
    @TypeConverters(DateTimeConverter::class)
    var endTime: LocalTime? = null,
    @TypeConverters(ColorConverter::class)
    var tag: Tag = Tag.SKY
): Parcelable {

    // Used for the color tag of the subject
    enum class Tag(val actualColor: Int) {
        SKY(Color.parseColor("#2196f3")),
        GRASS(Color.parseColor("#71a234")),
        SUNSET(Color.parseColor("#ff7e0f")),
        LEMON(Color.parseColor("#ffb600")),
        SEA(Color.parseColor("#01b1af")),
        GRAPE(Color.parseColor("#9c27b0")),
        CHERRY(Color.parseColor("#f50057")),
        CORAL(Color.parseColor("#f15b8d")),
        MIDNIGHT(Color.parseColor("#1a237e")),
        LAVENDER(Color.parseColor("#b39ddb")),
        MINT(Color.parseColor("#009c56")),
        GRAPHITE(Color.parseColor("#757575"));

        fun getNameResource(): Int {
            return when (this) {
                SKY -> R.string.tag_color_sky
                GRASS -> R.string.tag_color_grass
                SUNSET -> R.string.tag_color_sunset
                LEMON -> R.string.tag_color_lemon
                SEA -> R.string.tag_color_sea
                GRAPE -> R.string.tag_color_grape
                CHERRY -> R.string.tag_color_cherry
                CORAL -> R.string.tag_color_coral
                MIDNIGHT -> R.string.tag_color_midnight
                MINT -> R.string.tag_color_mint
                LAVENDER -> R.string.tag_color_lavender
                GRAPHITE -> R.string.tag_color_graphite
            }
        }

        companion object {
            private val colors: MutableMap<Int, Tag> = HashMap()
            init {
                for (i in values())
                    colors[i.actualColor] = i
            }

            fun convertColorToTag(int: Int): Tag? = colors[int]

            fun getColors(): IntArray {
                return colors.keys.toIntArray()
            }
        }
    }

    fun formatStartTime(): String {
        return formatTime(startTime)
    }

    fun formatEndTime(): String {
        return formatTime(endTime)
    }

    fun tintDrawable(drawable: Drawable?): Drawable? {
        return Companion.tintDrawable(drawable, tag)
    }

    /**
     *   Function to format the daysOfWeek attribute
     *   to human readable form
     *   @param context used to fetch the appropriate string localization
     *                  for the string resource id
     *   @return the formatted days of week
     *           (e.g. "Sunday, Monday and Thursday")
     */
    private fun formatDaysOfWeek(context: Context): String {
        val builder = StringBuilder()
        val list = getDaysList()
        list.forEachIndexed { index, i ->
            // Append the appropriate day name string from string resource
            builder.append(context.getString(getStringResourceForDay(i)))

            // Check if the item's index is second to last,
            // if it is, then add an "and" from string resource
            // and if not, just append a comma
            if (index == list.size - 2)
                builder.append(context.getString(R.string.and))
            else if (index < list.size - 2)
                builder.append(", ")
        }
        return builder.toString()
    }

    /**
     *   Function to format the daysOfWeek attribute
     *   together with the startTime and endTime attribute
     *   @param context used to fetch the appropriate string localization
     *                  for the string resource id
     *   @return the formatted schedule
     *           (e.g. "Sunday and Monday, 10:00AM to 11:30AM")
     */
    fun formatSchedule(context: Context): String {
        val builder = StringBuilder(formatDaysOfWeek(context))
        builder.append(", ")
            .append(formatStartTime())
            .append(" - ")
            .append(formatEndTime())
        return builder.toString()
    }

    /**
     *   Function to get the string resource ID using
     *   a day value from DateTimeConstants class
     *   @param day a day of week value from DateTimeConstants class
     *   @return a string resource id which can be used in getString()
     */
    private fun getStringResourceForDay(day: Int): Int {
        return when (day) {
            DateTimeConstants.SUNDAY -> R.string.days_of_week_item_sunday
            DateTimeConstants.MONDAY -> R.string.days_of_week_item_monday
            DateTimeConstants.TUESDAY -> R.string.days_of_week_item_tuesday
            DateTimeConstants.WEDNESDAY -> R.string.days_of_week_item_wednesday
            DateTimeConstants.THURSDAY -> R.string.days_of_week_item_thursday
            DateTimeConstants.FRIDAY -> R.string.days_of_week_item_friday
            DateTimeConstants.SATURDAY -> R.string.days_of_week_item_saturday
            else -> 0
        }
    }

    /**
     *   Function to determine the days selected
     *   from a bitwise value
     *   @return a list which contains the days inherited
     *           from the DateTimeConstants class
     */
    fun getDaysList(): List<Int> {
        val days = ArrayList<Int>()

        if (daysOfWeek and 1 == 1) days.add(DateTimeConstants.SUNDAY)
        if (daysOfWeek and 2 == 2) days.add(DateTimeConstants.MONDAY)
        if (daysOfWeek and 4 == 4) days.add(DateTimeConstants.TUESDAY)
        if (daysOfWeek and 8 == 8) days.add(DateTimeConstants.WEDNESDAY)
        if (daysOfWeek and 16 == 16) days.add(DateTimeConstants.THURSDAY)
        if (daysOfWeek and 32 == 32) days.add(DateTimeConstants.FRIDAY)
        if (daysOfWeek and 64 == 64) days.add(DateTimeConstants.SATURDAY)

        return days
    }

    companion object {

        const val BIT_VALUE_SUNDAY = 1
        const val BIT_VALUE_MONDAY = 2
        const val BIT_VALUE_TUESDAY = 4
        const val BIT_VALUE_WEDNESDAY = 8
        const val BIT_VALUE_THURSDAY = 16
        const val BIT_VALUE_FRIDAY = 32
        const val BIT_VALUE_SATURDAY = 64

        fun tintDrawable(drawable: Drawable?, tag: Tag): Drawable? {
            drawable?.let {
                it.mutate()
                it.colorFilter = BlendModeColorFilterCompat
                    .createBlendModeColorFilterCompat(tag.actualColor, BlendModeCompat.SRC_ATOP)
            }
            return drawable
        }

        fun formatTime(time: LocalTime?): String {
            return DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(time)
        }
    }
}