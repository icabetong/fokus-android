package com.isaiahvonrundstedt.fokus.components.service

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import com.isaiahvonrundstedt.fokus.components.extensions.android.getFileName
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import org.apache.commons.io.FileUtils
import java.io.File

class FileImporterService: BaseService() {

    private lateinit var targetDirectory: File

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                targetDirectory = File(getExternalFilesDir(null),
                    intent.getStringExtra(EXTRA_DIRECTORY) ?: Streamable.DIRECTORY_GENERIC)

                intent.data?.let { onStartCopy(it, intent.getStringExtra(EXTRA_FILE_NAME)) }
            }
            ACTION_CANCEL -> terminateService()
        }
        return START_REDELIVER_INTENT
    }

    private fun onStartCopy(uri: Uri, name: String? = null) {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            terminateService(BROADCAST_IMPORT_FAILED)
            return
        }
        sendLocalBroadcast(BROADCAST_IMPORT_ONGOING)

        try {
            contentResolver.openInputStream(uri)?.use {
                val imported = File(targetDirectory, name ?: uri.getFileName(this))
                FileUtils.copyToFile(it, imported)

                terminateService(BROADCAST_IMPORT_COMPLETED, imported.path)
            }
        } catch (e: Exception) {

            e.printStackTrace()
            terminateService(BROADCAST_IMPORT_FAILED)
        }
    }

    companion object {
        const val ACTION_START = "action:start"
        const val ACTION_CANCEL = "action:cancel"

        const val EXTRA_DIRECTORY = "extra:directory"
        const val EXTRA_FILE_NAME = "extra:filename"

        const val BROADCAST_IMPORT_ONGOING = "broadcast:attachment:ongoing"
        const val BROADCAST_IMPORT_COMPLETED = "broadcast:attachment:completed"
        const val BROADCAST_IMPORT_FAILED = "broadcast:attachment:failed"
    }
}