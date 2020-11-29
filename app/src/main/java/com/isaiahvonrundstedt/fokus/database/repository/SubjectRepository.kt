package com.isaiahvonrundstedt.fokus.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.dao.ScheduleDAO
import com.isaiahvonrundstedt.fokus.database.dao.SubjectDAO
import com.isaiahvonrundstedt.fokus.features.notifications.subject.ClassNotificationWorker
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import com.isaiahvonrundstedt.fokus.features.subject.widget.SubjectWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SubjectRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val subjects: SubjectDAO,
    private val schedules: ScheduleDAO,
    private val preferenceManager: PreferenceManager,
    private val workManager: WorkManager
) {

    fun fetchLiveData(): LiveData<List<SubjectPackage>> = subjects.fetchLiveData()

    suspend fun fetch(): List<SubjectPackage> = subjects.fetch()

    suspend fun insert(subject: Subject, scheduleList: List<Schedule> = emptyList()) {
        subjects.insert(subject)
        if (scheduleList.isNotEmpty())
            scheduleList.forEach { schedules.insert(it) }

        SubjectWidgetProvider.triggerRefresh(context)

        if (preferenceManager.subjectReminder) {
            scheduleList.forEach {
                it.subject = subject.code

                val data = BaseWorker.convertScheduleToData(it)
                val request = OneTimeWorkRequest.Builder(ClassNotificationWorker::class.java)
                    .setInputData(data)
                    .addTag(subject.subjectID)
                    .addTag(it.scheduleID)
                    .build()

                workManager.enqueueUniqueWork(it.scheduleID, ExistingWorkPolicy.REPLACE,
                    request)
            }
        }
    }

    suspend fun remove(subject: Subject) {
        subjects.remove(subject)

        SubjectWidgetProvider.triggerRefresh(context)

        workManager.cancelAllWorkByTag(subject.subjectID)
    }

    suspend fun update(subject: Subject, scheduleList: List<Schedule> = emptyList()) {
        subjects.update(subject)
        schedules.removeUsingSubjectID(subject.subjectID)
        if (scheduleList.isNotEmpty())
            scheduleList.forEach { schedules.insert(it) }

        SubjectWidgetProvider.triggerRefresh(context)

        if (preferenceManager.subjectReminder) {
            scheduleList.forEach {
                workManager.cancelAllWorkByTag(it.scheduleID)

                it.subject = subject.code
                val data = BaseWorker.convertScheduleToData(it)
                val request = OneTimeWorkRequest.Builder(ClassNotificationWorker::class.java)
                    .setInputData(data)
                    .addTag(subject.subjectID)
                    .addTag(it.scheduleID)
                    .build()
                workManager.enqueueUniqueWork(it.scheduleID, ExistingWorkPolicy.REPLACE,
                    request)
            }
        }
    }
}