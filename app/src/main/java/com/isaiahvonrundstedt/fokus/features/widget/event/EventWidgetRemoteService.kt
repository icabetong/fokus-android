package com.isaiahvonrundstedt.fokus.features.widget.event

import android.content.Intent
import android.widget.RemoteViewsService

class EventWidgetRemoteService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return EventWidgetRemoteView(applicationContext)
    }
}