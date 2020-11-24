package com.isaiahvonrundstedt.fokus.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.database.dao.ScheduleDAO
import com.isaiahvonrundstedt.fokus.database.dao.SubjectDAO
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import javax.inject.Inject

class SubjectRepository @Inject constructor(
    private val subjects: SubjectDAO,
    private val schedules: ScheduleDAO
) {

    fun fetchLiveData(): LiveData<List<SubjectPackage>> = subjects.fetchLiveData()

    suspend fun fetch(): List<SubjectPackage> = subjects.fetch()

    suspend fun insert(subject: Subject, scheduleList: List<Schedule> = emptyList()) {
        subjects.insert(subject)
        if (scheduleList.isNotEmpty())
            scheduleList.forEach { schedules.insert(it) }
    }

    suspend fun remove(subject: Subject) {
        subjects.remove(subject)
    }

    suspend fun update(subject: Subject, scheduleList: List<Schedule> = emptyList()) {
        subjects.update(subject)
        schedules.removeUsingSubjectID(subject.subjectID)
        if (scheduleList.isNotEmpty())
            scheduleList.forEach { schedules.insert(it) }
    }

}