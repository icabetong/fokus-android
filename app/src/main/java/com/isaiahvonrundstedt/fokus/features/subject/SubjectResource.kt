package com.isaiahvonrundstedt.fokus.features.subject

import androidx.room.Embedded
import androidx.room.Relation
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule

data class SubjectResource @JvmOverloads constructor (
    @Embedded
    var subject: Subject,
    @Relation(entity = Schedule::class, parentColumn = "subjectID", entityColumn = "subject")
    var scheduleList: List<Schedule> = emptyList()
)