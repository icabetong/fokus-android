package com.isaiahvonrundstedt.fokus.features.event

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.core.work.event.EventNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class EventViewModel(private var app: Application): BaseViewModel(app) {

    private var repository = EventRepository.getInstance(app)
    private var workManager = WorkManager.getInstance(app)
    private var items: LiveData<List<Event>>? = repository.fetch()

    fun fetch(): LiveData<List<Event>>? = items

    fun insert(event: Event) = viewModelScope.launch {
        repository.insert(event)

        if (PreferenceManager(app).eventReminder) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }

    fun remove(event: Event) = viewModelScope.launch {
        repository.remove(event)
        workManager.cancelUniqueWork(event.id)
    }

    fun update(event: Event) = viewModelScope.launch {
        repository.update(event)

        Log.e("DEBUG", "VIEWMODEL")
        if (PreferenceManager(app).eventReminder && event.schedule!!.isBeforeNow) {
            Log.e("DEBUG", "CONDITION")
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .build()
            workManager.enqueue(request)
        }
    }
}