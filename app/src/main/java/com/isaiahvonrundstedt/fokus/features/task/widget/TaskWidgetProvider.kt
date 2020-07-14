package com.isaiahvonrundstedt.fokus.features.task.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?,
                          appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds?.forEach {
            onUpdateWidget(context, appWidgetManager, it)
        }
    }

    private fun onUpdateWidget(context: Context?, manager: AppWidgetManager?, id: Int) {
        val mainIntent = PendingIntent.getActivity(context, 0,
            Intent(context, MainActivity::class.java), 0)

        val itemIntent = PendingIntent.getActivity(context, 0,
            Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_WIDGET_TASK
            }, PendingIntent.FLAG_UPDATE_CURRENT)

        val views = RemoteViews(context?.packageName, R.layout.layout_widget_tasks)
        with(views) {
            setOnClickPendingIntent(R.id.rootView, mainIntent)
            setRemoteAdapter(R.id.listView, Intent(context, TaskWidgetRemoteService::class.java))
            setPendingIntentTemplate(R.id.listView, itemIntent)
            setEmptyView(R.id.listView, R.id.emptyView)
        }

        manager?.notifyAppWidgetViewDataChanged(id, R.id.listView)
        manager?.updateAppWidget(id, views)
    }

}