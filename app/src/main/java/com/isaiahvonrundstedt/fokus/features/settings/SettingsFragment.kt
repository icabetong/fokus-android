package com.isaiahvonrundstedt.fokus.features.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.work.ReminderWorker
import com.isaiahvonrundstedt.fokus.features.core.work.event.EventNotificationScheduler
import com.isaiahvonrundstedt.fokus.features.core.work.task.TaskNotificationScheduler
import com.isaiahvonrundstedt.fokus.features.shared.PermissionManager
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import dev.doubledot.doki.ui.DokiActivity
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

class SettingsFragment: BasePreference() {

    companion object {
        const val soundRequestCode = 32
        const val url = "https://github.com/reichsadmiral/fokus/issues/new"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_preferences, rootKey)
    }

    private val preferences by lazy { PreferenceManager(requireContext()) }
    private val manager by lazy { WorkManager.getInstance(requireContext()) }

    override fun onStart() {
        super.onStart()

        findPreference<ListPreference>(R.string.key_theme)?.apply {
            setOnPreferenceChangeListener { _, value ->
                if (value is String) {
                    val theme = value.toString()
                    notifyAppCompatDelegate(theme)
                }
                true
            }
        }

        findPreference<Preference>(R.string.key_task_reminder_interval)?.apply {
            setOnPreferenceChangeListener { _, _ ->
                val request = OneTimeWorkRequest.Builder(TaskNotificationScheduler::class.java)
                    .addTag(TaskNotificationScheduler::class.java.simpleName)
                    .build()

                manager.enqueue(request)
                true
            }
        }

        findPreference<Preference>(R.string.key_event_reminder_interval)?.apply {
            setOnPreferenceChangeListener { _, _ ->
                val request = OneTimeWorkRequest.Builder(EventNotificationScheduler::class.java)
                    .addTag(EventNotificationScheduler::class.java.simpleName)
                    .build()

                manager.enqueue(request)
                true
            }
        }

        findPreference<Preference>(R.string.key_reminder_time)?.apply {
            summary = DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(preferences.reminderTime)
            setOnPreferenceClickListener {
                MaterialDialog(requireContext()).show {
                    timePicker(show24HoursView = false) { _, datetime ->
                        preferences.reminderTime = LocalTime.fromCalendarFields(datetime)

                        ReminderWorker.Scheduler()
                            .setTargetTime(preferences.reminderTime?.toDateTimeToday())
                            .removePrevious(true)
                            .schedule(requireContext())
                    }
                    positiveButton(R.string.button_done) { _ ->
                        it.summary = DateTimeFormat.forPattern(DateTimeConverter.timeFormat)
                            .print(preferences.reminderTime)
                    }
                }
                true
            }
        }

        findPreference<Preference>(R.string.key_custom_sound)?.apply {
            setOnPreferenceClickListener {
                if (!PermissionManager(requireContext()).readAccessGranted)
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                       PermissionManager.readStorageRequestCode)
                else startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                                                .setType("audio/*"), soundRequestCode)
                true
            }
        }

        findPreference<Preference>(R.string.key_report_issue)?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                true
            }
        }

        findPreference<Preference>(R.string.key_version)?.apply {
            summary = BuildConfig.VERSION_NAME
        }
    }

    private fun notifyAppCompatDelegate(newTheme: String) {
        when (PreferenceManager.Theme.parse(newTheme)) {
            PreferenceManager.Theme.DARK ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            PreferenceManager.Theme.LIGHT ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            PreferenceManager.Theme.SYSTEM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.readStorageRequestCode
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("audio/*"), soundRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == soundRequestCode && resultCode == Activity.RESULT_OK) {
            context?.contentResolver!!.takePersistableUriPermission(data?.data!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION)

            preferences.soundUri = data.data ?: Uri.parse(PreferenceManager.defaultSound)
        }
    }

}