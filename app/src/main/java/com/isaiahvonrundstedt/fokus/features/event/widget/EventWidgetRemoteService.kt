package com.isaiahvonrundstedt.fokus.features.event.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.event.EventResource
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class EventWidgetRemoteService : RemoteViewsService() {

    private fun fetch(): List<EventResource>? {
        val events = AppDatabase.getInstance(applicationContext)?.events()
        var items: List<EventResource>? = emptyList()
        runBlocking {
            val job = async { events?.fetch() }
            items = job.await()
        }
        return items
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        val items = mutableListOf<EventResource>()
        fetch()?.forEach {
            if (it.event.isToday())
                items.add(it)
        }

        return EventWidgetRemoteView(applicationContext, items)
    }
}