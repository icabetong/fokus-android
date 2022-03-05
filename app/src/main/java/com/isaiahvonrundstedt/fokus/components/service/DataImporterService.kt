package com.isaiahvonrundstedt.fokus.components.service

import android.content.Intent
import android.os.IBinder
import android.os.Parcelable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.components.json.Metadata
import com.isaiahvonrundstedt.fokus.components.utils.DataArchiver
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import java.util.zip.ZipEntry

class DataImporterService : BaseService() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onImport(intent)
        return START_REDELIVER_INTENT
    }

    private fun onImport(intent: Intent?) {
        if (intent?.data == null)
            terminateService(BROADCAST_IMPORT_FAILED)

        contentResolver.openInputStream(intent?.data!!)?.use { inputStream ->
            val archive = DataArchiver.parseInputStream(this, inputStream)

            try {
                archive.getInputStream(archive.getEntry(Metadata.FILE_NAME)).use { it ->
                    val metadata = Metadata.fromInputStream(it)
                    if (metadata.verify(Metadata.DATA_SUBJECT) &&
                        intent.action == ACTION_IMPORT_SUBJECT
                    ) {

                        val subjectPackage = SubjectPackage(Subject())
                        for (entry: ZipEntry in archive.entries()) {

                            if (entry.name == Streamable.FILE_NAME_SUBJECT) {
                                archive.getInputStream(entry)?.use { inputStream ->
                                    subjectPackage.subject = Subject.fromInputStream(inputStream)
                                }
                            } else if (entry.name == Streamable.FILE_NAME_SCHEDULE) {
                                archive.getInputStream(entry)?.use { inputStream ->
                                    JsonDataStreamer.decodeFromJson(
                                        inputStream,
                                        Schedule::class.java
                                    )
                                        ?.also { items ->
                                            subjectPackage.schedules = items
                                        }
                                }
                            }
                        }

                        sendResult(subjectPackage)
                    } else if (metadata.verify(Metadata.DATA_TASK) &&
                        intent.action == ACTION_IMPORT_TASK
                    ) {

                        val taskPackage = TaskPackage(Task())
                        for (entry: ZipEntry in archive.entries()) {

                            if (entry.name == Streamable.FILE_NAME_TASK) {
                                archive.getInputStream(entry)?.use { inputStream ->
                                    taskPackage.task = Task.fromInputStream(inputStream)
                                }
                            } else if (entry.name == Streamable.FILE_NAME_ATTACHMENT) {
                                archive.getInputStream(entry)?.use { inputStream ->
                                    JsonDataStreamer.decodeFromJson(
                                        inputStream,
                                        Attachment::class.java
                                    )
                                        ?.also { items ->
                                            taskPackage.attachments = items
                                        }
                                }
                            }
                        }

                        sendResult(taskPackage)
                    } else if (metadata.verify(Metadata.DATA_EVENT) &&
                        intent.action == ACTION_IMPORT_EVENT
                    ) {

                        val eventPackage = EventPackage(Event())
                        for (entry: ZipEntry in archive.entries()) {
                            if (entry.name == Streamable.FILE_NAME_EVENT) {
                                archive.getInputStream(entry)?.use { inputStream ->
                                    eventPackage.event = Event.fromInputStream(inputStream)
                                }
                            }
                        }
                        sendResult(eventPackage)
                    } else terminateService(BROADCAST_IMPORT_FAILED)
                }
            } catch (e: Exception) {

                e.printStackTrace()
                terminateService(BROADCAST_IMPORT_FAILED)
            }
        }
    }

    private fun <T : Parcelable> sendResult(t: T) {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(ACTION_SERVICE_BROADCAST).apply {
                putExtra(EXTRA_BROADCAST_STATUS, BROADCAST_IMPORT_COMPLETED)
                putExtra(EXTRA_BROADCAST_DATA, t)
            })
        terminateService()
    }

    companion object {
        const val ACTION_IMPORT_TASK = "action:import:task"
        const val ACTION_IMPORT_SUBJECT = "action:import:subject"
        const val ACTION_IMPORT_EVENT = "action:import:event"

        const val BROADCAST_IMPORT_ONGOING = "broadcast:import:ongoing"
        const val BROADCAST_IMPORT_COMPLETED = "broadcast:import:completed"
        const val BROADCAST_IMPORT_FAILED = "broadcast:import:failed"
    }
}