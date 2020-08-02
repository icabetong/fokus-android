package com.isaiahvonrundstedt.fokus.components.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.components.extensions.android.getFileName
import com.isaiahvonrundstedt.fokus.components.json.DateTimeJSONAdapter
import com.isaiahvonrundstedt.fokus.components.json.LocalTimeJSONAdapter
import com.isaiahvonrundstedt.fokus.components.json.UriJSONAdapter
import com.isaiahvonrundstedt.fokus.components.utils.NotificationChannelManager
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okio.Okio
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class BackupRestoreService: BaseService() {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(DateTimeJSONAdapter())
            .add(LocalTimeJSONAdapter())
            .add(UriJSONAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

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
            with(NotificationChannelManager(this)) {
                create(NotificationChannelManager.CHANNEL_ID_BACKUP)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(NOTIFICATION_RESTORE_ONGOING,
                createNotification(R.string.notification_restore_ongoing, ongoing = true))
        else manager?.notify(NOTIFICATION_RESTORE_ONGOING,
            createNotification(R.string.notification_restore_ongoing, ongoing = true))

        try {
            val archiveStream: InputStream? = contentResolver.openInputStream(uri)
            val temp = File(cacheDir, FILE_TEMP_WORKING_FILE)
            FileUtils.copyToFile(archiveStream, temp)
            val archive = ZipFile(temp)

            val e = archive.entries()
            while (e.hasMoreElements()) {
                val entry = e.nextElement() as ZipEntry
                archive.getInputStream(entry).use { stream ->
                    when (entry.name) {
                        FILE_BACKUP_NAME_SUBJECTS -> {
                            runBlocking {
                                val items = encodeFromJSON(stream, Subject::class.java)
                                items?.forEach { database?.subjects()?.insert(it) }
                            }
                        }
                        FILE_BACKUP_NAME_SCHEDULES -> {
                            runBlocking {
                                val items = encodeFromJSON(stream, Schedule::class.java)
                                items?.forEach { database?.schedules()?.insert(it) }
                            }
                        }
                        FILE_BACKUP_NAME_TASKS -> {
                            runBlocking {
                                val items = encodeFromJSON(stream, Task::class.java)
                                items?.forEach { database?.tasks()?.insert(it) }
                            }
                        }
                        FILE_BACKUP_NAME_ATTACHMENTS -> {
                            runBlocking {
                                val items = encodeFromJSON(stream, Attachment::class.java)
                                items?.forEach { database?.attachments()?.insert(it) }
                            }
                        }
                        FILE_BACKUP_NAME_EVENTS -> {
                            runBlocking {
                                val items = encodeFromJSON(stream, Event::class.java)
                                items?.forEach { database?.events()?.insert(it) }
                            }
                        }
                        FILE_BACKUP_NAME_LOGS -> {
                            runBlocking {
                                val items = encodeFromJSON(stream, Log::class.java)
                                items?.forEach { database?.logs()?.insert(it) }
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
            with(NotificationChannelManager(this)) {
                create(NotificationChannelManager.CHANNEL_ID_BACKUP)
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
                var jsonString: String

                fetchJob = async { database?.subjects()?.fetchCore() }
                jsonString = encodeToJSON(fetchJob.await(), Subject::class.java)
                if (jsonString.isNotEmpty())
                    items.add(createCache(FILE_BACKUP_NAME_SUBJECTS, jsonString))

                fetchJob = async { database?.schedules()?.fetch() }
                jsonString = encodeToJSON(fetchJob.await(), Schedule::class.java)
                if (jsonString.isNotEmpty())
                    items.add(createCache(FILE_BACKUP_NAME_SCHEDULES, jsonString))

                fetchJob = async { database?.tasks()?.fetchCore() }
                jsonString = encodeToJSON(fetchJob.await(), Task::class.java)
                if (jsonString.isNotEmpty())
                    items.add(createCache(FILE_BACKUP_NAME_TASKS, jsonString))

                fetchJob = async { database?.attachments()?.fetch() }
                jsonString = encodeToJSON(fetchJob.await(), Attachment::class.java)
                if (jsonString.isNotEmpty())
                    items.add(createCache(FILE_BACKUP_NAME_ATTACHMENTS, jsonString))

                fetchJob = async { database?.events()?.fetchCore() }
                jsonString = encodeToJSON(fetchJob.await(), Event::class.java)
                if (jsonString.isNotEmpty())
                    items.add(createCache(FILE_BACKUP_NAME_EVENTS, jsonString))

                fetchJob = async { database?.logs()?.fetchCore() }
                jsonString = encodeToJSON(fetchJob.await(), Log::class.java)
                if (jsonString.isNotEmpty())
                    items.add(createCache(FILE_BACKUP_NAME_LOGS, jsonString))


                if (items.isEmpty())
                    terminateService(BROADCAST_BACKUP_EMPTY)
                else zip(items, destination)
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

    private fun <T> encodeFromJSON(stream: InputStream, dataClass: Class<T>): List<T>? {
        if (stream.available() < 1)
            return emptyList()

        val type = Types.newParameterizedType(List::class.java, dataClass)
        val adapter: JsonAdapter<List<T>> = moshi.adapter(type)
        return adapter.fromJson(Okio.buffer(Okio.source(stream)))
    }

    private fun <T> encodeToJSON(items: List<T>?, dataClass: Class<T>): String {
        if (items == null || items.isEmpty()) return ""

        val type = Types.newParameterizedType(List::class.java, dataClass)
        val adapter: JsonAdapter<List<T>> = moshi.adapter(type)
        return adapter.toJson(items)
    }

    private fun createCache(name: String, json: String): File {
        return File(cacheDir, name).apply {
            FileOutputStream(this).use {
                it.write(json.toByteArray())
                it.flush()
            }
        }
    }

    private fun zip(items: List<File>, destination: Uri) {
        val buffer = 8096

        contentResolver.openOutputStream(destination).use { stream ->
            ZipOutputStream(BufferedOutputStream(stream)).use { zip ->
                items.forEach { temp ->
                    BufferedInputStream(FileInputStream(temp), buffer).use { inputStream ->
                        zip.putNextEntry(ZipEntry(temp.name))

                        inputStream.copyTo(zip, buffer)
                    }
                }
                zip.flush()
            }
            stream?.flush()
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
            NotificationChannelManager.CHANNEL_ID_BACKUP).apply {
            setSmallIcon(R.drawable.ic_outline_done_all_24)
            setContentTitle(getString(id))
            if (content != 0) setContentText(getString(content))
            setOngoing(ongoing)
            setCategory(Notification.CATEGORY_SERVICE)
            setChannelId(NotificationChannelManager.CHANNEL_ID_BACKUP)
            if (ongoing) setProgress(0, 0, true)
            color = ContextCompat.getColor(this@BackupRestoreService, R.color.color_primary)
        }.build()
    }

    companion object {
        const val FILE_BACKUP_NAME_ARCHIVE = "backup.ffs"
        const val FILE_BACKUP_NAME_SUBJECTS = "backup_1_subjects.json"
        const val FILE_BACKUP_NAME_SCHEDULES = "backup_2_schedules.json"
        const val FILE_BACKUP_NAME_TASKS = "backup_3_tasks.json"
        const val FILE_BACKUP_NAME_ATTACHMENTS = "backup_4_attachments.json"
        const val FILE_BACKUP_NAME_EVENTS = "backup_5_events.json"
        const val FILE_BACKUP_NAME_LOGS = "backup_6_logs.json"
        const val FILE_TEMP_WORKING_FILE = "temp.zip"

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
    }
}