package com.isaiahvonrundstedt.fokus.features.settings

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.startForegroundServiceCompat
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.BackupRestoreService
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import kotlinx.android.synthetic.main.layout_appbar.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

class BackupActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.settings_backup_and_restore)
    }

    companion object {
        class BackupFragment: BasePreference() {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                LocalBroadcastManager.getInstance(requireContext())
                    .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))
            }

            override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                setPreferencesFromResource(R.xml.xml_settings_backups, rootKey)
            }

            private var receiver = object: BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == BaseService.ACTION_SERVICE_BROADCAST) {
                        when (intent.getStringExtra(BaseService.EXTRA_BROADCAST_STATUS)) {
                            BackupRestoreService.BROADCAST_BACKUP_SUCCESS ->
                                setPreferenceSummary(R.string.key_backup,
                                    manager.previousBackupDate.parseForSummary())
                            BackupRestoreService.BROADCAST_BACKUP_FAILED ->
                                createSnackbar(R.string.feedback_backup_failed, requireView())
                            BackupRestoreService.BROADCAST_BACKUP_EMPTY ->
                                createSnackbar(R.string.feedback_backup_empty, requireView())
                        }
                    }
                }
            }

            override fun onStart() {
                super.onStart()

                setPreferenceSummary(R.string.key_backup, manager.previousBackupDate.parseForSummary())
                findPreference<Preference>(R.string.key_backup)
                    ?.setOnPreferenceClickListener {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(Intent.EXTRA_TITLE, BackupRestoreService.FILE_BACKUP_NAME)
                        type = Streamable.MIME_TYPE_ZIP
                    }
                    startActivityForResult(intent, REQUEST_CODE_BACKUP_FILE)
                    true
                }


                findPreference<Preference>(R.string.key_restore)
                    ?.setOnPreferenceClickListener {
                        val chooserIntent = Intent.createChooser(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            type = Streamable.MIME_TYPE_ZIP
                        }, getString(R.string.dialog_choose_backup))
                        startActivityForResult(chooserIntent, REQUEST_CODE_RESTORE_FILE)
                        true
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

                context?.startForegroundServiceCompat(service)
            }

            private fun startBackupService(uri: Uri) {
                val service = Intent(context, BackupRestoreService::class.java).apply {
                    action = BackupRestoreService.ACTION_BACKUP
                    data = uri
                }

                context?.startForegroundServiceCompat(service)
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
    }
}