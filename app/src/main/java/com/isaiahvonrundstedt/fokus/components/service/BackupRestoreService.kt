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
import com.isaiahvonrundstedt.fokus.components.json.DateTimeJSONAdapter
import com.isaiahvonrundstedt.fokus.components.json.LocalTimeJSONAdapter
import com.isaiahvonrundstedt.fokus.components.json.UriJSONAdapter
import com.isaiahvonrundstedt.fokus.components.utils.NotificationChannelManager
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
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
        if (intent?.action == ACTION_BACKUP)
            startBackup()
        else if (intent?.action == ACTION_RESTORE)
            intent.data?.also { startRestore(it) }
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
                createNotification(R.string.notification_restore_ongoing, true))
        else manager?.notify(NOTIFICATION_RESTORE_ONGOING,
            createNotification(R.string.notification_restore_ongoing, true))

        try {
            val archiveStream: InputStream? = contentResolver.openInputStream(uri)
            val temp = File(cacheDir, FILE_TEMP_WORKING_FILE)
            FileUtils.copyToFile(archiveStream, temp)
            val archive = ZipFile(temp)

            val e = archive.entries()
            while (e.hasMoreElements()) {
                val entry = e.nextElement() as ZipEntry
                val inputStream: InputStream = archive.getInputStream(entry)

                when (entry.name) {
                    FILE_BACKUP_NAME_SUBJECTS -> {
                        runBlocking {
                            val items = encodeFromJSON(inputStream, Subject::class.java)
                            items?.forEach { database?.subjects()?.insert(it) }
                        }
                    }
                    FILE_BACKUP_NAME_SCHEDULES -> {
                        runBlocking {
                            val items = encodeFromJSON(inputStream, Schedule::class.java)
                            items?.forEach { database?.schedules()?.insert(it) }
                        }
                    }
                    FILE_BACKUP_NAME_TASKS -> {
                        runBlocking {
                            val items = encodeFromJSON(inputStream, Task::class.java)
                            items?.forEach { database?.tasks()?.insert(it) }
                        }
                    }
                    FILE_BACKUP_NAME_ATTACHMENTS -> {
                        runBlocking {
                            val items = encodeFromJSON(inputStream, Attachment::class.java)
                            items?.forEach { database?.attachments()?.insert(it) }
                        }
                    }
                    FILE_BACKUP_NAME_EVENTS -> {
                        runBlocking {
                            val items = encodeFromJSON(inputStream, Event::class.java)
                            items?.forEach { database?.events()?.insert(it) }
                        }
                    }
                    FILE_BACKUP_NAME_LOGS -> {
                        runBlocking {
                            val items = encodeFromJSON(inputStream, Log::class.java)
                            items?.forEach { database?.logs()?.insert(it) }
                        }
                    }
                }
            }
            archive.close()

            manager?.notify(NOTIFICATION_RESTORE_SUCCESS,
                createNotification(R.string.notification_restore_success))
        } catch (e: Exception) {
            e.printStackTrace()
            manager?.notify(NOTIFICATION_RESTORE_FAILED,
                createNotification(R.string.notification_restore_error))
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                stopForeground(true)
            else manager?.cancel(NOTIFICATION_BACKUP_ONGOING)
        }

        terminateService()
    }

    private fun startBackup() {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED)
            return

        val backupFolder = File(getExternalFilesDir(null), FOLDER_BACKUP_NAME)

        if (!backupFolder.exists()) backupFolder.mkdirs()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(NotificationChannelManager(this)) {
                create(NotificationChannelManager.CHANNEL_ID_BACKUP)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(NOTIFICATION_BACKUP_ONGOING,
                createNotification(R.string.notification_backup_ongoing, true))
        else manager?.notify(NOTIFICATION_BACKUP_ONGOING,
            createNotification(R.string.notification_backup_ongoing, true))

        try {
            runBlocking {
                val files = mutableListOf<File>()

                val subjects = async { database?.subjects()?.fetchCore() }
                files.add(saveAsFile(backupFolder, FILE_BACKUP_NAME_SUBJECTS,
                    encodeToJSON(subjects.await() ?: emptyList(), Subject::class.java)
                        .toByteArray()))

                val schedules = async { database?.schedules()?.fetch() }
                files.add(saveAsFile(backupFolder, FILE_BACKUP_NAME_SCHEDULES,
                    encodeToJSON(schedules.await() ?: emptyList(), Schedule::class.java)
                        .toByteArray()))

                val tasks = async { database?.tasks()?.fetchCore() }
                files.add(saveAsFile(backupFolder, FILE_BACKUP_NAME_TASKS,
                    encodeToJSON(tasks.await() ?: emptyList(), Task::class.java)
                        .toByteArray()))

                val attachments = async { database?.attachments()?.fetch() }
                files.add(saveAsFile(backupFolder, FILE_BACKUP_NAME_ATTACHMENTS,
                    encodeToJSON(attachments.await() ?: emptyList(), Attachment::class.java)
                        .toByteArray()))

                val events = async { database?.events()?.fetchCore() }
                files.add(saveAsFile(backupFolder, FILE_BACKUP_NAME_EVENTS,
                    encodeToJSON(events.await() ?: emptyList(), Event::class.java)
                        .toByteArray()))

                val logs = async { database?.logs()?.fetchCore() }
                files.add(saveAsFile(backupFolder, FILE_BACKUP_NAME_LOGS,
                    encodeToJSON(logs.await() ?: emptyList(), Log::class.java)
                        .toByteArray()))

                zip(files, backupFolder.path)
            }

            manager?.notify(NOTIFICATION_BACKUP_SUCCESS,
                createNotification(R.string.notification_backup_success))

            PreferenceManager(this).backupSummary = DateTimeConverter
                .fromDateTime(DateTime.now())
        } catch (e: Exception) {
            e.printStackTrace()

            manager?.notify(NOTIFICATION_BACKUP_FAILED,
                createNotification(R.string.notification_backup_error))
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                stopForeground(true)
            else manager?.cancel(NOTIFICATION_BACKUP_ONGOING)
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

    private fun <T> encodeToJSON(items: List<T>, dataClass: Class<T>): String {
        if (items.isEmpty()) return ""

        val type = Types.newParameterizedType(List::class.java, dataClass)
        val adapter: JsonAdapter<List<T>> = moshi.adapter(type)
        return adapter.toJson(items)
    }

    private fun saveAsFile(parent: File, name: String, buffer: ByteArray): File {
        val targetFile = File(parent, name)
        targetFile.createNewFile()

        with(FileOutputStream(targetFile)) {
            write(buffer)
            flush()
            close()
        }
        return targetFile
    }

    private fun zip(files: List<File>, zipDirectory: String) {
        try {
            var origin: BufferedInputStream

            val buffer = 8096
            val destination = FileOutputStream("$zipDirectory/$FILE_BACKUP_NAME_ARCHIVE")
            val out = ZipOutputStream(BufferedOutputStream(destination))
            val data = ByteArray(buffer)

            files.forEach {
                val inputStream = FileInputStream(it)
                origin = BufferedInputStream(inputStream, buffer)

                val entry = ZipEntry(it.path.substring(it.path.lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count = 0

                while ({count = origin.read(data, 0, buffer); count }() != -1) {
                    out.write(data, 0, count);
                }
                it.delete()
                origin.close()
            }
            out.flush()
            out.close()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun terminateService(status: String = "") {
        if (status.isNotBlank())
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(ACTION_SERVICE_BROADCAST).apply {
                    putExtra(EXTRA_BROADCAST_STATUS, status)
                })
        stopSelf()
    }

    private fun createNotification(@StringRes id: Int, ongoing: Boolean = false): Notification {
        return NotificationCompat.Builder(this,
            NotificationChannelManager.CHANNEL_ID_BACKUP).apply {
            setSmallIcon(R.drawable.ic_outline_done_all_24)
            setContentTitle(getString(id))
            setOngoing(ongoing)
            setCategory(Notification.CATEGORY_SERVICE)
            setChannelId(NotificationChannelManager.CHANNEL_ID_BACKUP)
            color = ContextCompat.getColor(this@BackupRestoreService, R.color.color_primary)
        }.build()
    }

    companion object {
        const val FOLDER_BACKUP_NAME = "backups"
        const val FILE_BACKUP_NAME_ARCHIVE = "backup.zip"
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
    }
}