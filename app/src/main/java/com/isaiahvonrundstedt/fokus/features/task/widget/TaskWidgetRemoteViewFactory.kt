package com.isaiahvonrundstedt.fokus.features.task.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.putExtra
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class TaskWidgetRemoteViewFactory(private var context: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private var itemList = mutableListOf<TaskPackage>()

    private fun fetch() {
        itemList.clear()

        val tasks = AppDatabase.getInstance(context).tasks()
        var items = emptyList<TaskPackage>()
        runBlocking {
            val job = async { tasks.fetchAsPackage() }
            items = job.await() ?: emptyList()
            items.forEach { if (it.task.isDueToday() || !it.task.hasDueDate()) itemList.add(it) }
        }
    }

    override fun onDataSetChanged() = fetch()

    override fun getLoadingView(): RemoteViews =
        RemoteViews(context.packageName, R.layout.layout_widget_progress)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val task = itemList[position].task
        val subject = itemList[position].subject
        val attachments = itemList[position].attachments

        val itemIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_TASK, task)
            putExtra(MainActivity.EXTRA_SUBJECT, subject)
            putExtra(MainActivity.EXTRA_ATTACHMENTS, attachments)
        }

        val views = RemoteViews(context.packageName, R.layout.layout_item_widget)
        with(views) {
            setTextViewText(R.id.titleView, task.name)
            if (task.hasDueDate())
                setTextViewText(R.id.summaryView, task.formatDueDate(context))
            else setViewVisibility(R.id.summaryView, View.GONE)
            setOnClickFillInIntent(R.id.rootView, itemIntent)
            if (subject != null)
                setInt(R.id.imageView, "setColorFilter", subject.tag.color)
            else setViewVisibility(R.id.imageView, View.GONE)
        }
        return views
    }

    override fun getCount(): Int = itemList.size

    override fun getViewTypeCount(): Int = 1

    override fun onCreate() {}

    override fun onDestroy() {}

}