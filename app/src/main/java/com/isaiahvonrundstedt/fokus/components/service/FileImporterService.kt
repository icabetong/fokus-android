package com.isaiahvonrundstedt.fokus.components.service

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.isaiahvonrundstedt.fokus.components.extensions.android.getFileName
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import org.apache.commons.io.FileUtils
import java.io.File

class FileImporterService : BaseService() {

    private lateinit var targetDirectory: File

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                targetDirectory = File(
                    getExternalFilesDir(null),
                    Streamable.DIRECTORY_ATTACHMENTS
                )

                intent.data?.let { onStartCopy(it, intent.getStringExtra(EXTRA_OBJECT_ID)!!) }
            }
            ACTION_CANCEL -> terminateService()
        }
        return START_REDELIVER_INTENT
    }

    private fun onStartCopy(uri: Uri, id: String) {
        // Check if we have access to the storage
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            terminateService(BROADCAST_IMPORT_FAILED)
            return
        }
        sendLocalBroadcast(BROADCAST_IMPORT_ONGOING)

        try {
            contentResolver.openInputStream(uri)?.use {
                // Use the attachment id to link the raw file
                // to the database
                // note: need to remove the target column in the database as it becomes redundant.
                val fileName = uri.getFileName(this)
                val extension = File(fileName).extension

                val targetFile = File(targetDirectory, "${id}.${extension}")
                FileUtils.copyToFile(it, targetFile)

                broadcastResultThenTerminate(id, fileName)
            }
        } catch (e: Exception) {

            e.printStackTrace()
            terminateService(BROADCAST_IMPORT_FAILED)
        }
    }

    /**
     *  Sends the required data back to the activity then
     *  terminates itself.
     *  @param id the attachment id that the file will be linked in
     *  @param name the original file name of the file
     */
    private fun broadcastResultThenTerminate(id: String, name: String) {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(ACTION_SERVICE_BROADCAST).apply {

                // This function is only called when the import is
                // completed and therefore we should just
                // put a completed status in the broadcast
                putExtra(EXTRA_BROADCAST_STATUS, BROADCAST_IMPORT_COMPLETED)

                // Send the attachment id back to the calling activity
                putExtra(EXTRA_BROADCAST_DATA, id)

                // Send the file name back to the calling activity
                putExtra(EXTRA_BROADCAST_EXTRA, name)
            })
        stopSelf()
    }

    companion object {
        const val ACTION_START = "action:start"
        const val ACTION_CANCEL = "action:cancel"

        const val EXTRA_OBJECT_ID = "extra:id"
        const val EXTRA_BROADCAST_EXTRA = "extra:broadcast:extra"

        const val BROADCAST_IMPORT_ONGOING = "broadcast:attachment:ongoing"
        const val BROADCAST_IMPORT_COMPLETED = "broadcast:attachment:completed"
        const val BROADCAST_IMPORT_FAILED = "broadcast:attachment:failed"
    }
}