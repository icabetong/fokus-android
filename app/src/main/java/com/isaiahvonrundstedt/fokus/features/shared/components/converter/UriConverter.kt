package com.isaiahvonrundstedt.fokus.features.shared.components.converter

import android.net.Uri
import androidx.room.TypeConverter

class UriConverter private constructor() {

    companion object {
        @JvmStatic
        @TypeConverter
        fun toUri(s: String): Uri? {
            return Uri.parse(s)
        }

        @JvmStatic
        @TypeConverter
        fun fromUri(u: Uri): String {
            return u.toString()
        }
    }

}