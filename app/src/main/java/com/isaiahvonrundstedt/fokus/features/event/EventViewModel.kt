package com.isaiahvonrundstedt.fokus.features.event

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.repository.EventRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewModel

class EventViewModel(app: Application): BaseViewModel(app) {

    private var dataStore = EventRepository(app)
    private var items: LiveData<List<Event>>? = dataStore.fetch()

    fun fetch(): LiveData<List<Event>>? = items

}