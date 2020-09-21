package com.isaiahvonrundstedt.fokus.features.task.editor

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime

class TaskEditorViewModel(app: Application): BaseViewModel(app) {

    private var database = AppDatabase.getInstance(applicationContext)

    private var _items: ArrayList<Attachment> = ArrayList()

    private var _task: MutableLiveData<Task> = MutableLiveData(Task())
    private var _attachments: MutableLiveData<List<Attachment>> = MutableLiveData(emptyList())
    private var _subject: MutableLiveData<Subject?> = MutableLiveData(null)
    private var _schedules: MutableLiveData<List<Schedule>> = MutableLiveData(emptyList())

    private var _hasTaskName: LiveData<Boolean> =
        Transformations.map(_task) { !it.name.isNullOrEmpty() }
    private var _hasDueDate: LiveData<Boolean> =
        Transformations.map(_task) { it.hasDueDate() }

    val task: LiveData<Task> = _task
    val attachments: LiveData<List<Attachment>> = _attachments
    val subject: LiveData<Subject?> = _subject
    val schedules: LiveData<List<Schedule>> = _schedules

    val hasTaskName: Boolean
        get() = _hasTaskName.value ?: false
    val hasDueDate: Boolean
        get() = _hasDueDate.value ?: false

    fun getTask(): Task? { return _task.value }
    fun setTask(task: Task?) { _task.value = task }

    fun getTaskDueDate(): ZonedDateTime? { return _task.value?.dueDate }

    fun getAttachments(): List<Attachment> { return _attachments.value ?: emptyList() }
    fun setAttachments(attachments: List<Attachment>?) { _attachments.value = attachments }

    fun addAttachment(attachment: Attachment) {
        _items.add(attachment)
        _attachments.value = _items
    }
    fun removeAttachment(attachment: Attachment) {
        _items.remove(attachment)
        _attachments.value = _items
    }

    fun getSubject(): Subject? { return _subject.value }
    fun setSubject(subject: Subject?) = viewModelScope.launch {
        if (subject != null) {
            _task.value?.subject = subject.subjectID
            _subject.value = subject
            _schedules.value = database.schedules().fetchUsingID(subject.subjectID)
        } else {
            _task.value?.subject = null
            _subject.value = null
            _schedules.value = emptyList()
        }
    }

    fun getSchedules(): List<Schedule> { return _schedules.value ?: emptyList() }
    fun setSchedules(schedules: List<Schedule>) { _schedules.value = schedules }

    fun setNextMeetingForDueDate() {
        getTask()?.dueDate = getDateTimeForNextMeeting()
    }

    fun setClassScheduleAsDueDate(schedule: Schedule) {
        getTask()?.dueDate = schedule.startTime?.let {
            Schedule.getNearestDateTime(schedule.daysOfWeek, it)
        }
    }

    private fun getDateTimeForNextMeeting(): ZonedDateTime? {
        val currentDate = LocalDate.now()
        val individualDates = mutableListOf<Schedule>()

        // Create new instance of schedule with
        // one day of week each
        getSchedules().forEach {
            it.getDaysAsList().forEach { day ->
                val newSchedule = Schedule(startTime = it.startTime,
                    endTime = it.endTime)
                newSchedule.daysOfWeek = day
                individualDates.add(newSchedule)
            }
        }

        // Map the schedules to their respective
        // dateTime instances
        val dates = individualDates.map {
            it.startTime?.let { time -> Schedule.getNearestDateTime(it.daysOfWeek, time) }

        }
        if (dates.isEmpty())
            return null

        // Get the nearest date
        var targetDate = dates[0]
        dates.forEach {
            if (currentDate.isAfter(it?.toLocalDate()) && targetDate?.isBefore(it) == true)
                targetDate = it
        }

        return targetDate
    }

}