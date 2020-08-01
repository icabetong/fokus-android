package com.isaiahvonrundstedt.fokus.features.settings.child

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PermissionManager
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.components.service.BackupRestoreService
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class BackupRestorePreference: BasePreference() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(BackupRestoreService.ACTION_SERVICE_BROADCAST))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_settings_backups, rootKey)
    }

    private var receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BackupRestoreService.ACTION_SERVICE_BROADCAST) {
                if (intent.getStringExtra(BackupRestoreService.EXTRA_BROADCAST_STATUS)
                    == BackupRestoreService.BROADCAST_BACKUP_SUCCESS)
                    findPreference<Preference>(R.string.key_backup)?.apply {
                        summary = manager.backupSummary
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        findPreference<Preference>(R.string.key_backup)?.apply {
            summary = manager.backupSummary
            setOnPreferenceClickListener {
                startBackupService()
                true
            }
        }

        findPreference<Preference>(R.string.key_restore)?.apply {
            setOnPreferenceClickListener {
                val chooserIntent = Intent.createChooser(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                }, "Choose file")
                startActivityForResult(chooserIntent, REQUEST_CODE_BACKUP_FILE)
                true
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BACKUP_FILE &&
                resultCode == Activity.RESULT_OK)
            data?.data?.let { startRestoreProcedure(it) }
    }

    private fun startRestoreProcedure(uri: Uri) {
        val service = Intent(context, BackupRestoreService::class.java)
        service.action = BackupRestoreService.ACTION_RESTORE
        service.data = uri

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(service)
        else context?.startService(service)
    }

    private fun startBackupService() {
        val service = Intent(context, BackupRestoreService::class.java)
        service.action = BackupRestoreService.ACTION_BACKUP

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(service)
        else context?.startService(service)
    }

    private val manager by lazy {
        PreferenceManager(requireContext())
    }

    companion object {
        const val REQUEST_CODE_BACKUP_FILE = 43
    }
}