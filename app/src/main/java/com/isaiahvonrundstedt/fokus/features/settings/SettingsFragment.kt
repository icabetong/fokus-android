package com.isaiahvonrundstedt.fokus.features.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.work.DeadlineIntervalScheduler
import com.isaiahvonrundstedt.fokus.features.core.work.TaskReminderNotifier
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import dev.doubledot.doki.ui.DokiActivity
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

class SettingsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_preferences, rootKey)
    }

    override fun onStart() {
        super.onStart()

        findPreference<ListPreference>(PreferenceManager.themeKey)
            ?.onPreferenceChangeListener = changeListener

        findPreference<Preference>(PreferenceManager.intervalKey)
            ?.onPreferenceChangeListener = changeListener

        val remindTimePreference = findPreference<Preference>(PreferenceManager.timeKey)
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
            PreferenceManager.Theme.ALWAYS ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            PreferenceManager.Theme.NEVER ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            PreferenceManager.Theme.AUTOMATIC -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Configuration.UI_MODE_NIGHT_NO ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Configuration.UI_MODE_NIGHT_UNDEFINED ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
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
            PreferenceManager.intervalKey -> {
                val request = OneTimeWorkRequestBuilder<DeadlineIntervalScheduler>()
                    .addTag(DeadlineIntervalScheduler::class.java.simpleName)
                    .build()
                WorkManager.getInstance(requireContext()).enqueue(request)
                true
            }
            else -> false
        }
    }

    private var clickListener = Preference.OnPreferenceClickListener {
        when (it.key) {
            PreferenceManager.timeKey -> {
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
                LibsBuilder()
                    .withActivityTitle(getString(R.string.settings_third_party_notices))
                    .withAboutIconShown(false)
                    .withLicenseDialog(true)
                    .withActivityStyle(Libs.ActivityStyle.LIGHT)
                    .start(requireContext())
            }
        }
        return@OnPreferenceClickListener true
    }

    private fun scheduleNextReminder() {
        TaskReminderNotifier.Scheduler()
            .setTargetTime(PreferenceManager(requireContext()).reminderTime?.toDateTimeToday())
            .removePrevious(true)
            .schedule(requireContext())
    }
}