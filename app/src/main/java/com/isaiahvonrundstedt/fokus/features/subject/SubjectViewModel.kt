package com.isaiahvonrundstedt.fokus.features.subject

import androidx.lifecycle.*
import com.isaiahvonrundstedt.fokus.components.enums.SortDirection
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: SubjectRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _subjects: LiveData<List<SubjectPackage>> = repository.fetchLiveData()

    val subjects: MediatorLiveData<List<SubjectPackage>> = MediatorLiveData()
    val isEmpty: LiveData<Boolean> = Transformations.map(subjects) { it.isNullOrEmpty() }

    var constraint: Constraint = preferenceManager.subjectConstraint
        set(value) {
            field = value
            preferenceManager.subjectConstraint = value
            if (constraint == Constraint.ALL)
                sort = Sort.CODE
            rearrange(value, sort, direction)
        }

    var sort: Sort = preferenceManager.subjectSort
        set(value) {
            field = value
            preferenceManager.subjectSort = value
            rearrange(constraint, value, direction)
        }

    var direction: SortDirection = preferenceManager.subjectSortDirection
        set(value) {
            field = value
            preferenceManager.subjectSortDirection = value
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

    fun insert(subject: Subject, schedules: List<Schedule>) = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        repository.insert(subject, schedules)
    }

    fun remove(subject: Subject) = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        repository.remove(subject)
    }

    fun update(subject: Subject, schedules: List<Schedule> = emptyList()) = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        repository.update(subject, schedules)
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
                    Sort.SCHEDULE -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { it.hasScheduleToday() }
                                    .sortedBy { it.getScheduleToday()?.startTime }
                            SortDirection.DESCENDING ->
                                items.filter { it.hasScheduleToday() }
                                    .sortedByDescending { it.getScheduleToday()?.startTime }
                        }
                    }
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
                    Sort.SCHEDULE -> {
                        when (direction) {
                            SortDirection.ASCENDING ->
                                items.filter { it.hasScheduleTomorrow() }
                                    .sortedBy { it.getScheduleTomorrow()?.startTime }
                            SortDirection.DESCENDING ->
                                items.filter { it.hasScheduleTomorrow() }
                                    .sortedByDescending { it.getScheduleTomorrow()?.startTime }
                        }
                    }
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