package com.isaiahvonrundstedt.fokus.features.subject.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.getIndexByID
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectEditorViewModel @Inject constructor(
    private val repository: SubjectRepository
) : ViewModel() {

    private val _subject: MutableLiveData<Subject> = MutableLiveData(Subject())
    private val _schedules: MutableLiveData<ArrayList<Schedule>> = MutableLiveData(arrayListOf())
    private val _isCodeExists: MutableLiveData<Boolean> = MutableLiveData(false)

    val subject: LiveData<Subject> = _subject
    val schedules: LiveData<ArrayList<Schedule>> = _schedules
    val isCodeExists: LiveData<Boolean> = _isCodeExists

    fun getSubject(): Subject? {
        return subject.value
    }

    fun setSubject(data: Subject?) {
        _subject.value = data
    }

    fun getSchedules(): ArrayList<Schedule> {
        return schedules.value ?: arrayListOf()
    }

    fun setSchedules(items: ArrayList<Schedule>) {
        _schedules.value = items
    }

    fun addSchedule(schedule: Schedule) {
        val items = ArrayList(getSchedules())
        items.add(schedule)
        setSchedules(items)
    }

    fun removeSchedule(schedule: Schedule) {
        val items = ArrayList(getSchedules())
        items.remove(schedule)
        setSchedules(items)
    }

    fun updateSchedule(schedule: Schedule) {
        val items = ArrayList(getSchedules())
        val index: Int = items.getIndexByID(schedule.scheduleID)

        if (index != -1) {
            items[index] = schedule
            setSchedules(items)
        }
    }

    fun checkCodeUniqueness(code: String?) = viewModelScope.launch {
        val result = repository.checkCodeExists(code, getSubject()?.subjectID)
        _isCodeExists.value = !result.contains(getID()) && result.isNotEmpty()
    }

    fun getID(): String? {
        return getSubject()?.subjectID
    }

    fun getCode(): String? {
        return getSubject()?.code
    }

    fun setCode(code: String) {
        if (code == getCode())
            return

        val subject = getSubject()
        subject?.code = code
        setSubject(subject)

    }

    fun getDescription(): String? {
        return getSubject()?.description
    }

    fun setDescription(description: String) {
        if (description == getDescription())
            return

        val subject = getSubject()
        subject?.description = description
        setSubject(subject)
    }

    fun getInstructor(): String? {
        return getSubject()?.instructor
    }
    fun setInstructor(instructor: String?) {
        if (instructor == getInstructor())
            return

        val subject = getSubject()
        subject?.instructor = instructor
        android.util.Log.e("DEBUG", instructor ?: "null")
        setSubject(subject)
    }

    fun getTag(): Subject.Tag? {
        return getSubject()?.tag
    }

    fun setTag(tag: Subject.Tag) {
        val subject = getSubject()
        subject?.tag = tag
        setSubject(subject)
    }

    fun insert() = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        getSubject()?.let {
            repository.insert(it, getSchedules())
        }
    }

    fun update() = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        getSubject()?.let {
            repository.update(it, getSchedules())
        }
    }
}