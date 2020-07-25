package com.isaiahvonrundstedt.fokus.features.widget.task

import android.content.Intent
import android.widget.RemoteViewsService

class TaskWidgetRemoteService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return TaskWidgetRemoteView(applicationContext)
    }

}