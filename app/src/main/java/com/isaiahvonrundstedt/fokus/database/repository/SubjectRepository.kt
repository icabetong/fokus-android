package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage

class SubjectRepository private constructor(app: Application) {

    companion object {
        private var instance: SubjectRepository? = null

        fun getInstance(app: Application): SubjectRepository {
            if (instance == null) {
                synchronized(SubjectRepository::class) {
                    instance = SubjectRepository(app)
                }
            }
            return instance!!
        }
    }

    private var database = AppDatabase.getInstance(app)
    private var subjects = database.subjects()
    private var schedules = database.schedules()

    fun fetch(): LiveData<List<SubjectPackage>> = subjects.fetchLiveData()

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