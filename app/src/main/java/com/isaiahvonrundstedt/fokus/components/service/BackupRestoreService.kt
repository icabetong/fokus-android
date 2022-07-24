package com.isaiahvonrundstedt.fokus.components.service

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.json.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.components.json.Metadata
import com.isaiahvonrundstedt.fokus.components.utils.DataArchiver
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.AppDatabase
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.sink
import org.apache.commons.io.FileUtils
import java.io.EOFException
import java.io.File
import java.io.InputStream
import java.time.ZonedDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class BackupRestoreService : BaseService() {

    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }

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
        startForegroundCompat(
            NOTIFICATION_RESTORE_ONGOING,
            createNotification(
                true, R.string.notification_restore_ongoing,
                iconRes = R.drawable.ic_outline_file_download_24
            )
        )

        try {
            val archiveStream: InputStream? = contentResolver.openInputStream(uri)
            val archive = DataArchiver.parseInputStream(this, archiveStream)

            archive.getInputStream(archive.getEntry(Metadata.FILE_NAME))?.use {
                val metadata = Metadata.fromInputStream(it)

                if (!metadata.verify(Metadata.DATA_BUNDLE)) {
                    stopForegroundCompat(NOTIFICATION_RESTORE_ONGOING)
                    manager?.notify(
                        NOTIFICATION_RESTORE_FAILED,
                        createNotification(titleRes = R.string.notification_restore_error)
                    )
                    terminateService()
                    archive.close()
                }
            }

            for (entry: ZipEntry in archive.entries()) {
                archive.getInputStream(entry)?.use {
                    tryParse(archive, entry, it)
                }
            }

            stopForegroundCompat(NOTIFICATION_RESTORE_ONGOING)
            manager?.notify(
                NOTIFICATION_RESTORE_SUCCESS,
                createNotification(titleRes = R.string.notification_restore_success)
            )
            terminateService()

            archive.close()
        } catch (e: EOFException) {
            e.printStackTrace()

            stopForegroundCompat(NOTIFICATION_RESTORE_ONGOING)
            manager?.notify(
                NOTIFICATION_RESTORE_FAILED,
                createNotification(
                    titleRes = R.string.notification_restore_error,
                    contentRes = R.string.feedback_restore_corrupted
                )
            )
            terminateService()
        } catch (e: Exception) {
            e.printStackTrace()

            stopForegroundCompat(NOTIFICATION_RESTORE_ONGOING)
            manager?.notify(
                NOTIFICATION_RESTORE_FAILED,
                createNotification(
                    titleRes = R.string.notification_restore_error,
                    contentRes = R.string.feedback_restore_invalid
                )
            )
            terminateService()
        }
    }

    private fun tryParse(archive: ZipFile, entry: ZipEntry, stream: InputStream) {
        if (entry.name == Streamable.FILE_NAME_SUBJECT) {
            JsonDataStreamer.decodeFromJson(stream, Subject::class.java)?.run {
                runBlocking { forEach { database.subjects().insert(it) } }
            }
        } else if (entry.name == Streamable.FILE_NAME_SCHEDULE) {
            JsonDataStreamer.decodeFromJson(stream, Schedule::class.java)?.run {
                runBlocking { forEach { database.schedules().insert(it) } }
            }
        } else if (entry.name == Streamable.FILE_NAME_TASK) {
            JsonDataStreamer.decodeFromJson(stream, Task::class.java)?.run {
                runBlocking { forEach { database.tasks().insert(it) } }
            }
        } else if (entry.name == Streamable.FILE_NAME_ATTACHMENT) {
            JsonDataStreamer.decodeFromJson(stream, Attachment::class.java)?.run {
                runBlocking { forEach { database.attachments().insert(it) } }
            }
        } else if (entry.name == Streamable.FILE_NAME_EVENT) {
            JsonDataStreamer.decodeFromJson(stream, Event::class.java)?.run {
                runBlocking { forEach { database.events().insert(it) } }
            }
        } else if (entry.name == Streamable.FILE_NAME_LOG) {
            JsonDataStreamer.decodeFromJson(stream, Log::class.java)?.run {
                runBlocking { forEach { database.logs().insert(it) } }
            }
        } else if (entry.name.contains(Streamable.DIRECTORY_ATTACHMENTS)
            && !entry.isDirectory
        ) {

            val targetDirectory = File(
                getExternalFilesDir(null),
                Streamable.DIRECTORY_ATTACHMENTS
            )

            val destination = File(targetDirectory, File(entry.name).name)

            archive.getInputStream(entry)?.use { inputStream ->
                FileUtils.copyToFile(inputStream, destination)
            }
        }
    }

    private fun startBackup(destination: Uri) {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED)
            return

        startForegroundCompat(
            NOTIFICATION_BACKUP_ONGOING,
            createNotification(
                ongoing = true, titleRes = R.string.notification_backup_ongoing,
                iconRes = R.drawable.ic_outline_file_upload_24
            )
        )

        try {
            runBlocking {
                val items = mutableListOf<File>()
                var fetchJob: Job

                fetchJob = async { database.subjects().fetch() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Subject::class.java)?.let {
                    items.add(createCache(Streamable.FILE_NAME_SUBJECT, it))
                }

                fetchJob = async { database.schedules().fetch() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Schedule::class.java)?.let {
                    items.add(createCache(Streamable.FILE_NAME_SCHEDULE, it))
                }

                fetchJob = async { database.tasks().fetch() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Task::class.java)?.let {
                    items.add(createCache(Streamable.FILE_NAME_TASK, it))
                }

                fetchJob = async { database.attachments().fetch() }
                val attachments: List<Attachment>? = fetchJob.await()
                JsonDataStreamer.encodeToJson(attachments, Attachment::class.java)?.let {
                    items.add(createCache(Streamable.FILE_NAME_ATTACHMENT, it))
                }
                val attachmentFolder = File(
                    cacheDir,
                    Streamable.DIRECTORY_ATTACHMENTS
                )
                if (!attachmentFolder.exists()) attachmentFolder.mkdir()
                attachments?.forEach {
                    if (it.type == Attachment.TYPE_IMPORTED_FILE && it.target != null)
                        FileUtils.copyFileToDirectory(
                            File(it.target!!),
                            attachmentFolder
                        )
                }
                items.add(attachmentFolder)

                fetchJob = async { database.events().fetch() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Event::class.java)?.let {
                    items.add(createCache(Streamable.FILE_NAME_EVENT, it))
                }

                fetchJob = async { database.logs().fetchCore() }
                JsonDataStreamer.encodeToJson(fetchJob.await(), Log::class.java)?.let {
                    items.add(createCache(Streamable.FILE_NAME_LOG, it))
                }

                items.add(
                    Metadata(data = Metadata.DATA_BUNDLE)
                        .toJsonFile(cacheDir, Metadata.FILE_NAME)
                )

                if (items.isEmpty()) {
                    stopForegroundCompat(NOTIFICATION_BACKUP_ONGOING)
                    terminateService(BROADCAST_BACKUP_EMPTY)
                }

                DataArchiver.Create(this@BackupRestoreService)
                    .addSource(items)
                    .toDestination(destination)
                    .start()

                PreferenceManager(this@BackupRestoreService)
                    .previousBackupDate = ZonedDateTime.now()

                stopForegroundCompat(NOTIFICATION_BACKUP_ONGOING)
                manager?.notify(
                    NOTIFICATION_BACKUP_SUCCESS,
                    createNotification(titleRes = R.string.notification_backup_success)
                )
                terminateService(BROADCAST_BACKUP_SUCCESS)
            }
        } catch (e: Exception) {
            e.printStackTrace()

            stopForegroundCompat(NOTIFICATION_BACKUP_ONGOING)
            manager?.notify(
                NOTIFICATION_BACKUP_FAILED,
                createNotification(titleRes = R.string.notification_backup_error)
            )
            terminateService(BROADCAST_BACKUP_FAILED)
        }
    }

    private fun createCache(name: String, json: String): File {
        return File(cacheDir, name).apply {
            this.sink().buffer().use {
                it.write(json.toByteArray())
                it.flush()
            }
        }
    }

    companion object {
        const val FILE_BACKUP_NAME = "backup"

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
    }
}