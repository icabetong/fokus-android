package com.isaiahvonrundstedt.fokus.features.event.editor

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.dao.ScheduleDAO
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class EventEditorViewModel @Inject constructor(
    private val scheduleDao: ScheduleDAO,
    private val repository: EventRepository
): ViewModel() {

    private val _event: MutableLiveData<Event> = MutableLiveData(Event())
    private val _subject: MutableLiveData<Subject> = MutableLiveData(null)

    val event: LiveData<Event> = _event
    val subject: LiveData<Subject> = _subject

    var schedules: List<Schedule> = emptyList()

    fun getEvent(): Event? {
        return event.value
    }
    fun setEvent(event: Event?) {
        _event.value = event
    }

    fun getSubject(): Subject? {
        return subject.value
    }
    fun setSubject(subject: Subject?) {
        _subject.value = subject
        if (subject != null) {
            fetchSchedulesFromDatabase(subject.subjectID)
            setEventSubjectID(subject.subjectID)
        } else {
            schedules = emptyList()
            setEventSubjectID(null)
        }
    }


    fun getName(): String? {
        return getEvent()?.name
    }
    fun setName(name: String?) {
        val event = getEvent()
        event?.name = name
        setEvent(event)
    }

    fun getSchedule(): ZonedDateTime? {
        return getEvent()?.schedule
    }
    fun setSchedule(schedule: ZonedDateTime?) {
        val event = getEvent()
        event?.schedule = schedule
        setEvent(event)
    }

    fun getLocation(): String? {
        return getEvent()?.location
    }
    fun setLocation(location: String?) {
        val event = getEvent()
        event?.location = location
        setEvent(event)
    }

    fun getEventSubjectID(): String? {
        return getEvent()?.eventID
    }
    fun setEventSubjectID(id: String?) {
        val event = getEvent()
        event?.subject = id
        setEvent(event)
    }

    fun getImportant(): Boolean {
        return getEvent()?.isImportant == true
    }
    fun setImportant(isImportant: Boolean) {
        val event = getEvent()
        event?.isImportant = isImportant
        setEvent(event)
    }

    fun getNotes(): String? {
        return getEvent()?.notes
    }
    fun setNotes(notes: String) {
        val event = getEvent()
        event?.notes = notes
        setEvent(event)
    }

    fun setNextMeetingForDueDate() {
        setSchedule(getNextMeetingForSchedule())
    }

    fun setClassScheduleAsDueDate(schedule: Schedule) {
        setSchedule(schedule.startTime?.let { Schedule.getNearestDateTime(schedule.daysOfWeek, it) })
    }

    private fun getNextMeetingForSchedule(): ZonedDateTime? {
        val currentDate = LocalDate.now()
        val individualDates = mutableListOf<Schedule>()

        // Create new instances of Schedule
        // with individual day of week values
        schedules.forEach {
            it.parseDaysOfWeek().forEach { day ->
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

    private fun fetchSchedulesFromDatabase(id: String) = viewModelScope.launch {
        schedules = scheduleDao.fetchUsingID(id)
    }

    fun insert() = viewModelScope.launch {
        getEvent()?.let {
            repository.insert(it)
        }
    }

    fun update() = viewModelScope.launch {
        getEvent()?.let {
            repository.update(it)
        }
    }
}