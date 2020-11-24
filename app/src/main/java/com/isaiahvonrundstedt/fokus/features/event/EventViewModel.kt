package com.isaiahvonrundstedt.fokus.features.event

import android.app.NotificationManager
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isAfterNow
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isBeforeNow
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.notifications.event.EventNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class EventViewModel @ViewModelInject constructor(
    private val repository: EventRepository,
    private val preferenceManager: PreferenceManager,
    private val workManager: WorkManager,
    private val notificationManager: NotificationManager,
    @Assisted
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _events: LiveData<List<EventPackage>> = repository.fetchLiveData()

    val dates: MediatorLiveData<List<LocalDate>> = MediatorLiveData()
    val events: MediatorLiveData<List<EventPackage>> = MediatorLiveData()
    val eventsEmpty: LiveData<Boolean> = Transformations.map(events) { it.isNullOrEmpty() }

    val today: LocalDate
        get() = LocalDate.now()
    val currentMonth: YearMonth
        get() = YearMonth.now()

    var selectedDate: LocalDate = today
        set(value) {
            field = value
            events.value = _events.value?.filter { it.event.schedule!!.toLocalDate() == selectedDate }
        }

    var startMonth: YearMonth = currentMonth.minusMonths(10)
    var endMonth: YearMonth = currentMonth.plusMonths(10)

    init {
        events.addSource(_events) { items ->
            events.value = items.filter { it.event.schedule!!.toLocalDate() == selectedDate }
        }
        dates.addSource(_events) { items ->
            dates.value = items.map { it.event.schedule!!.toLocalDate() }.distinct()
        }
    }

    fun insert(event: Event) = viewModelScope.launch {
        repository.insert(event)

        if (preferenceManager.eventReminder && event.schedule?.isAfterNow() == true) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .addTag(event.eventID)
                .build()
            workManager.enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
                request)
        }

        //EventWidgetProvider.triggerRefresh(app)
    }

    fun remove(event: Event) = viewModelScope.launch {
        repository.remove(event)

        if (event.isImportant)
            notificationManager?.cancel(event.eventID, BaseWorker.NOTIFICATION_ID_EVENT)

        workManager.cancelUniqueWork(event.eventID)

        //EventWidgetProvider.triggerRefresh(app)
    }

    fun update(event: Event) = viewModelScope.launch {
        repository.update(event)

        if (event.schedule?.isBeforeNow() == true || !event.isImportant)
            notificationManager.cancel(event.eventID, BaseWorker.NOTIFICATION_ID_EVENT)

        if (preferenceManager.eventReminder && event.schedule?.isAfterNow() == true) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .addTag(event.eventID)
                .build()
            workManager.enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
                request)
        }

        //EventWidgetProvider.triggerRefresh(app)
    }

}