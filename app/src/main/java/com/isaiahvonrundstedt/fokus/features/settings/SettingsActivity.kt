package com.isaiahvonrundstedt.fokus.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toLocalTime
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.databinding.ActivitySettingsBinding
import com.isaiahvonrundstedt.fokus.features.notifications.event.EventNotificationScheduler
import com.isaiahvonrundstedt.fokus.features.notifications.subject.ClassNotificationScheduler
import com.isaiahvonrundstedt.fokus.features.notifications.task.TaskNotificationScheduler
import com.isaiahvonrundstedt.fokus.features.notifications.task.TaskReminderWorker
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import java.util.*

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBarLayout.toolbar)
        setToolbarTitle(R.string.activity_settings)
    }

    companion object {
        class SettingsFragment: BasePreference() {

            override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                setPreferencesFromResource(R.xml.xml_settings_main, rootKey)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)

                findPreference<ListPreference>(PreferenceManager.PREFERENCE_THEME)
                    ?.setOnPreferenceChangeListener { _, value ->
                        if (value is String) {
                            val theme = value.toString()
                            notifyThemeChanged(PreferenceManager.Theme.parse(theme))
                        }
                        true
                    }

                findPreference<SwitchPreferenceCompat>(PreferenceManager.PREFERENCE_TASK_NOTIFICATION)
                    ?.setOnPreferenceChangeListener { _, isChecked ->
                        if (isChecked is Boolean) {
                            val workerClass = TaskNotificationScheduler::class.java
                            if (isChecked)
                                scheduleWorker(workerClass)
                            else cancelWorker(workerClass)
                        } else false
                    }

                findPreference<SwitchPreferenceCompat>(PreferenceManager.PREFERENCE_EVENT_NOTIFICATION)
                    ?.setOnPreferenceChangeListener { _, isChecked ->
                        if (isChecked is Boolean) {
                            val workerClass = EventNotificationScheduler::class.java
                            if (isChecked)
                                scheduleWorker(workerClass)
                            else cancelWorker(workerClass)
                        } else false
                    }

                findPreference<SwitchPreferenceCompat>(PreferenceManager.PREFERENCE_COURSE_NOTIFICATION)
                    ?.setOnPreferenceChangeListener { _, isChecked ->
                        if (isChecked is Boolean) {
                            val workerClass = ClassNotificationScheduler::class.java
                            if (isChecked)
                                scheduleWorker(workerClass)
                            else cancelWorker(workerClass)
                        } else false
                    }

                findPreference<Preference>(PreferenceManager.PREFERENCE_TASK_NOTIFICATION_INTERVAL)
                    ?.setOnPreferenceChangeListener { _, _ ->
                        scheduleWorker(TaskNotificationScheduler::class.java)
                    }


                findPreference<Preference>(PreferenceManager.PREFERENCE_EVENT_NOTIFICATION_INTERVAL)
                    ?.setOnPreferenceChangeListener { _, _ ->
                        scheduleWorker(EventNotificationScheduler::class.java)
                    }

                findPreference<Preference>(PreferenceManager.PREFERENCE_COURSE_NOTIFICATION_INTERVAL)
                    ?.setOnPreferenceChangeListener { _, _ ->
                        scheduleWorker(ClassNotificationScheduler::class.java)
                    }

                setPreferenceSummary(PreferenceManager.PREFERENCE_REMINDER_TIME,
                    preferences.reminderTime?.format(DateTimeConverter.getTimeFormatter(requireContext())))
                findPreference<Preference>(PreferenceManager.PREFERENCE_REMINDER_TIME)
                    ?.setOnPreferenceClickListener {
                        MaterialDialog(requireContext()).show {
                            timePicker(show24HoursView = false) { _, time ->
                                preferences.reminderTime = time.toLocalTime()

                                TaskReminderWorker.reschedule(requireContext())
                            }
                            positiveButton(R.string.button_done) { _ ->
                                it.summary = preferences.reminderTime
                                    ?.format(DateTimeConverter.getTimeFormatter(requireContext()))
                            }
                        }
                        true
                    }

                findPreference<Preference>(PreferenceManager.PREFERENCE_SYSTEM_NOTIFICATION)
                    ?.setOnPreferenceClickListener {
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


                findPreference<Preference>(PreferenceManager.PREFERENCE_BACKUP_RESTORE)
                    ?.setOnPreferenceClickListener {
                        startActivity(Intent(context, BackupActivity::class.java))
                        true
                    }


                findPreference<Preference>(PreferenceManager.PREFERENCE_BATTERY_OPTIMIZATION)
                    ?.setOnPreferenceClickListener {
                        val manufacturerArray = resources.getStringArray(R.array.oem_battery_optimization)

                        var manufacturer = Build.MANUFACTURER.toLowerCase(Locale.getDefault())
                        if (!manufacturerArray.contains(manufacturer))
                            manufacturer = "generic"

                        CustomTabsIntent.Builder().build()
                            .launchUrl(requireContext(),
                                Uri.parse(SETTINGS_URL_BATTERY_OPTIMIZATION + manufacturer))

                        true
                    }
            }

            private fun notifyThemeChanged(theme: PreferenceManager.Theme) {
                when (theme) {
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

            private fun <T: BaseWorker> cancelWorker(worker: Class<T>): Boolean {
                try {
                    WorkManager.getInstance(requireContext())
                        .cancelAllWorkByTag(worker.simpleName)

                    return true
                } catch (e: Exception) { e.printStackTrace() }
                return false
            }

            private fun <T: BaseWorker> scheduleWorker(worker: Class<T>): Boolean {
                try {
                    val request = OneTimeWorkRequest.Builder(worker)
                        .addTag(worker.simpleName)
                        .build()

                    WorkManager.getInstance(requireContext())
                        .enqueue(request)

                    return true
                } catch (e: Exception) { e.printStackTrace() }
                return false
            }

            private val preferences by lazy {
                PreferenceManager(requireContext())
            }

            companion object {
                const val SETTINGS_URL_BATTERY_OPTIMIZATION = "https://www.dontkillmyapp.com/"
            }
        }
    }

}