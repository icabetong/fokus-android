package com.isaiahvonrundstedt.fokus.features.event

import android.app.Application
import androidx.lifecycle.*
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isAfterNow
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isBeforeNow
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.notifications.event.EventNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.widget.event.EventWidgetProvider
import kotlinx.coroutines.launch
import java.time.LocalDate

class EventViewModel(private var app: Application) : BaseViewModel(app) {

    private val repository = EventRepository.getInstance(app)
    private val _events: LiveData<List<EventPackage>> = repository.fetchLiveData()

    val events: MediatorLiveData<List<EventPackage>> = MediatorLiveData()
    val eventsEmpty: LiveData<Boolean> = Transformations.map(events) { it.isNullOrEmpty() }

    val today: LocalDate
        get() = LocalDate.now()

    var selectedDate: LocalDate = today
        set(value) {
            field = value
            events.value = _events.value?.filter { it.event.schedule!!.toLocalDate() == selectedDate }
        }

    init {
        events.addSource(_events) { items ->
            events.value = items.filter { it.event.schedule!!.toLocalDate() == selectedDate }
        }
    }

    fun insert(event: Event) = viewModelScope.launch {
        repository.insert(event)

        if (PreferenceManager(app).eventReminder && event.schedule?.isAfterNow() == true) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .addTag(event.eventID)
                .build()
            workManager.enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
                request)
        }

        EventWidgetProvider.triggerRefresh(app)
    }

    fun remove(event: Event) = viewModelScope.launch {
        repository.remove(event)

        if (event.isImportant)
            notificationService?.cancel(event.eventID, BaseWorker.NOTIFICATION_ID_EVENT)

        workManager.cancelUniqueWork(event.eventID)

        EventWidgetProvider.triggerRefresh(app)
    }

    fun update(event: Event) = viewModelScope.launch {
        repository.update(event)

        if (event.schedule?.isBeforeNow() == true || !event.isImportant)
            notificationService?.cancel(event.eventID, BaseWorker.NOTIFICATION_ID_EVENT)

        if (PreferenceManager(app).eventReminder && event.schedule?.isAfterNow() == true) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .addTag(event.eventID)
                .build()
            workManager.enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
                request)
        }

        EventWidgetProvider.triggerRefresh(app)
    }

    private fun getDates(): List<LocalDate?> {
        return events.value?.map { it.event.schedule!!.toLocalDate() }?.distinct() ?: emptyList()
    }

    fun hasDate(date: LocalDate): Boolean {
        return getDates().contains(date)
    }

}