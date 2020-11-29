package com.isaiahvonrundstedt.fokus.features.event.widget

import android.content.Intent
import android.widget.RemoteViewsService

class EventWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return EventWidgetRemoteViewFactory(applicationContext)
    }
}