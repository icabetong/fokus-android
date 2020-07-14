package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.core.work.subject.ClassNotificationWorker
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import kotlinx.coroutines.launch

class SubjectViewModel(app: Application) : BaseViewModel(app) {

    private var repository = SubjectRepository.getInstance(app)
    private var items: LiveData<List<SubjectResource>>? = repository.fetch()

    fun fetch(): LiveData<List<SubjectResource>>? = items

    fun insert(subject: Subject, scheduleList: List<Schedule>) = viewModelScope.launch {
        repository.insert(subject, scheduleList)

        if (preferenceManager.subjectReminder) {
            scheduleList.forEach {
                it.subject = subject.code

                val data = BaseWorker.convertScheduleToData(it)
                val request = OneTimeWorkRequest.Builder(ClassNotificationWorker::class.java)
                    .setInputData(data)
                    .build()

                workManager.enqueueUniqueWork(it.scheduleID, ExistingWorkPolicy.REPLACE,
                    request)
            }
        }
    }

    fun remove(subject: Subject,
               scheduleList: List<Schedule> = emptyList()) = viewModelScope.launch {
        repository.remove(subject)

        scheduleList.forEach {
            workManager.cancelAllWorkByTag(it.scheduleID)
        }
    }

    fun update(subject: Subject,
               scheduleList: List<Schedule> = emptyList()) = viewModelScope.launch {
        repository.update(subject, scheduleList)

        if (preferenceManager.subjectReminder) {
            scheduleList.forEach {
                workManager.cancelAllWorkByTag(it.scheduleID)

                it.subject = subject.code
                val data = BaseWorker.convertScheduleToData(it)
                val request = OneTimeWorkRequest.Builder(ClassNotificationWorker::class.java)
                    .setInputData(data)
                    .build()
                workManager.enqueueUniqueWork(it.scheduleID, ExistingWorkPolicy.REPLACE,
                    request)
            }
        }
    }

}