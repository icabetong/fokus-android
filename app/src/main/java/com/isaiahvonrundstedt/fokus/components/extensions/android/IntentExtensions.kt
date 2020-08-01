package com.isaiahvonrundstedt.fokus.components.extensions.android

import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList

fun <T : Parcelable> Intent.putExtra(key: String, items: List<T>) {
    putParcelableArrayListExtra(key, items.toArrayList())
}

fun <T : Parcelable> Intent.getParcelableListExtra(key: String): List<T>? {
    return getParcelableArrayListExtra<T>(key)?.toList()
}

fun Intent.putStringListExtra(key: String, items: List<String>) {
    putStringArrayListExtra(key, ArrayList(items))
}