package com.isaiahvonrundstedt.fokus.features.task.editor

import android.content.ClipboardManager
import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.isaiahvonrundstedt.fokus.database.dao.ScheduleDAO
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime

class TaskEditorViewModel @ViewModelInject constructor(
    private val clipboardManager: ClipboardManager,
    private val scheduleDao: ScheduleDAO,
    @ApplicationContext
    private val context: Context,
    @Assisted savedStateHandle: SavedStateHandle
): ViewModel() {

    var task: Task? = Task()
        set(value) {
            field = value
            _taskObservable.value = value
        }
    var attachments = arrayListOf<Attachment>()
        set(value) {
            field = value
            _attachmentObservable.value = value
        }
    var subject: Subject? = null
        set(value) {
            field = value
            _subjectObservable.value = value
            if (value != null) {
                task?.subject = value.subjectID
                fetchSchedulesFromDatabase(value.subjectID)
            } else {
                task?.subject = null
                schedules = emptyList()
            }
        }
    var schedules = listOf<Schedule>()

    private val _taskObservable = MutableLiveData<Task?>(task)
    private val _attachmentObservable = MutableLiveData<List<Attachment>>(emptyList())
    private val _subjectObservable = MutableLiveData<Subject?>(subject)

    val taskObservable: LiveData<Task?> = _taskObservable
    val attachmentObservable: LiveData<List<Attachment>> = _attachmentObservable
    val subjectObservable: LiveData<Subject?> = _subjectObservable

    fun setTaskName(name: String?) { task?.name = name }
    fun getTaskName(): String? = task?.name
    fun hasTaskName(): Boolean = task?.name?.isNotEmpty() == true

    fun setDueDate(dueDate: ZonedDateTime?) { task?.dueDate = dueDate }
    fun getDueDate(): ZonedDateTime? = task?.dueDate
    fun hasDueDate(): Boolean = task?.hasDueDate() == true
    fun getFormattedDueDate(): String = task?.formatDueDate(context) ?: ""

    fun hasAttachmentWithFile(): Boolean = attachments.any { it.type != Attachment.TYPE_WEBSITE_LINK }
    fun setIsImportant(isImportant: Boolean) { task?.isImportant = isImportant }
    fun setIsFinished(isFinished: Boolean) { task?.isFinished = isFinished }
    fun setNotes(notes: String?) { task?.notes = notes }

    fun addAttachment(item: Attachment) {
        attachments.add(item)
        _attachmentObservable.value = attachments
    }
    fun removeAttachment(item: Attachment) {
        attachments.remove(item)
        _attachmentObservable.value = attachments
    }

    fun fetchRecentItemFromClipboard(): String?
            = clipboardManager.primaryClip?.getItemAt(0)?.text.toString()

    fun setNextMeetingForDueDate() {
        task?.dueDate = getDateTimeForNextMeeting()
    }

    fun setClassScheduleAsDueDate(schedule: Schedule) {
        task?.dueDate = schedule.startTime?.let {
            Schedule.getNearestDateTime(schedule.daysOfWeek, it)
        }
    }

    private fun getDateTimeForNextMeeting(): ZonedDateTime? {
        val currentDate = LocalDate.now()
        val individualDates = mutableListOf<Schedule>()

        // Create new instance of schedule with
        // one day of week each
        schedules.forEach {
            it.getDays().forEach { day ->
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

    private fun fetchSchedulesFromDatabase(id: String) = viewModelScope.launch {
        schedules = scheduleDao.fetchUsingID(id)
    }

}