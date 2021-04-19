package com.isaiahvonrundstedt.fokus.database.repository

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isAfterNow
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.isBeforeNow
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.dao.EventDAO
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import com.isaiahvonrundstedt.fokus.features.event.widget.EventWidgetProvider
import com.isaiahvonrundstedt.fokus.features.notifications.event.EventNotificationWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EventRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val events: EventDAO,
    private val preferenceManager: PreferenceManager,
    private val workManager: WorkManager,
    private val notificationManager: NotificationManager
) {

    fun fetchLiveData(): LiveData<List<EventPackage>> = events.fetchLiveData()

    fun fetchArchivedLiveData(): LiveData<List<EventPackage>> = events.fetchArchivedLiveData()

    suspend fun checkNameUniqueness(name: String?): List<String> = events.checkNameUniqueness(name)

    suspend fun fetch(): List<EventPackage> = events.fetchPackage()

    suspend fun fetchCore(): List<Event> = events.fetch()

    suspend fun insert(event: Event) {
        events.insert(event)

        EventWidgetProvider.triggerRefresh(context)

        if (preferenceManager.eventReminder && event.schedule?.isAfterNow() == true) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .addTag(event.eventID)
                .build()
            workManager.enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
                request)
        }
    }

    suspend fun remove(event: Event) {
        events.remove(event)

        EventWidgetProvider.triggerRefresh(context)

        if (event.isImportant)
            notificationManager.cancel(event.eventID, BaseWorker.NOTIFICATION_ID_EVENT)

        workManager.cancelUniqueWork(event.eventID)
    }

    suspend fun update(event: Event) {
        events.update(event)

        EventWidgetProvider.triggerRefresh(context)

        if (event.schedule?.isBeforeNow() == true || !event.isImportant)
            notificationManager.cancel(event.eventID, BaseWorker.NOTIFICATION_ID_EVENT)

        if (preferenceManager.eventReminder && event.schedule?.isAfterNow() == true) {
            val data = BaseWorker.convertEventToData(event)
            val request = OneTimeWorkRequest.Builder(EventNotificationWorker::class.java)
                .setInputData(data)
                .addTag(event.eventID)
                .build()
            workManager.enqueueUniqueWork(event.eventID, ExistingWorkPolicy.REPLACE,
                request)
        }
    }
}