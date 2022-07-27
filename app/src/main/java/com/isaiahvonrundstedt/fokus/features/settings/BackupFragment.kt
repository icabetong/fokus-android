package com.isaiahvonrundstedt.fokus.features.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.startForegroundServiceCompat
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.BackupRestoreService
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.databinding.FragmentBackupBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject

@AndroidEntryPoint
class BackupFragment : BaseFragment() {
    private var _binding: FragmentBackupBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(binding.root, binding.appBarLayout.toolbar, emptyArray())

        with(binding.appBarLayout.toolbar) {
            setTitle(R.string.activity_backup)
            setupNavigation(this, R.drawable.ic_outline_arrow_back_24) {
                controller?.navigateUp()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
    }

    companion object {
        @AndroidEntryPoint
        class BackupPreference : BasePreference() {

            private lateinit var createLauncher: ActivityResultLauncher<Intent>
            private lateinit var restoreLauncher: ActivityResultLauncher<Intent>

            @Inject
            lateinit var manager: PreferenceManager

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                createLauncher =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        val service = Intent(context, BackupRestoreService::class.java).apply {
                            action = BackupRestoreService.ACTION_BACKUP
                            data = it.data?.data
                        }

                        context?.startForegroundServiceCompat(service)
                    }

                restoreLauncher =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        val service = Intent(context, BackupRestoreService::class.java).apply {
                            action = BackupRestoreService.ACTION_RESTORE
                            data = it.data?.data
                        }

                        context?.startForegroundServiceCompat(service)
                    }

                LocalBroadcastManager.getInstance(requireContext())
                    .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))
            }

            override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                setPreferencesFromResource(R.xml.xml_settings_backups, rootKey)
            }

            private var receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == BaseService.ACTION_SERVICE_BROADCAST) {
                        when (intent.getStringExtra(BaseService.EXTRA_BROADCAST_STATUS)) {
                            BackupRestoreService.BROADCAST_BACKUP_SUCCESS ->
                                setPreferenceSummary(
                                    PreferenceManager.PREFERENCE_BACKUP,
                                    manager.previousBackupDate.parseForSummary()
                                )
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

                setPreferenceSummary(
                    PreferenceManager.PREFERENCE_BACKUP,
                    manager.previousBackupDate.parseForSummary()
                )
                findPreference<Preference>(PreferenceManager.PREFERENCE_BACKUP)
                    ?.setOnPreferenceClickListener {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(Intent.EXTRA_TITLE, BackupRestoreService.FILE_BACKUP_NAME)
                            type = Streamable.MIME_TYPE_ZIP
                        }

                        createLauncher.launch(intent)
                        true
                    }


                findPreference<Preference>(PreferenceManager.PREFERENCE_RESTORE)
                    ?.setOnPreferenceClickListener {
                        val intent =
                            Intent.createChooser(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                type = Streamable.MIME_TYPE_ZIP
                            }, getString(R.string.dialog_choose_backup))

                        restoreLauncher.launch(intent)
                        true
                    }

            }

            override fun onDestroy() {
                LocalBroadcastManager.getInstance(requireContext())
                    .unregisterReceiver(receiver)
                super.onDestroy()
            }

            private fun ZonedDateTime?.parseForSummary(): String? {
                if (this == null)
                    return getString(R.string.settings_backup_summary_no_previous)

                val currentDateTime = ZonedDateTime.now()

                return if (this.toLocalDate().isEqual(LocalDate.now()))
                    String.format(
                        getString(R.string.today_at),
                        format(DateTimeConverter.getTimeFormatter(requireContext()))
                    )
                else if (this.minusDays(1)?.compareTo(currentDateTime) == 0)
                    String.format(
                        getString(R.string.yesterday_at),
                        format(DateTimeConverter.getTimeFormatter(requireContext()))
                    )
                else if (this.plusDays(1)?.compareTo(currentDateTime) == 0)
                    String.format(
                        getString(R.string.tomorrow_at),
                        format(DateTimeConverter.getTimeFormatter(requireContext()))
                    )
                else format(DateTimeConverter.getDateTimeFormatter(requireContext()))
            }
        }
    }
}