package com.isaiahvonrundstedt.fokus.features.subject.widget

import android.content.Intent
import android.util.Log
import android.widget.RemoteViewsService
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.subject.SubjectResource
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class SubjectWidgetRemoteService: RemoteViewsService() {

    private fun fetch(): List<SubjectResource>? {
        val subjects = AppDatabase.getInstance(applicationContext)?.subjects()
        var items: List<SubjectResource>? = emptyList()
        runBlocking {
            val job = async { subjects?.fetch() }
            items = job.await()
        }
        return items
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        val items = mutableListOf<SubjectResource>()
        fetch()?.forEach { resource ->
            resource.scheduleList.forEach {
                if (it.isToday())
                    items.add(resource)
            }
        }

        return SubjectWidgetRemoteView(applicationContext, items)
    }

}