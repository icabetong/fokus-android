package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.event.Event

class EventRepository (app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var events = database?.events()

    fun fetch(): LiveData<List<Event>>? = events?.fetch()

    suspend fun insert(event: Event) {
        events?.insert(event)
    }

    suspend fun remove(event: Event) {
        events?.remove(event)
    }

    suspend fun update(event: Event) {
        events?.update(event)
    }
}