package com.isaiahvonrundstedt.fokus.features.event.editor

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.isaiahvonrundstedt.fokus.database.dao.ScheduleDAO
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime

class EventEditorViewModel @ViewModelInject constructor(
    private val scheduleDao: ScheduleDAO,
    @ApplicationContext private val context: Context,
    @Assisted val savedStateHandle: SavedStateHandle
): ViewModel() {

    var event: Event? = Event()
        set(value) {
            field = value
            _eventObservable.value = value
        }
    var subject: Subject? = null
        set(value) {
            field = value
            _subjectObservable.value = value
            if (value != null) {
                event?.subject = value.subjectID
                fetchSchedulesFromDatabase(value.subjectID)
            } else {
                event?.subject = null
                schedules = emptyList()
            }
        }
    var schedules: List<Schedule> = emptyList()

    private val _eventObservable = MutableLiveData<Event?>(event)
    private val _subjectObservable = MutableLiveData<Subject?>(subject)

    val eventObservable: LiveData<Event?> = _eventObservable
    val subjectObservable: LiveData<Subject?> = _subjectObservable

    fun setEventName(name: String?) { event?.name = name }
    fun getEventName(): String? = event?.name
    fun hasEventName(): Boolean = event?.name?.isNotEmpty() == true

    fun setSchedule(schedule: ZonedDateTime?) { event?.schedule = schedule }
    fun getSchedule(): ZonedDateTime? = event?.schedule
    fun hasSchedule(): Boolean = event?.schedule != null
    fun getFormattedSchedule(): String = event?.formatSchedule(context) ?: ""

    fun setLocation(location: String?) { event?.location = location }
    fun hasLocation(): Boolean = event?.location?.isNotEmpty() == true

    fun setIsImportant(isImportant: Boolean) { event?.isImportant = isImportant }
    fun setNotes(notes: String?) { event?.notes = notes }

    fun setNextMeetingForDueDate() {
        event?.schedule = getNextMeetingForSchedule()
    }

    fun setClassScheduleAsDueDate(schedule: Schedule) {
        event?.schedule = schedule.startTime?.let { Schedule.getNearestDateTime(schedule.daysOfWeek, it) }
    }

    private fun getNextMeetingForSchedule(): ZonedDateTime? {
        val currentDate = LocalDate.now()
        val individualDates = mutableListOf<Schedule>()

        // Create new instances of Schedule
        // with individual day of week values
        schedules.forEach {
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

    private fun fetchSchedulesFromDatabase(id: String) = viewModelScope.launch {
        schedules = scheduleDao.fetchUsingID(id)
    }
}