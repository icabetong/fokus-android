package com.isaiahvonrundstedt.fokus.features.event

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.core.work.EventNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class EventViewModel(private var app: Application): BaseViewModel(app) {

    private var dataStore = EventRepository(app)
    private var workManager = WorkManager.getInstance(app)
    private var items: LiveData<List<Event>>? = dataStore.fetch()

    fun fetch(): LiveData<List<Event>>? = items

    fun insert(event: Event) = viewModelScope.launch {
        dataStore.insert(event)

        if (PreferenceManager(app).remindWhenDue) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }

    fun remove(event: Event) = viewModelScope.launch {
        dataStore.remove(event)
        workManager.cancelUniqueWork(event.id)
    }

    fun update(event: Event) = viewModelScope.launch {
        dataStore.update(event)

        val currentTime = DateTime.now()
        if (PreferenceManager(app).remindWhenDue && event.schedule!!.isAfter(currentTime)) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }
}