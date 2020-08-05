package com.isaiahvonrundstedt.fokus.components.service

import android.app.Notification
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.components.utils.AppNotificationManager
import com.isaiahvonrundstedt.fokus.components.utils.JsonDataStreamer
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(AppNotificationManager(this)) {
                create(AppNotificationManager.CHANNEL_ID_BACKUP)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(NOTIFICATION_RESTORE_ONGOING,
                createNotification(R.string.notification_restore_ongoing, ongoing = true))
        else manager?.notify(NOTIFICATION_RESTORE_ONGOING,
            createNotification(R.string.notification_restore_ongoing, ongoing = true))

        try {
            val archiveStream: InputStream? = contentResolver.openInputStream(uri)
            val archive = ZipArchiveManager.convertInputStream(this, archiveStream)

            val e = archive.entries()
            while (e.hasMoreElements()) {
                val entry = e.nextElement() as ZipEntry
                archive.getInputStream(entry).use { stream ->
                    when (entry.name) {
                        FILE_BACKUP_NAME_SUBJECTS -> {
                            runBlocking {
                                JsonDataStreamer.decodeFromJson(stream, Subject::class.java)?.run {
                                    forEach { database?.subjects()?.insert(it) }
                                }
                            }
                        }
                        FILE_BACKUP_NAME_SCHEDULES -> {
                            runBlocking {
                                JsonDataStreamer.decodeFromJson(stream, Schedule::class.java)?.run {
                                    forEach { database?.schedules()?.insert(it) }
                                }
                            }
                        }
                        FILE_BACKUP_NAME_TASKS -> {
                            runBlocking {
                                JsonDataStreamer.decodeFromJson(stream, Task::class.java)?.run {
                                    forEach { database?.tasks()?.insert(it) }
                                }
                            }
                        }
                        FILE_BACKUP_NAME_ATTACHMENTS -> {
                            runBlocking {
                                JsonDataStreamer.decodeFromJson(stream, Attachment::class.java)?.run {
                                    forEach { database?.attachments()?.insert(it) }
                                }
                            }
                        }
                        FILE_BACKUP_NAME_EVENTS -> {
                            runBlocking {
                                JsonDataStreamer.decodeFromJson(stream, Event::class.java)?.run {
                                    forEach { database?.events()?.insert(it) }
                                }
                            }
                        }
                        FILE_BACKUP_NAME_LOGS -> {
                            runBlocking {
                                JsonDataStreamer.decodeFromJson(stream, Log::class.java)?.run {
                                    forEach { database?.logs()?.insert(it) }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
            archive.close()

            removeOngoingNotification(NOTIFICATION_RESTORE_ONGOING)
            manager?.notify(NOTIFICATION_RESTORE_SUCCESS,
                createNotification(R.string.notification_restore_success))
        } catch (e: EOFException) {
            e.printStackTrace()

            removeOngoingNotification(NOTIFICATION_RESTORE_ONGOING)
            manager?.notify(NOTIFICATION_RESTORE_FAILED,
                createNotification(R.string.notification_restore_error,
                    R.string.feedback_backup_corrupted))
        } catch (e: Exception) {
            e.printStackTrace()

            removeOngoingNotification(NOTIFICATION_RESTORE_ONGOING)
            manager?.notify(NOTIFICATION_RESTORE_FAILED,
                createNotification(R.string.notification_restore_error,
                    R.string.feedback_backup_invalid))
        }

        terminateService()
    }

    private fun removeOngoingNotification(id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true)
        else manager?.cancel(id)
    }

    private fun startBackup(destination: Uri) {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED)
            return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(AppNotificationManager(this)) {
                create(AppNotificationManager.CHANNEL_ID_BACKUP)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(NOTIFICATION_BACKUP_ONGOING,
                createNotification(R.string.notification_backup_ongoing, ongoing = true))
        else manager?.notify(NOTIFICATION_BACKUP_ONGOING,
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

                if (items.isEmpty())
                    terminateService(BROADCAST_BACKUP_EMPTY)
                else
                    ZipArchiveManager.Create(this@BackupRestoreService)
                        .fromSource(items)
                        .toDestination(destination)
                        .compress()
            }

            removeOngoingNotification(NOTIFICATION_BACKUP_ONGOING)
            manager?.notify(NOTIFICATION_BACKUP_SUCCESS,
                createNotification(R.string.notification_backup_success))

            PreferenceManager(this).backupDate = DateTime.now()
        } catch (e: Exception) {
            e.printStackTrace()

            removeOngoingNotification(NOTIFICATION_BACKUP_ONGOING)
            manager?.notify(NOTIFICATION_BACKUP_FAILED,
                createNotification(R.string.notification_backup_error))
        }

        terminateService(BROADCAST_BACKUP_SUCCESS)
    }

    private fun createCache(name: String, json: String): File {
        return File(cacheDir, name).apply {
            FileOutputStream(this).use {
                it.write(json.toByteArray())
                it.flush()
            }
        }
    }

    private fun terminateService(status: String = "") {
        if (status.isNotEmpty())
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(ACTION_SERVICE_BROADCAST).apply {
                    putExtra(EXTRA_BROADCAST_STATUS, status)
                })
        stopSelf()
    }

    private fun createNotification(@StringRes id: Int, @StringRes content: Int = 0,
                                   ongoing: Boolean = false): Notification {
        return NotificationCompat.Builder(this,
            AppNotificationManager.CHANNEL_ID_BACKUP).apply {
            setSmallIcon(R.drawable.ic_outline_done_all_24)
            setContentTitle(getString(id))
            if (content != 0) setContentText(getString(content))
            setOngoing(ongoing)
            setCategory(Notification.CATEGORY_SERVICE)
            setChannelId(AppNotificationManager.CHANNEL_ID_BACKUP)
            if (ongoing) setProgress(0, 0, true)
            color = ContextCompat.getColor(this@BackupRestoreService, R.color.color_primary)
        }.build()
    }

    companion object {
        const val FILE_BACKUP_NAME_ARCHIVE = "backup.zip"
        const val FILE_BACKUP_NAME_SUBJECTS = "backup_1_subjects.json"
        const val FILE_BACKUP_NAME_SCHEDULES = "backup_2_schedules.json"
        const val FILE_BACKUP_NAME_TASKS = "backup_3_tasks.json"
        const val FILE_BACKUP_NAME_ATTACHMENTS = "backup_4_attachments.json"
        const val FILE_BACKUP_NAME_EVENTS = "backup_5_events.json"
        const val FILE_BACKUP_NAME_LOGS = "backup_6_logs.json"

        const val ACTION_BACKUP = "action:backup"
        const val ACTION_RESTORE = "action:restore"

        const val NOTIFICATION_BACKUP_ONGOING = 1
        const val NOTIFICATION_BACKUP_SUCCESS = 2
        const val NOTIFICATION_BACKUP_FAILED = 3
        const val NOTIFICATION_RESTORE_ONGOING = 4
        const val NOTIFICATION_RESTORE_SUCCESS = 5
        const val NOTIFICATION_RESTORE_FAILED = 6

        const val ACTION_SERVICE_BROADCAST = "action:service:status"
        const val EXTRA_BROADCAST_STATUS = "extra:broadcast:status"
        const val BROADCAST_BACKUP_SUCCESS = "broadcast:backup:success"
        const val BROADCAST_BACKUP_FAILED = "broadcast:backup:failed"
        const val BROADCAST_BACKUP_EMPTY = "broadcast:backup:empty"

        const val MIME_TYPE_ZIP = "application/zip"
        const val MIME_TYPE_JSON = "text/json"
    }
}