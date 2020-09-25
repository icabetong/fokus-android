package com.isaiahvonrundstedt.fokus.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventPackage

class EventRepository private constructor(context: Context) {

    private var database = AppDatabase.getInstance(context)
    private var events = database.events()

    companion object {
        private var instance: EventRepository? = null

        fun getInstance(context: Context): EventRepository {
            if (instance == null) {
                synchronized(EventRepository::class) {
                    instance = EventRepository(context)
                }
            }
            return instance!!
        }
    }

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