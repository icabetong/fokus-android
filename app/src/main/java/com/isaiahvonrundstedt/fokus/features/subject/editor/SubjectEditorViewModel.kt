package com.isaiahvonrundstedt.fokus.features.subject.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.isaiahvonrundstedt.fokus.components.extensions.android.notifyObservers
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.getIndexByID
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toArrayList
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject

class SubjectEditorViewModel: ViewModel() {

    private var _subject: MutableLiveData<Subject> = MutableLiveData(Subject())
    private var _schedules: MutableLiveData<ArrayList<Schedule>> = MutableLiveData(ArrayList())

    private val _hasSubjectCode: LiveData<Boolean> =
        Transformations.map(_subject) { !it.code.isNullOrEmpty() }
    private val _hasDescription: LiveData<Boolean> =
        Transformations.map(_subject) { !it.description.isNullOrEmpty() }
    private val _hasSchedules: LiveData<Boolean> =
        Transformations.map(_schedules) { it.isNotEmpty() }

    val subject: LiveData<Subject> = _subject
    val schedules: LiveData<List<Schedule>> = Transformations.map(_schedules) { it.toList() }

    fun getSubject(): Subject? {
        return _subject.value
    }
    fun setSubject(subject: Subject?) {
        _subject.value = subject
    }

    fun getSchedules(): List<Schedule> {
        return _schedules.value ?: emptyList()
    }
    fun setSchedules(schedules: List<Schedule>?) {
        _schedules.value = schedules?.toArrayList()
    }
    fun addSchedule(schedule: Schedule) {
        _schedules.value?.add(schedule)
        _schedules.notifyObservers()
    }
    fun removeSchedule(schedule: Schedule) {
        _schedules.value?.remove(schedule)
        _schedules.notifyObservers()
    }
    fun updateSchedule(schedule: Schedule) {
        val index: Int = _schedules.value?.getIndexByID(schedule.scheduleID) ?: -1

        if (index != -1) {
            _schedules.value?.set(index, schedule)
            _schedules.notifyObservers()
        }
    }

    val hasSubjectCode: Boolean
        get() = _hasSubjectCode.value ?: false
    val hasDescription: Boolean
        get() = _hasDescription.value ?: false
    val hasSchedules: Boolean
        get() = _hasSchedules.value ?: false
}