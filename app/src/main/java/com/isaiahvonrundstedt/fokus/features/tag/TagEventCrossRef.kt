package com.isaiahvonrundstedt.fokus.features.tag

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "eventTags", primaryKeys = ["eventID", "tagID"])
data class TagEventCrossRef(
    var eventID: String,
    var tagID: String
): Parcelable {}
