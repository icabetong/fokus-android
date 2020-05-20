package com.isaiahvonrundstedt.fokus.features.event

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel
import kotlinx.coroutines.launch

class EventViewModel(app: Application): BaseViewModel(app) {

    private var dataStore = EventRepository(app)
    private var items: LiveData<List<Event>>? = dataStore.fetch()

    fun fetch(): LiveData<List<Event>>? = items

    fun insert(event: Event) = viewModelScope.launch {
        dataStore.insert(event)
    }

    fun remove(event: Event) = viewModelScope.launch {
        dataStore.remove(event)
    }

    fun update(event: Event) = viewModelScope.launch {
        dataStore.update(event)
    }
}