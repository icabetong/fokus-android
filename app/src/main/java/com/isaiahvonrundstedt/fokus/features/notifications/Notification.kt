package com.isaiahvonrundstedt.fokus.features.notifications

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.HashMap

@Parcelize
@Entity(tableName = "notifications")
data class Notification @JvmOverloads constructor (
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var title: String? = null,
    var content: String? = null,
    var data: String? = null,
    var type: Int = typeReminder
): Parcelable {
    companion object {
        const val typeReminder = 0
        const val typeDueAlert = 1
    }
}