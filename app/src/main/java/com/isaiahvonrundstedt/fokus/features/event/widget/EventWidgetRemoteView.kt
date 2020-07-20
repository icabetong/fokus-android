package com.isaiahvonrundstedt.fokus.features.event.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.features.event.EventResource

class EventWidgetRemoteView(private var context: Context, private var items: List<EventResource>)
    : RemoteViewsService.RemoteViewsFactory {

    override fun getLoadingView(): RemoteViews = RemoteViews(context.packageName, R.layout.layout_widget_progress)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val event = items[position].event
        val subject = items[position].subject

        val itemIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_EVENT, event)
            putExtra(MainActivity.EXTRA_SUBJECT, subject)
        }

        val views = RemoteViews(context.packageName, R.layout.layout_item_widget)
        with(views) {
            setTextViewText(R.id.titleView, event.name)
            setTextViewText(R.id.summaryView, event.formatSchedule(context))
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