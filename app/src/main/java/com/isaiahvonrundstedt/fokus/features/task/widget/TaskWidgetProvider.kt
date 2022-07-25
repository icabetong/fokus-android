package com.isaiahvonrundstedt.fokus.features.task.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.features.task.editor.TaskEditorContainer

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == WIDGET_ACTION_UPDATE) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context!!, TaskWidgetProvider::class.java)

            manager.notifyAppWidgetViewDataChanged(
                manager.getAppWidgetIds(component),
                R.id.listView
            )
        }
    }

    override fun onUpdate(
        context: Context?, appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        appWidgetIds?.forEach {
            onUpdateWidget(context, appWidgetManager, it)
        }
    }

    private fun onUpdateWidget(context: Context?, manager: AppWidgetManager?, id: Int) {
        val mainIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_NAVIGATION_TASK
            }, PendingIntent.FLAG_IMMUTABLE
        )

        val itemIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_WIDGET_TASK
            }, PendingIntent.FLAG_IMMUTABLE
        )

        val addIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, TaskEditorContainer::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(context?.packageName, R.layout.layout_widget_tasks)
        with(views) {
            setOnClickPendingIntent(R.id.rootView, mainIntent)
            setRemoteAdapter(R.id.listView, Intent(context, TaskWidgetService::class.java))
            setPendingIntentTemplate(R.id.listView, itemIntent)
            setEmptyView(R.id.listView, R.id.emptyView)
            setOnClickPendingIntent(R.id.actionButton, addIntent)
        }

        manager?.notifyAppWidgetViewDataChanged(id, R.id.listView)
        manager?.updateAppWidget(id, views)
    }

    companion object {
        private const val WIDGET_ACTION_UPDATE = "widget:task:update"

        fun triggerRefresh(context: Context?) {
            context?.sendBroadcast(Intent(WIDGET_ACTION_UPDATE).apply {
                component = ComponentName(context, TaskWidgetProvider::class.java)
            })
        }

    }

}