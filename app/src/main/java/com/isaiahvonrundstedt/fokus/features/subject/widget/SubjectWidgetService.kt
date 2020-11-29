package com.isaiahvonrundstedt.fokus.features.subject.widget

import android.content.Intent
import android.widget.RemoteViewsService

class SubjectWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return SubjectWidgetRemoteViewFactory(applicationContext)
    }

}