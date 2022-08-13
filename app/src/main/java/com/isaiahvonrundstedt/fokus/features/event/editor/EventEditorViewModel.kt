package com.isaiahvonrundstedt.fokus.features.event.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.database.dao.ScheduleDAO
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class EventEditorViewModel @Inject constructor(
    private val scheduleDao: ScheduleDAO,
    private val repository: EventRepository
) : ViewModel() {

    private val _event: MutableLiveData<Event> = MutableLiveData(Event())
    private val _subject: MutableLiveData<Subject> = MutableLiveData(null)
    private val _isNameTaken: MutableLiveData<Boolean> = MutableLiveData(false)

    val event: LiveData<Event> = _event
    val subject: LiveData<Subject> = _subject
    val isNameTaken: LiveData<Boolean> = _isNameTaken

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

    fun checkNameUniqueness(name: String? = null, schedule: ZonedDateTime? = null) =
        viewModelScope.launch {
            val result = repository.checkNameUniqueness(
                name ?: getName(),
                DateTimeConverter.fromLocalDate(
                    schedule?.toLocalDate() ?: getSchedule()?.toLocalDate()
                ),
                getEvent()?.eventID
            )
            _isNameTaken.value = result.isNotEmpty()
        }

    fun getID(): String? {
        return getEvent()?.eventID
    }

    fun getName(): String? {
        return getEvent()?.name
    }

    fun setName(name: String?) {
        // Check if the same value is being set
        if (name == getName())
            return

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
        // Check if the same value is being set
        if (location == getLocation())
            return

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
        if (notes == getNotes())
            return

        val event = getEvent()
        event?.notes = notes
        setEvent(event)
    }

    fun setNextMeetingForDueDate() {
        setSchedule(getNextMeetingForSchedule())
    }

    fun setClassScheduleAsDueDate(schedule: Schedule) {
        setSchedule(schedule.startTime?.let {
            Schedule.getNearestDateTime(
                schedule.daysOfWeek,
                it
            )
        })
    }

    private fun getNextMeetingForSchedule(): ZonedDateTime? {
        val currentDate = LocalDate.now()
        val individualDates = mutableListOf<Schedule>()

        // Create new instances of Schedule
        // with individual day of week values
        schedules.forEach {
            it.parseDaysOfWeek().forEach { day ->
                val newSchedule = Schedule(
                    startTime = it.startTime,
                    endTime = it.endTime
                )
                newSchedule.daysOfWeek = day
                individualDates.add(newSchedule)
            }
        }

        // Map the schedule instances to
        // a ZonedDateTime instance
        val dates = individualDates.map {
            it.startTime?.let { time ->
                Schedule.getNearestDateTime(
                    it.daysOfWeek,
                    time
                )
            }
        }
        if (dates.isEmpty())
            return null

        return dates.singleOrNull {
            currentDate.isAfter(it?.toLocalDate()) || currentDate.isEqual(it?.toLocalDate())
        } ?: dates[0]
    }

    private fun fetchSchedulesFromDatabase(id: String) = viewModelScope.launch {
        schedules = scheduleDao.fetchUsingID(id)
    }

    fun insert() = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        getEvent()?.let {
            repository.insert(it)
        }
    }

    fun update() = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        getEvent()?.let {
            repository.update(it)
        }
    }
}