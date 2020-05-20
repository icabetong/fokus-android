package com.isaiahvonrundstedt.fokus.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.event.Event

class EventRepository (app: Application) {

    private var database = AppDatabase.getInstance(app)
    private var events = database?.events()

    fun fetch(): LiveData<List<Event>>? = events?.fetch()

}