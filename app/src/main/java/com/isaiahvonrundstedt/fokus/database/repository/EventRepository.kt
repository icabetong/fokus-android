package com.isaiahvonrundstedt.fokus.database.repository

import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.dao.EventDAO
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import javax.inject.Inject

class EventRepository @Inject constructor(private val events: EventDAO) {

    fun fetchLiveData(): LiveData<List<EventPackage>> = events.fetchLiveData()

    suspend fun fetch(): List<EventPackage> = events.fetch()

    suspend fun fetchCore(): List<Event> = events.fetchCore()

    suspend fun insert(event: Event) {
        events.insert(event)
    }

    suspend fun remove(event: Event) {
        events.remove(event)
    }

    suspend fun update(event: Event) {
        events.update(event)
    }
}