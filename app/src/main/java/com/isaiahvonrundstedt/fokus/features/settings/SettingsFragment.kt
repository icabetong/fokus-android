package com.isaiahvonrundstedt.fokus.features.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.work.event.EventNotificationScheduler
import com.isaiahvonrundstedt.fokus.features.core.work.task.TaskNotificationScheduler
import com.isaiahvonrundstedt.fokus.features.core.work.ReminderWorker
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import dev.doubledot.doki.ui.DokiActivity
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

class SettingsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_preferences, rootKey)
    }

    private val manager by lazy {
        WorkManager.getInstance(requireContext())
    }

    override fun onStart() {
        super.onStart()

        findPreference<ListPreference>(PreferenceManager.themeKey)
            ?.onPreferenceChangeListener = changeListener

        findPreference<Preference>(PreferenceManager.taskIntervalKey)
            ?.onPreferenceChangeListener = changeListener

        findPreference<Preference>(PreferenceManager.eventIntervalKey)
            ?.onPreferenceChangeListener = changeListener

        val remindTimePreference = findPreference<Preference>(PreferenceManager.reminderTimeKey)
        remindTimePreference?.summary = DateTimeFormat.forPattern(DateTimeConverter.timeFormat)
            .print(PreferenceManager(context).reminderTime)
        remindTimePreference?.onPreferenceClickListener = clickListener

        findPreference<Preference>(PreferenceManager.notificationKey)
            ?.onPreferenceClickListener = clickListener

        findPreference<Preference>(PreferenceManager.locationKey)
            ?.onPreferenceClickListener = clickListener

        findPreference<Preference>(PreferenceManager.noticesKey)
            ?.onPreferenceClickListener = clickListener

        findPreference<Preference>(PreferenceManager.versionKey)
            ?.summary = BuildConfig.VERSION_NAME
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

    private var changeListener = Preference.OnPreferenceChangeListener { preference, value ->
        return@OnPreferenceChangeListener when (preference.key) {
            PreferenceManager.themeKey -> {
                if (value is String) {
                    val theme = value.toString()
                    notifyAppCompatDelegate(theme)
                }
                true
            }
            PreferenceManager.taskIntervalKey -> {
                val request = OneTimeWorkRequest.Builder(TaskNotificationScheduler::class.java)
                    .addTag(TaskNotificationScheduler::class.java.simpleName)
                    .build()

                manager.enqueue(request)
                true
            }
            PreferenceManager.eventIntervalKey -> {
                val request = OneTimeWorkRequest.Builder(EventNotificationScheduler::class.java)
                    .addTag(EventNotificationScheduler::class.java.simpleName)
                    .build()

                manager.enqueue(request)
                true
            }
            else -> false
        }
    }

    private var clickListener = Preference.OnPreferenceClickListener {
        when (it.key) {
            PreferenceManager.reminderTimeKey -> {
                MaterialDialog(requireContext()).show {
                    timePicker(show24HoursView = false) { _, datetime ->
                        val selectedTime = LocalTime.fromCalendarFields(datetime)
                        PreferenceManager(context).reminderTime = selectedTime

                        scheduleNextReminder()
                    }
                    positiveButton(R.string.button_done) { _ ->
                        it.summary = DateTimeFormat.forPattern(DateTimeConverter.timeFormat)
                            .print(PreferenceManager(context).reminderTime)
                    }
                }
            }
            PreferenceManager.notificationKey -> {
                DokiActivity.start(requireContext())
            }
            PreferenceManager.noticesKey -> {
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.activity_notices))
                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            }
        }
        return@OnPreferenceClickListener true
    }

    private fun scheduleNextReminder() {
        ReminderWorker.Scheduler()
            .setTargetTime(PreferenceManager(requireContext()).reminderTime?.toDateTimeToday())
            .removePrevious(true)
            .schedule(requireContext())
    }
}