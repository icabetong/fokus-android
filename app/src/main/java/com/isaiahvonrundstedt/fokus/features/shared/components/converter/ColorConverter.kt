package com.isaiahvonrundstedt.fokus.features.shared.components.converter

import androidx.room.TypeConverter
import com.isaiahvonrundstedt.fokus.features.subject.Subject

class ColorConverter {

    companion object {
        @JvmStatic
        @TypeConverter
        fun toColor(rawColour: Int): Subject.Tag? {
            return Subject.Tag.convertColorToTag(rawColour)
        }

        @JvmStatic
        @TypeConverter
        fun fromColor(tag: Subject.Tag): Int {
            return tag.actualColor
        }
    }

}