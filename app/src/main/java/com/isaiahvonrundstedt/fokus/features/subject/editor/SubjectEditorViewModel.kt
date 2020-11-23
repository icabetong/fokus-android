package com.isaiahvonrundstedt.fokus.features.subject.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.getIndexByID
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject

class SubjectEditorViewModel: ViewModel() {

    var subject: Subject? = Subject()
        set(value) {
            field = value
            _subjectObservable.value = value
        }
    var schedules = arrayListOf<Schedule>()
        set(value) {
            field = value
            _schedulesObservable.value = value
        }

    private val _subjectObservable = MutableLiveData<Subject?>(subject)
    private val _schedulesObservable = MutableLiveData<List<Schedule>>(schedules)

    val subjectObservable: LiveData<Subject?> = _subjectObservable
    val schedulesObservable: LiveData<List<Schedule>> = _schedulesObservable

    fun hasSubjectCode(): Boolean = subject?.code?.isNotEmpty() == true
    fun setSubjectCode(code: String?) { subject?.code = code }
    fun getSubjectCode(): String? = subject?.code

    fun hasDescription(): Boolean = subject?.description?.isNotEmpty() == true
    fun setDescription(description: String?) { subject?.description = description }
    fun getDescription(): String? = subject?.description

    fun hasSchedules(): Boolean = schedules.isNotEmpty()

    fun setTag(tag: Subject.Tag) { subject?.tag = tag }
    fun getTag(): Subject.Tag? = subject?.tag

    fun addSchedule(item: Schedule) {
        schedules.add(item)
        _schedulesObservable.value = schedules
    }
    fun removeSchedule(item: Schedule) {
        schedules.remove(item)
        _schedulesObservable.value = schedules
    }
    fun updateSchedule(item: Schedule) {
        val index: Int = schedules.getIndexByID(item.scheduleID)

        if (index != -1) {
            schedules[index] = item
            _schedulesObservable.value = schedules
        }
    }
}