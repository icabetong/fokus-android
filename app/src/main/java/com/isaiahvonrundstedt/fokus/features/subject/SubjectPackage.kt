package com.isaiahvonrundstedt.fokus.features.subject

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubjectPackage @JvmOverloads constructor(
    @Embedded
    var subject: Subject,
    @Relation(entity = Schedule::class, parentColumn = "subjectID", entityColumn = "subject")
    var schedules: List<Schedule> = emptyList()
): Parcelable