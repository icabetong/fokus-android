package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.core.work.subject.ClassNotificationWorker
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.widget.subject.SubjectWidgetProvider
import kotlinx.coroutines.launch

class SubjectViewModel(private var app: Application) : BaseViewModel(app) {

    private var repository = SubjectRepository.getInstance(app)
    private var items: LiveData<List<SubjectPackage>>? = repository.fetch()

    fun fetch(): LiveData<List<SubjectPackage>>? = items

    fun insert(subject: Subject, scheduleList: List<Schedule>) = viewModelScope.launch {
        repository.insert(subject, scheduleList)

        if (preferences.subjectReminder) {
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

        SubjectWidgetProvider.triggerRefresh(app)
    }

    fun remove(subject: Subject) = viewModelScope.launch {
        repository.remove(subject)

        workManager.cancelAllWorkByTag(subject.subjectID)

        SubjectWidgetProvider.triggerRefresh(app)
    }

    fun update(subject: Subject,
               scheduleList: List<Schedule> = emptyList()) = viewModelScope.launch {
        repository.update(subject, scheduleList)

        if (preferences.subjectReminder) {
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

        SubjectWidgetProvider.triggerRefresh(app)
    }

}