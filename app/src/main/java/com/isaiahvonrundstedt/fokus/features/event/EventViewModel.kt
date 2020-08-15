package com.isaiahvonrundstedt.fokus.features.event

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
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
    private var items: LiveData<List<EventPackage>>? = repository.fetch()

    fun fetch(): LiveData<List<EventPackage>>? = items

    fun insert(event: Event) = viewModelScope.launch {
        repository.insert(event)

        if (PreferenceManager(app).eventReminder && event.schedule!!.isAfterNow) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }

        EventWidgetProvider.triggerRefresh(app)
    }

    fun remove(event: Event) = viewModelScope.launch {
        repository.remove(event)

        if (event.isImportant)
            notificationManager?.cancel(event.eventID, BaseWorker.NOTIFICATION_ID_EVENT)

        workManager.cancelUniqueWork(event.eventID)

        EventWidgetProvider.triggerRefresh(app)
    }

    fun update(event: Event) = viewModelScope.launch {
        repository.update(event)

        if (event.schedule?.isBeforeNow == true || !event.isImportant)
            notificationManager?.cancel(event.eventID, BaseWorker.NOTIFICATION_ID_EVENT)

        if (PreferenceManager(app).eventReminder && event.schedule!!.isAfterNow) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }

        EventWidgetProvider.triggerRefresh(app)
    }
}