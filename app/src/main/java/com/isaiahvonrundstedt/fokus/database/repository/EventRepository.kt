package com.isaiahvonrundstedt.fokus.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.isaiahvonrundstedt.fokus.database.dao.EventDAO
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import com.isaiahvonrundstedt.fokus.features.widget.event.EventWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EventRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val events: EventDAO
) {

    fun fetchLiveData(): LiveData<List<EventPackage>> = events.fetchLiveData()

    suspend fun fetch(): List<EventPackage> = events.fetch()

    suspend fun fetchCore(): List<Event> = events.fetchCore()

    suspend fun insert(event: Event) {
        events.insert(event)

        EventWidgetProvider.triggerRefresh(context)
    }

    suspend fun remove(event: Event) {
        events.remove(event)

        EventWidgetProvider.triggerRefresh(context)
    }

    suspend fun update(event: Event) {
        events.update(event)

        EventWidgetProvider.triggerRefresh(context)
    }
}