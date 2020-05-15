package com.isaiahvonrundstedt.fokus.features.subject

import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.ColorConverter
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.collections.ArrayList

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
) {

    enum class Tag(val actualColor: Int) {
        SKY(Color.parseColor("#2196f3")),
        GRASS(Color.parseColor("#71a234")),
        SUNSET(Color.parseColor("#ff7e0f")),
        LEMON(Color.parseColor("#ffb600")),
        SEA(Color.parseColor("#01b1af")),
        GRAPE(Color.parseColor("#c14ce6")),
        LEAF(Color.parseColor("#0f9d58")),
        ROSE(Color.parseColor("#f15b8d"));

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

            fun getName(tag: Tag): Int {
                return when (tag) {
                    SKY -> R.string.tag_color_sky
                    GRASS -> R.string.tag_color_grass
                    SUNSET -> R.string.tag_color_sunset
                    LEMON -> R.string.tag_color_lemon
                    SEA -> R.string.tag_color_sea
                    GRAPE -> R.string.tag_color_grape
                    LEAF -> R.string.tag_color_leaf
                    ROSE -> R.string.tag_color_rose
                }
            }
        }
    }

    fun formatStartTime(): String {
        return formatTime(startTime)
    }

    fun formatEndTime(): String {
        return formatTime(endTime)
    }

    fun tintDrawable(drawable: Drawable): Drawable {
        return Companion.tintDrawable(drawable, tag)
    }

    companion object {

        fun tintDrawable(drawable: Drawable, tag: Tag): Drawable {
            drawable.mutate()
            drawable.colorFilter = BlendModeColorFilterCompat
                .createBlendModeColorFilterCompat(tag.actualColor, BlendModeCompat.SRC_ATOP)
            return drawable
        }

        fun getDayNameResource(day: Int): Int {
            return when (day) {
                Calendar.SUNDAY -> R.string.days_of_week_item_sunday
                Calendar.MONDAY -> R.string.days_of_week_item_monday
                Calendar.TUESDAY -> R.string.days_of_week_item_tuesday
                Calendar.WEDNESDAY -> R.string.days_of_week_item_wednesday
                Calendar.THURSDAY -> R.string.days_of_week_item_thursday
                Calendar.FRIDAY -> R.string.days_of_week_item_friday
                Calendar.SATURDAY -> R.string.days_of_week_item_saturday
                else -> 0
            }
        }

        fun getDays(bits: Int): ArrayList<Int> {
            val days = ArrayList<Int>()

            if (bits and 1 == 1) days.add(Calendar.SUNDAY)
            if (bits and 2 == 2) days.add(Calendar.MONDAY)
            if (bits and 4 == 4) days.add(Calendar.TUESDAY)
            if (bits and 8 == 8) days.add(Calendar.WEDNESDAY)
            if (bits and 16 == 16) days.add(Calendar.THURSDAY)
            if (bits and 32 == 32) days.add(Calendar.FRIDAY)
            if (bits and 64 == 64) days.add(Calendar.SATURDAY)

            return days
        }

        fun formatTime(time: LocalTime?): String {
            return DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(time)
        }
    }
}