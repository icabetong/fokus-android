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
import org.joda.time.LocalDate
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
                        summary = manager.backupDate.parseForSummary()
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        findPreference<Preference>(R.string.key_backup)?.apply {
            summary = manager.backupDate.parseForSummary()
            setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, BackupRestoreService.FILE_BACKUP_NAME_ARCHIVE)
                    type = BackupRestoreService.MIME_TYPE_ZIP
                }
                startActivityForResult(intent, REQUEST_CODE_BACKUP_FILE)
                true
            }
        }

        findPreference<Preference>(R.string.key_restore)?.apply {
            setOnPreferenceClickListener {
                val chooserIntent = Intent.createChooser(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = BackupRestoreService.MIME_TYPE_ZIP
                }, getString(R.string.dialog_choose_backup))
                startActivityForResult(chooserIntent, REQUEST_CODE_RESTORE_FILE)
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
        if (resultCode != Activity.RESULT_OK)
            return

        data?.data?.also {
            when (requestCode) {
                REQUEST_CODE_BACKUP_FILE ->
                    startBackupService(it)
                REQUEST_CODE_RESTORE_FILE -> {
                    startRestoreProcedure(it)
                }
            }
        }
    }

    private fun startRestoreProcedure(uri: Uri) {
        val service = Intent(context, BackupRestoreService::class.java).apply {
            action = BackupRestoreService.ACTION_RESTORE
            data = uri
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(service)
        else context?.startService(service)
    }

    private fun startBackupService(uri: Uri) {
        val service = Intent(context, BackupRestoreService::class.java).apply {
            action = BackupRestoreService.ACTION_BACKUP
            data = uri
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(service)
        else context?.startService(service)
    }

    private val manager by lazy {
        PreferenceManager(requireContext())
    }

    private fun DateTime?.parseForSummary(): String {
        if (this == null)
            return getString(R.string.settings_backup_summary_no_previous)

        val currentDateTime = DateTime.now()

        return if (this.toLocalDate().isEqual(LocalDate.now()))
            String.format(getString(R.string.today_at),
                DateTimeFormat.forPattern(DateTimeConverter.FORMAT_TIME).print(this))
        else if (this.minusDays(1)?.compareTo(currentDateTime) == 0)
            String.format(getString(R.string.yesterday_at),
                DateTimeFormat.forPattern(DateTimeConverter.FORMAT_TIME).print(this))
        else if (this.plusDays(1)?.compareTo(currentDateTime) == 0)
            String.format(getString(R.string.tomorrow_at),
                DateTimeFormat.forPattern(DateTimeConverter.FORMAT_TIME).print(this))
        else DateTimeFormat.forPattern(DateTimeConverter.FORMAT_DATE).print(this)
    }

    companion object {
        const val REQUEST_CODE_BACKUP_FILE = 43
        const val REQUEST_CODE_RESTORE_FILE = 78
    }
}