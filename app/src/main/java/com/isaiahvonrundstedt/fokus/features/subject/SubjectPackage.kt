package com.isaiahvonrundstedt.fokus.features.subject

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.room.Relation
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubjectPackage(
    @Embedded
    var subject: Subject,
    @Relation(entity = Schedule::class, parentColumn = "subjectID", entityColumn = "subject")
    var schedules: List<Schedule> = emptyList()
) : Parcelable {

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

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SubjectPackage>() {
            override fun areItemsTheSame(
                oldItem: SubjectPackage,
                newItem: SubjectPackage
            ): Boolean {
                return oldItem.subject.subjectID == newItem.subject.subjectID
            }

            override fun areContentsTheSame(
                oldItem: SubjectPackage,
                newItem: SubjectPackage
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}