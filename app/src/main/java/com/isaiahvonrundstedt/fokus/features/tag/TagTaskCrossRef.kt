package com.isaiahvonrundstedt.fokus.features.tag

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "taskTags", primaryKeys = ["taskID", "tagID"])
data class TagTaskCrossRef (
    var taskID: String,
    var tagID: String,
): Parcelable {}
