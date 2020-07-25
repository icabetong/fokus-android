package com.isaiahvonrundstedt.fokus.features.widget.subject

import android.content.Intent
import android.widget.RemoteViewsService

class SubjectWidgetRemoteService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return SubjectWidgetRemoteView(applicationContext)
    }

}