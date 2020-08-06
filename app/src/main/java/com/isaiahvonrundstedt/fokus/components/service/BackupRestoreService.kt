package com.isaiahvonrundstedt.fokus.components.service

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.components.utils.ZipArchiveManager
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task
import kotlinx.coroutines.*
import okio.Okio
import org.joda.time.DateTime
import java.io.*
import java.util.zip.ZipEntry

class BackupRestoreService: BaseService() {

    private val database = AppDatabase.getInstance(this)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.data?.also {
            when (intent.action) {
                ACTION_BACKUP ->
                    startBackup(it)
                ACTION_RESTORE ->
                    startRestore(it)
            }
        }
        return START_NOT_STICKY
    }

    private fun startRestore(uri: Uri) {
        startForegroundCompat(NOTIFICATION_RESTORE_ONGOING,
            createNotification(R.string.notification_restore_ongoing, ongoing = true))

        try {
            val archiveStream: InputStream? = contentResolver.openInputStream(uri)
            val archive = ZipArchiveManager.convertInputStream(this, archiveStream)
            runBlocking {

                val entries = mutableListOf<ZipEntry>()
                val e = archive.entries()
                while (e.hasMoreElements()) {
                    val entry = e.nextElement() as ZipEntry
                    when (entry.name) {
                        FILE_BACKUP_NAME_SUBJECTS -> entries.add(0, entry)
                        FILE_BACKUP_NAME_SCHEDULES -> entries.add(1, entry)
                        FILE_BACKUP_NAME_TASKS -> entries.add(2, entry)
                        FILE_BACKUP_NAME_ATTACHMENTS -> entries.add(3, entry)
                        FILE_BACKUP_NAME_EVENTS -> entries.add(4, entry)
                        FILE_BACKUP_NAME_LOGS -> entries.add(5, entry)
                    }
                }
                entries.forEach { entry ->
                    archive.getInputStream(entry)?.use { tryParse(entry.name, it) }
                }
                entries.clear()

                stopForegroundCompat(NOTIFICATION_RESTORE_ONGOING)
                manager?.notify(NOTIFICATION_RESTORE_SUCCESS,
                    createNotification(R.string.notification_restore_success))
                terminateService()
            }
            archive.close()
        } catch (e: EOFException) {
            e.printStackTrace()

            stopForegroundCompat(NOTIFICATION_RESTORE_ONGOING)
            manager?.notify(NOTIFICATION_RESTORE_FAILED,
                createNotification(R.string.notification_restore_error,
                    R.string.feedback_restore_corrupted))
            terminateService()
        } catch (e: Exception) {
            e.printStackTrace()

            stopForegroundCompat(NOTIFICATION_RESTORE_ONGOING)
            manager?.notify(NOTIFICATION_RESTORE_FAILED,
                createNotification(R.string.notification_restore_error,
                    R.string.feedback_restore_invalid))
            terminateService()
        }
    }

    private suspend fun tryParse(name: String, stream: InputStream) {
        when (name) {
            FILE_BACKUP_NAME_SUBJECTS -> {
                JsonDataStreamer.decodeFromJson(stream, Subject::class.java)?.run {
                    forEach { database?.subjects()?.insert(it) }
                }
            }
            FILE_BACKUP_NAME_SCHEDULES -> {
                JsonDataStreamer.decodeFromJson(stream, Schedule::class.java)?.run {
                    forEach { database?.schedules()?.insert(it) }
                }
            }
            FILE_BACKUP_NAME_TASKS -> {
                JsonDataStreamer.decodeFromJson(stream, Task::class.java)?.run {
                    forEach { database?.tasks()?.insert(it) }
                }
            }
            FILE_BACKUP_NAME_ATTACHMENTS -> {
                JsonDataStreamer.decodeFromJson(stream, Attachment::class.java)?.run {
                    forEach { database?.attachments()?.insert(it) }
                }
            }
            FILE_BACKUP_NAME_EVENTS -> {
                JsonDataStreamer.decodeFromJson(stream, Event::class.java)?.run {
                    forEach { database?.events()?.insert(it) }
                }
            }
            FILE_BACKUP_NAME_LOGS -> {
                JsonDataStreamer.decodeFromJson(stream, Log::class.java)?.run {
                    forEach { database?.logs()?.insert(it) }
                }
            }
        }
    }

    private fun startBackup(destination: Uri) {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED)
            return

        startForegroundCompat(NOTIFICATION_BACKUP_ONGOING,
            createNotification(R.string.notification_backup_ongoing, ongoing = true))

        try {
            runBlocking {
                val items = mutableListOf<File>()
                var fetchJob: Job

                fetchJob = async { database?.subjects()?.fetchCore() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Subject::class.java)?.let {
                    items.add(createCache(FILE_BACKUP_NAME_SUBJECTS, it))
                }

                fetchJob = async { database?.schedules()?.fetch() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Schedule::class.java)?.let {
                    items.add(createCache(FILE_BACKUP_NAME_SCHEDULES, it))
                }

                fetchJob = async { database?.tasks()?.fetchCore() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Task::class.java)?.let {
                    items.add(createCache(FILE_BACKUP_NAME_TASKS, it))
                }

                fetchJob = async { database?.attachments()?.fetch() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Attachment::class.java)?.let {
                    items.add(createCache(FILE_BACKUP_NAME_ATTACHMENTS, it))
                }

                fetchJob = async { database?.events()?.fetchCore() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Event::class.java)?.let {
                    items.add(createCache(FILE_BACKUP_NAME_EVENTS, it))
                }

                fetchJob = async { database?.logs()?.fetchCore() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Log::class.java)?.let {
                    items.add(createCache(FILE_BACKUP_NAME_LOGS, it))
                }

                if (items.isNotEmpty())
                    ZipArchiveManager.Create(this@BackupRestoreService)
                        .fromSource(items)
                        .toDestination(destination)
                        .start()
                else terminateService(BROADCAST_BACKUP_EMPTY)

                stopForegroundCompat(NOTIFICATION_BACKUP_ONGOING)
                manager?.notify(NOTIFICATION_BACKUP_SUCCESS,
                    createNotification(R.string.notification_backup_success))
                terminateService(BROADCAST_BACKUP_SUCCESS)
            }

            PreferenceManager(this).backupDate = DateTime.now()
        } catch (e: Exception) {
            e.printStackTrace()

            stopForegroundCompat(NOTIFICATION_BACKUP_ONGOING)
            manager?.notify(NOTIFICATION_BACKUP_FAILED,
                createNotification(R.string.notification_backup_error))
            terminateService(BROADCAST_BACKUP_FAILED)
        }
    }

    private fun createCache(name: String, json: String): File {
        return File(cacheDir, name).apply {
            Okio.buffer(Okio.sink(this)).use {
                it.write(json.toByteArray())
                it.flush()
            }
        }
    }

    companion object {
        const val FILE_BACKUP_NAME_ARCHIVE = "backup.zip"
        const val FILE_BACKUP_NAME_SUBJECTS = "subjects.json"
        const val FILE_BACKUP_NAME_SCHEDULES = "schedules.json"
        const val FILE_BACKUP_NAME_TASKS = "tasks.json"
        const val FILE_BACKUP_NAME_ATTACHMENTS = "attachments.json"
        const val FILE_BACKUP_NAME_EVENTS = "events.json"
        const val FILE_BACKUP_NAME_LOGS = "logs.json"

        const val ACTION_BACKUP = "action:backup"
        const val ACTION_RESTORE = "action:restore"

        const val NOTIFICATION_BACKUP_ONGOING = 1
        const val NOTIFICATION_BACKUP_SUCCESS = 2
        const val NOTIFICATION_BACKUP_FAILED = 3
        const val NOTIFICATION_RESTORE_ONGOING = 4
        const val NOTIFICATION_RESTORE_SUCCESS = 5
        const val NOTIFICATION_RESTORE_FAILED = 6

        const val BROADCAST_BACKUP_SUCCESS = "broadcast:backup:success"
        const val BROADCAST_BACKUP_FAILED = "broadcast:backup:failed"
        const val BROADCAST_BACKUP_EMPTY = "broadcast:backup:empty"

        const val MIME_TYPE_ZIP = "application/zip"
        const val MIME_TYPE_JSON = "text/json"
    }
}