package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage

class SubjectRepository private constructor(context: Context) {

    private var database = AppDatabase.getInstance(context)
    private var subjects = database.subjects()
    private var schedules = database.schedules()

    companion object {
        private var instance: SubjectRepository? = null

        fun getInstance(context: Context): SubjectRepository {
            if (instance == null) {
                synchronized(SubjectRepository::class) {
                    instance = SubjectRepository(context)
                }
            }
            return instance!!
        }
    }

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