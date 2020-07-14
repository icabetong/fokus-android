package com.isaiahvonrundstedt.fokus.features.subject.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.SubjectResource

class SubjectWidgetRemoteView(private var context: Context,
                              private var items: List<SubjectResource>)
    : RemoteViewsService.RemoteViewsFactory {

    override fun getLoadingView(): RemoteViews = RemoteViews(context.packageName, R.layout.layout_widget_progress)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val subject = items[position].subject
        val schedules = items[position].schedules
        val schedule: Schedule? = schedules.run {
            forEach {
                if (it.isToday())
                    return@run it
            }
            return@run null
        }

        val itemIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_SUBJECT, subject)
            putExtra(MainActivity.EXTRA_SCHEDULES, schedule)
        }

        val views = RemoteViews(context.packageName, R.layout.layout_item_widget)
        with(views) {
            setTextViewText(R.id.titleView, subject.code)
            setTextViewText(R.id.summaryView, schedule?.formatBothTime())
            setOnClickFillInIntent(R.id.listView, itemIntent)
            setInt(R.id.imageView, "setColorFilter", subject.tag.color)
        }
        return views
    }

    override fun getCount(): Int = items.size

    override fun getViewTypeCount(): Int = 1

    override fun onCreate() {}
    override fun onDestroy() {}
    override fun onDataSetChanged() {}

}