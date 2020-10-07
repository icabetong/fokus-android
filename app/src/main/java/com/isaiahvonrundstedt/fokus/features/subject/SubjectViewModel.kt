package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.notifications.subject.ClassNotificationWorker
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.widget.subject.SubjectWidgetProvider
import kotlinx.coroutines.launch

class SubjectViewModel(private var app: Application) : BaseViewModel(app) {

    private val repository = SubjectRepository.getInstance(app)
    private val _subjects: LiveData<List<SubjectPackage>> = repository.fetchLiveData()

    val subjects: MediatorLiveData<List<SubjectPackage>> = MediatorLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(subjects) { it.isNullOrEmpty() }

    var filterOption = preferences.subjectFilterOption
        set(value) {
            field = value
            preferences.subjectFilterOption = value
            performFilter(value)
        }

    init {
        subjects.addSource(_subjects) { items ->
            when (filterOption) {
                FilterOption.ALL ->
                    subjects.value = items
                FilterOption.TODAY ->
                    subjects.value = items.filter { it.hasScheduleToday() }
                FilterOption.TOMORROW ->
                    subjects.value = items.filter { it.hasScheduleTomorrow() }
            }
        }
    }

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

    private fun performFilter(option: FilterOption) = when(option) {
        FilterOption.ALL ->
            _subjects.value?.let { subjects.value = it }
        FilterOption.TODAY ->
            _subjects.value?.let { subjects.value = it.filter { subject -> subject.hasScheduleToday() } }
        FilterOption.TOMORROW ->
            _subjects.value?.let { subjects.value = it.filter { subject -> subject.hasScheduleTomorrow() } }
    }

    enum class FilterOption {
        ALL, TODAY, TOMORROW;

        companion object {
            fun parse(value: String): FilterOption {
                return when(value) {
                    ALL.toString() -> ALL
                    TODAY.toString() -> TODAY
                    TOMORROW.toString() -> TOMORROW
                    else -> TODAY
                }
            }
        }
    }

}