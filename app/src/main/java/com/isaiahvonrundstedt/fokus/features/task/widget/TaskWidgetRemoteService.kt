package com.isaiahvonrundstedt.fokus.features.task.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.task.TaskResource
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class TaskWidgetRemoteService: RemoteViewsService() {

    private fun fetch(): List<TaskResource>? {
        val tasks = AppDatabase.getInstance(applicationContext)?.tasks()
        var items: List<TaskResource>? = emptyList()
        runBlocking {
            val job = async { tasks?.fetch() }
            items = job.await()
        }
        return items
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        val items = mutableListOf<TaskResource>()
        fetch()?.forEach {
            if (it.task.isDueToday())
                items.add(it)
        }

        return TaskWidgetRemoteView(applicationContext, items)
    }

}