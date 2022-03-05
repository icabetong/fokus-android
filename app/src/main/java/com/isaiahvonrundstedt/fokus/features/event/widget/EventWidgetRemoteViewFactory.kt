package com.isaiahvonrundstedt.fokus.features.event.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class EventWidgetRemoteViewFactory(private var context: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private var itemList = mutableListOf<EventPackage>()

    private fun fetch() {
        itemList.clear()

        val events = AppDatabase.getInstance(context).events()
        var items = emptyList<EventPackage>()
        runBlocking {
            val job = async { events.fetchPackage() }
            items = job.await() ?: emptyList()
            items.forEach { if (it.event.isToday()) itemList.add(it) }
        }
    }

    override fun onDataSetChanged() = fetch()

    override fun getLoadingView(): RemoteViews =
        RemoteViews(context.packageName, R.layout.layout_widget_progress)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val event = itemList[position].event
        val subject = itemList[position].subject

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

    override fun getCount(): Int = itemList.size

    override fun getViewTypeCount(): Int = 1

    override fun onCreate() {}
    override fun onDestroy() {}

}