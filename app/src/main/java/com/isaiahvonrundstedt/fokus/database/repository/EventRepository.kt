package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import org.joda.time.DateTime

class EventRepository private constructor(app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var events = database.events()

    companion object {
        private var instance: EventRepository? = null

        fun getInstance(app: Application): EventRepository {
            if (instance == null) {
                synchronized(EventRepository::class) {
                    instance = EventRepository(app)
                }
            }
            return instance!!
        }
    }

    fun fetch(isAfterNow: Boolean = true): LiveData<List<EventPackage>> {
        return if (isAfterNow)
         events.fetchLiveData(DateTimeConverter.fromDateTime(DateTime.now()))
        else events.fetchLiveDataPrevious(DateTimeConverter.fromDateTime(DateTime.now()))
    }

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