package com.isaiahvonrundstedt.fokus.features.notifications

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
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
    var type: Int = typeReminder,
    var dateTimeTriggered: LocalDateTime? = null
): Parcelable {

    fun formatDateTime(): String {
        return if (dateTimeTriggered!!.toLocalDate() == LocalDate.now())
            DateTimeFormat.forPattern("h:mm a").print(dateTimeTriggered)
        else DateTimeFormat.forPattern("M d yyyy, h:mm a").print(dateTimeTriggered)
    }

    companion object {
        const val typeReminder = 0
        const val typeDueAlert = 1
    }
}