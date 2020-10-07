package com.isaiahvonrundstedt.fokus.features.subject

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

@Parcelize
data class SubjectPackage @JvmOverloads constructor(
    @Embedded
    var subject: Subject,
    @Relation(entity = Schedule::class, parentColumn = "subjectID", entityColumn = "subject")
    var schedules: List<Schedule> = emptyList()
): Parcelable {

    fun hasScheduleToday(): Boolean {
        for (s: Schedule in schedules)
            if (s.isToday()) return true
        return false
    }

    fun getScheduleToday(): Schedule? {
        for (s: Schedule in schedules)
            if (s.isToday()) return s
        return null
    }

    fun hasScheduleTomorrow(): Boolean {
        for (s: Schedule in schedules)
            if (s.isTomorrow()) return true
        return false
    }

    fun getScheduleTomorrow(): Schedule? {
        for (s: Schedule in schedules)
            if (s.isTomorrow()) return s
        return null
    }
}