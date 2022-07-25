package com.isaiahvonrundstedt.fokus.features.subject.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.activities.MainActivity
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditorContainer

class SubjectWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == WIDGET_ACTION_UPDATE) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context!!, SubjectWidgetProvider::class.java)

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
                action = MainActivity.ACTION_NAVIGATION_SUBJECT
            }, PendingIntent.FLAG_IMMUTABLE
        )

        val itemIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_WIDGET_SUBJECT
            }, PendingIntent.FLAG_IMMUTABLE
        )

        val addIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, SubjectEditorContainer::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(context?.packageName, R.layout.layout_widget_subjects)
        with(views) {
            setOnClickPendingIntent(R.id.rootView, mainIntent)
            setOnClickPendingIntent(R.id.actionButton, addIntent)
            setRemoteAdapter(R.id.listView, Intent(context, SubjectWidgetService::class.java))
            setPendingIntentTemplate(R.id.listView, itemIntent)
            setEmptyView(R.id.listView, R.id.emptyView)
        }

        manager?.notifyAppWidgetViewDataChanged(id, R.id.listView)
        manager?.updateAppWidget(id, views)
    }

    companion object {
        private const val WIDGET_ACTION_UPDATE = "widget:event:update"

        fun triggerRefresh(context: Context?) {
            context?.sendBroadcast(Intent(WIDGET_ACTION_UPDATE).apply {
                component = ComponentName(context, SubjectWidgetProvider::class.java)
            })
        }
    }
}