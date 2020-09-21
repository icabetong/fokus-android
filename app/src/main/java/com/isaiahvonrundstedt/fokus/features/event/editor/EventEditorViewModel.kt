package com.isaiahvonrundstedt.fokus.features.event.editor

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime

class EventEditorViewModel(app: Application): BaseViewModel(app) {

    private var database = AppDatabase.getInstance(applicationContext)

    private var _event: MutableLiveData<Event> = MutableLiveData(Event())
    private var _subject: MutableLiveData<Subject?> = MutableLiveData(null)
    private var _schedules: MutableLiveData<List<Schedule>> = MutableLiveData(emptyList())

    val event: LiveData<Event> = _event
    val subject: LiveData<Subject?> = _subject
    val schedules: LiveData<List<Schedule>> = _schedules

    val hasEventName: Boolean
        get() = getEvent()?.name?.isNotEmpty() == true
    val hasLocation: Boolean
        get() = getEvent()?.location?.isNotEmpty() == true
    val hasSchedule: Boolean
        get() = getEvent()?.schedule != null

    fun getEvent(): Event? { return _event.value }
    fun setEvent(event: Event?) { _event.value = event }

    fun getEventSchedule(): ZonedDateTime? { return _event.value?.schedule }

    fun getSubject(): Subject? { return _subject.value }
    fun setSubject(subject: Subject?) = viewModelScope.launch {
        _subject.value = subject
        if (subject != null) {
            _event.value?.subject = subject.subjectID
            _subject.value = subject
            _schedules.value = database.schedules().fetchUsingID(subject.subjectID)
        } else {
            _event.value?.subject = null
            _subject.value = null
            _schedules.value = emptyList()
        }
    }

    fun getSchedules(): List<Schedule> { return _schedules.value ?: emptyList() }
    fun setSchedules(schedules: List<Schedule>) { _schedules.value = schedules }

    fun setNextMeetingForDueDate() {
        getEvent()?.schedule = getNextMeetingForSchedule()
    }

    fun setClassScheduleAsDueDate(schedule: Schedule) {
        getEvent()?.schedule = schedule.startTime?.let { Schedule.getNearestDateTime(schedule.daysOfWeek, it) }
    }

    private fun getNextMeetingForSchedule(): ZonedDateTime? {
        val currentDate = LocalDate.now()
        val individualDates = mutableListOf<Schedule>()

        // Create new instances of Schedule
        // with individual day of week values
        getSchedules().forEach {
            it.getDaysAsList().forEach { day ->
                val newSchedule = Schedule(startTime = it.startTime,
                    endTime = it.endTime)
                newSchedule.daysOfWeek = day
                individualDates.add(newSchedule)
            }
        }

        // Map the schedule instances to
        // a ZonedDateTime instance
        val dates = individualDates.map { it.startTime?.let { time -> Schedule.getNearestDateTime(it.daysOfWeek, time) } }
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