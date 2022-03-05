package com.isaiahvonrundstedt.fokus.components.service

import android.content.Intent
import android.net.Uri
import android.os.IBinder
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.Metadata
import com.isaiahvonrundstedt.fokus.components.utils.DataArchiver
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task
import java.io.File

class DataExporterService : BaseService() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onExport(intent)
        sendLocalBroadcast(BROADCAST_EXPORT_ONGOING)

        return START_REDELIVER_INTENT
    }

    private fun onExport(intent: Intent?) {
        if (intent?.hasExtra(EXTRA_EXPORT_SOURCE) == false)
            terminateService(BROADCAST_EXPORT_FAILED)

        val destination: Uri? = intent?.data
        val items = mutableListOf<File>()

        var fileName: String = Streamable.ARCHIVE_NAME_GENERIC

        try {
            when (intent?.action) {
                ACTION_EXPORT_SUBJECT -> {
                    items.add(
                        Metadata(data = Metadata.DATA_SUBJECT)
                            .toJsonFile(cacheDir, Metadata.FILE_NAME)
                    )

                    val subject: Subject? = intent.getParcelableExtra(EXTRA_EXPORT_SOURCE)
                    val schedules: List<Schedule>? =
                        intent.getParcelableListExtra(EXTRA_EXPORT_DEPENDENTS)

                    if (subject != null) {
                        fileName = subject.code ?: Streamable.ARCHIVE_NAME_GENERIC
                        items.add(subject.toJsonFile(cacheDir, Streamable.FILE_NAME_SUBJECT))
                    }
                    items.add(Schedule.toJsonFile(schedules ?: emptyList(), cacheDir))

                }
                ACTION_EXPORT_TASK -> {
                    items.add(
                        Metadata(data = Metadata.DATA_TASK)
                            .toJsonFile(cacheDir, Metadata.FILE_NAME)
                    )

                    val task: Task? = intent.getParcelableExtra(EXTRA_EXPORT_SOURCE)
                    if (task != null) {
                        fileName = task.name ?: Streamable.ARCHIVE_NAME_GENERIC
                        items.add(task.toJsonFile(cacheDir, Streamable.FILE_NAME_TASK))
                    }

                    var attachments: List<Attachment> =
                        intent.getParcelableListExtra(EXTRA_EXPORT_DEPENDENTS) ?: emptyList()
                    attachments = attachments.filter { it.type == Attachment.TYPE_WEBSITE_LINK }
                    items.add(Attachment.toJsonFile(attachments, cacheDir))
                }
                ACTION_EXPORT_EVENT -> {

                    items.add(
                        Metadata(data = Metadata.DATA_EVENT)
                            .toJsonFile(cacheDir, Metadata.FILE_NAME)
                    )

                    val event: Event? = intent.getParcelableExtra(EXTRA_EXPORT_SOURCE)

                    if (event != null) {
                        fileName = event.name ?: Streamable.ARCHIVE_NAME_GENERIC
                        items.add(event.toJsonFile(cacheDir, Streamable.FILE_NAME_EVENT))
                    }
                }
            }

            if (destination == null) {
                val cache = File(externalCacheDir, fileName)

                DataArchiver.Create(this)
                    .addSource(items)
                    .toDestination(cache)
                    .start()

                terminateService(BROADCAST_EXPORT_COMPLETED, cache.path)
            } else {
                DataArchiver.Create(this)
                    .addSource(items)
                    .toDestination(destination)
                    .start()

                terminateService(BROADCAST_EXPORT_COMPLETED)
            }
        } catch (e: Exception) {
            terminateService(BROADCAST_EXPORT_FAILED)
        }
    }

    companion object {
        const val EXTRA_EXPORT_SOURCE = "extra:export:source"
        const val EXTRA_EXPORT_DEPENDENTS = "extra:export:dependents"

        const val ACTION_EXPORT_SUBJECT = "action:export:subject"
        const val ACTION_EXPORT_TASK = "action:export:task"
        const val ACTION_EXPORT_EVENT = "action:export:event"

        const val BROADCAST_EXPORT_ONGOING = "broadcast:export:ongoing"
        const val BROADCAST_EXPORT_COMPLETED = "broadcast:export:completed"
        const val BROADCAST_EXPORT_FAILED = "broadcast:export:failed"
    }
}