package com.isaiahvonrundstedt.fokus.features.event

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.core.work.event.EventNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import com.isaiahvonrundstedt.fokus.features.widget.event.EventWidgetProvider
import kotlinx.coroutines.launch

class EventViewModel(private var app: Application) : BaseViewModel(app) {

    private var repository = EventRepository.getInstance(app)

    val futureEvents: LiveData<List<EventPackage>> = repository.fetch()
    val noFutureEvents: LiveData<Boolean> = Transformations.map(futureEvents) { it.isEmpty() }

    val previousEvents: LiveData<List<EventPackage>> = repository.fetch(false)
    val noPreviousEvents: LiveData<Boolean> = Transformations.map(previousEvents) { it.isEmpty() }

    fun insert(event: Event) = viewModelScope.launch {
        repository.insert(event)

        if (PreferenceManager(app).eventReminder && event.schedule!!.isAfterNow) {
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

        if (event.schedule?.isBeforeNow == true || !event.isImportant)
            notificationService?.cancel(event.eventID, BaseWorker.NOTIFICATION_ID_EVENT)

        if (PreferenceManager(app).eventReminder && event.schedule!!.isAfterNow) {
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
}