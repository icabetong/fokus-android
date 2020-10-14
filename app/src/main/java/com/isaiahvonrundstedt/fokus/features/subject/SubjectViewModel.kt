package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import com.isaiahvonrundstedt.fokus.components.enums.SortDirection
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

    var constraint: Constraint = preferences.subjectConstraint
        set(value) {
            field = value
            preferences.subjectConstraint = value
            rearrange(value, sort, direction)
        }

    var sort: Sort = preferences.subjectSort
        set(value) {
            field = value
            preferences.subjectSort = value
            rearrange(constraint, value, direction)
        }

    var direction: SortDirection = preferences.subjectSortDirection
        set(value) {
            field = value
            preferences.subjectSortDirection = value
            rearrange(constraint, sort, value)
        }

    init {
        subjects.addSource(_subjects) { items ->
            when (constraint) {
                Constraint.ALL ->
                    subjects.value = items
                Constraint.TODAY ->
                    subjects.value = items.filter { it.hasScheduleToday() }
                Constraint.TOMORROW ->
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

    private fun rearrange(filter: Constraint, sort: Sort, direction: SortDirection)
            = when (filter) {
        Constraint.ALL -> {
            _subjects.value?.let { items ->
                subjects.value = when(sort) {
                    Sort.CODE -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.sortedBy { it.subject.code }
                            SortDirection.DESCENDING ->
                                items.sortedByDescending { it.subject.code }
                        }
                    }
                    Sort.DESCRIPTION -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.sortedBy { it.subject.description }
                            SortDirection.DESCENDING ->
                                items.sortedByDescending { it.subject.description }
                        }
                    }
                    Sort.SCHEDULE -> items.sortedBy { it.subject.code }
                }
            }
        }
        Constraint.TODAY -> {
            _subjects.value?.let { items ->
                subjects.value = when(sort) {
                    Sort.CODE -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { it.hasScheduleToday() }
                                    .sortedBy { it.subject.code }
                            SortDirection.DESCENDING ->
                                items.filter { it.hasScheduleToday() }
                                    .sortedByDescending { it.subject.code }
                        }
                    }
                    Sort.DESCRIPTION -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { it.hasScheduleToday() }
                                    .sortedBy { it.subject.description }
                            SortDirection.DESCENDING ->
                                items.filter { it.hasScheduleToday() }
                                    .sortedByDescending { it.subject.description }
                        }
                    }
                    Sort.SCHEDULE -> { items }
                }
            }
        }
        Constraint.TOMORROW -> {
            _subjects.value?.let { items ->
                subjects.value = when(sort) {
                    Sort.CODE -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { it.hasScheduleTomorrow() }
                                    .sortedBy { it.subject.code }
                            SortDirection.DESCENDING ->
                                items.filter { it.hasScheduleTomorrow() }
                                    .sortedByDescending { it.subject.code }
                        }
                    }
                    Sort.DESCRIPTION -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { it.hasScheduleTomorrow() }
                                    .sortedBy { it.subject.description }
                            SortDirection.DESCENDING ->
                                items.filter { it.hasScheduleTomorrow() }
                                    .sortedByDescending { it.subject.description }
                        }
                    }
                    Sort.SCHEDULE -> { items }
                }
            }
        }
    }

    enum class Sort {
        CODE, DESCRIPTION, SCHEDULE;

        companion object {
            fun parse(value: String): Sort {
                return when(value) {
                    CODE.toString() -> CODE
                    DESCRIPTION.toString() -> DESCRIPTION
                    SCHEDULE.toString() -> SCHEDULE
                    else -> CODE
                }
            }
        }
    }

    enum class Constraint {
        ALL, TODAY, TOMORROW;

        companion object {
            fun parse(value: String): Constraint {
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