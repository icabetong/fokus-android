package com.isaiahvonrundstedt.fokus.components.service

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.isaiahvonrundstedt.fokus.components.extensions.android.getFileName
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import org.apache.commons.io.FileUtils
import java.io.File

class AttachmentImportService: BaseService() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> intent.data?.let { onStartCopy(it) }
            ACTION_CANCEL -> terminateService()
        }
        return START_REDELIVER_INTENT
    }

    private fun onStartCopy(uri: Uri) {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            terminateService(BROADCAST_IMPORT_FAILED)
            return
        }

        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(ACTION_SERVICE_BROADCAST).apply {
                putExtra(EXTRA_BROADCAST_STATUS, BROADCAST_IMPORT_ONGOING)
            })

        try {
            contentResolver.openInputStream(uri)?.use {
                val imported = File(targetDirectory, uri.getFileName(this))
                FileUtils.copyToFile(it, imported)

                terminateService(BROADCAST_IMPORT_COMPLETED, Uri.fromFile(imported))
            }
        } catch (e: Exception) {

            e.printStackTrace()
            terminateService(BROADCAST_IMPORT_FAILED)
        }
    }

    private val targetDirectory: File
        get() = File(getExternalFilesDir(null), "imports")

    companion object {
        const val ACTION_START = "action:start"
        const val ACTION_CANCEL = "action:cancel"

        const val BROADCAST_IMPORT_ONGOING = "broadcast:import:ongoing"
        const val BROADCAST_IMPORT_COMPLETED = "broadcast:import:completed"
        const val BROADCAST_IMPORT_FAILED = "broadcast:import:failed"
    }
}