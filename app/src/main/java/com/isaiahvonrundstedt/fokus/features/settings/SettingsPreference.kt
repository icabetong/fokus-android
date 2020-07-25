package com.isaiahvonrundstedt.fokus.features.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.core.work.ReminderWorker
import com.isaiahvonrundstedt.fokus.features.core.work.event.EventNotificationScheduler
import com.isaiahvonrundstedt.fokus.features.core.work.task.TaskNotificationScheduler
import com.isaiahvonrundstedt.fokus.components.PermissionManager
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.core.activities.DokiActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

class SettingsPreference : BasePreference() {

    companion object {
        const val REQUEST_CODE_SOUND = 32
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_settings, rootKey)
    }

    private val preferences by lazy {
        PreferenceManager(requireContext())
    }
    private val manager by lazy {
        WorkManager.getInstance(requireContext())
    }

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

                        ReminderWorker.reschedule(requireContext())
                    }
                    positiveButton(R.string.button_done) { _ ->
                        it.summary = DateTimeFormat.forPattern(DateTimeConverter.timeFormat)
                            .print(preferences.reminderTime)
                    }
                }
                true
            }
        }

        findPreference<Preference>(R.string.key_custom_sound_uri)?.apply {
            setOnPreferenceClickListener {
                if (!PermissionManager(
                        requireContext()
                    ).storageReadGranted)
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        PermissionManager.REQUEST_CODE_STORAGE)
                else startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .setType("audio/*"), REQUEST_CODE_SOUND)
                true
            }
        }

        findPreference<Preference>(R.string.key_more_notification_settings)?.apply {
            setOnPreferenceClickListener {
                val intent = Intent()
                with(intent) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
                    } else {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", context?.packageName)
                        putExtra("app_uid", context?.applicationInfo?.uid)
                    }
                    startActivity(this)
                }

                true
            }
        }

        findPreference<Preference>(R.string.key_not_working_notifications)?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), DokiActivity::class.java))
                true
            }
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
        if (requestCode == PermissionManager.REQUEST_CODE_STORAGE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("audio/*"), REQUEST_CODE_SOUND)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SOUND && resultCode == Activity.RESULT_OK) {
            context?.contentResolver!!.takePersistableUriPermission(data?.data!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION)

            preferences.customSoundUri = data.data ?: Uri.parse(
                PreferenceManager.DEFAULT_SOUND)
        }
    }

}