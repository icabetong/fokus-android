package com.isaiahvonrundstedt.fokus.features.subject.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class SubjectWidgetRemoteViewFactory(private var context: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private var itemList = mutableListOf<SubjectPackage>()

    private fun fetch() {
        itemList.clear()

        val subjects = AppDatabase.getInstance(context)?.subjects()
        var items = emptyList<SubjectPackage>()
        runBlocking {
            val job = async { subjects?.fetchAsPackage() }
            items = job.await() ?: emptyList()
            items.forEach { resource ->
                resource.schedules.forEach {
                    if (it.isToday()) itemList.add(resource)
                }
            }
        }
    }

    override fun onDataSetChanged() = fetch()

    override fun getLoadingView(): RemoteViews =
        RemoteViews(context.packageName, R.layout.layout_widget_progress)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val subject = itemList[position].subject
        val schedules = itemList[position].schedules
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
            setTextViewText(R.id.summaryView, schedule?.formatBothTime(context))
            setOnClickFillInIntent(R.id.listView, itemIntent)
            setInt(R.id.imageView, "setColorFilter", subject.tag.color)
        }
        return views
    }

    override fun getCount(): Int = itemList.size

    override fun getViewTypeCount(): Int = 1

    override fun onCreate() {}
    override fun onDestroy() {}

}