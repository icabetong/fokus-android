package com.isaiahvonrundstedt.fokus.features.shared

import android.content.Context
import androidx.preference.PreferenceManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import org.joda.time.LocalTime

class PreferenceManager(private val context: Context?) {

    enum class Theme {
        AUTOMATIC, ALWAYS, NEVER;

        companion object {
            fun parse(s: String?): Theme {
                return when (s) {
                    ALWAYS.toString() -> ALWAYS
                    NEVER.toString() -> NEVER
                    else -> AUTOMATIC
                }
            }
        }
    }

    companion object {
        const val durationEveryday = "EVERYDAY"
        const val durationWeekends = "WEEKENDS"

        const val dueDelayHour = "HOUR"
        const val dueDelayThreeHours = "THREE"
        const val dueDelayDay = "DAY"

        const val nameKey = "usernamePreference"
        const val themeKey = "themePreference"
        const val soundKey = "soundPreference"
        const val archiveKey = "archivePreference"
        const val frequencyKey = "frequencyPreference"
        const val timeKey = "timePreference"
        const val dueAlertKey = "dueAlertPreference"
        const val intervalKey = "intervalPreference"
        const val notificationKey = "notificationPreference"
        const val locationKey = "locationPreference"
        const val noticesKey = "noticesPreference"
        const val versionKey = "versionPreference"
    }

    val name: String?
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getString(nameKey, context?.getString(R.string.app_name))
        }

    var theme: Theme
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return Theme.parse(shared?.getString(themeKey, Theme.AUTOMATIC.toString()))
        }
        set(value) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext).edit()

            editor?.putString(themeKey, value.toString())
            editor?.apply()
        }

    val completedSounds: Boolean
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getBoolean(soundKey, true) ?: true
        }

    var autoArchive: Boolean
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getBoolean(archiveKey, false) ?: false
        }
        set(value) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext).edit()

            editor?.putBoolean(archiveKey, value)
            editor?.apply()
        }

    val reminderFrequency: String
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getString(frequencyKey, durationEveryday)!!
        }

    var reminderTime: LocalTime?
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return DateTimeConverter.toTime(
                shared.getString(timeKey, "08:30") ?: "08:30")
        }
        set(value) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext).edit()

            editor?.putString(timeKey, DateTimeConverter.fromTime(value))
            editor?.apply()
        }

    val remindWhenDue: Boolean
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getBoolean(dueAlertKey, true) ?: true
        }

    val dueInterval: String
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getString(intervalKey, dueDelayThreeHours) ?: dueDelayThreeHours
        }
}