package com.isaiahvonrundstedt.fokus.features.task.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.putExtra
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.features.task.TaskResource

class TaskWidgetRemoteView(private var context: Context, private var items: List<TaskResource>)
    : RemoteViewsService.RemoteViewsFactory {

    override fun getLoadingView(): RemoteViews
            = RemoteViews(context.packageName, R.layout.layout_widget_progress)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val task = items[position].task
        val subject = items[position].subject
        val attachments = items[position].attachmentList

        val itemIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_TASK, task)
            putExtra(MainActivity.EXTRA_SUBJECT, subject)
            putExtra(MainActivity.EXTRA_ATTACHMENTS, attachments)
        }

        val views = RemoteViews(context.packageName, R.layout.layout_item_widget)
        with(views) {
            setTextViewText(R.id.titleView, task.name)
            setTextViewText(R.id.summaryView, task.formatDueDate(context))
            setOnClickFillInIntent(R.id.rootView, itemIntent)
            if (subject != null)
                setInt(R.id.imageView, "setColorFilter", subject.tag.color)
            else setViewVisibility(R.id.imageView, View.GONE)
        }
        return views
    }

    override fun getCount(): Int = items.size

    override fun getViewTypeCount(): Int = 1

    override fun onCreate() {}
    override fun onDestroy() {}
    override fun onDataSetChanged() {}

}