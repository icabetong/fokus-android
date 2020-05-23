package com.isaiahvonrundstedt.fokus.features.shared

import android.content.Context
import androidx.preference.PreferenceManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import org.joda.time.LocalTime

class PreferenceManager(private val context: Context?) {

    enum class Theme {
        SYSTEM, DARK, LIGHT;

        companion object {
            fun parse(s: String?): Theme {
                return when (s) {
                    DARK.toString() -> DARK
                    LIGHT.toString() -> LIGHT
                    else -> SYSTEM
                }
            }
        }
    }

    companion object {
        const val durationEveryday = "EVERYDAY"
        const val durationWeekends = "WEEKENDS"

        const val taskReminderIntervalHour = "1"
        const val taskReminderIntervalThreeHours = "3"
        const val taskReminderIntervalDay = "24"

        const val eventReminderIntervalQuarter = "15"
        const val eventReminderIntervalHalf = "30"
        const val eventReminderIntervalFull = "60"

        const val isFirstRunKey = "isFirstRunKey"
        const val nameKey = "usernamePreference"
        const val themeKey = "themePreference"
        const val soundKey = "soundPreference"
        const val frequencyKey = "frequencyPreference"
        const val reminderTimeKey = "timePreference"
        const val taskReminderKey = "taskReminderPreference"
        const val taskIntervalKey = "taskIntervalPreference"
        const val eventReminderKey = "eventReminderPreference"
        const val eventIntervalKey = "eventIntervalPreference"
        const val notificationKey = "notificationPreference"
        const val locationKey = "locationPreference"
        const val noticesKey = "noticesPreference"
        const val versionKey = "versionPreference"
    }

    var isFirstRun: Boolean
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getBoolean(isFirstRunKey, true) ?: true
        }
        set(value) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext).edit()
            editor.putBoolean(isFirstRunKey, value)
            editor.apply()
        }

    val name: String?
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getString(nameKey, context?.getString(R.string.app_name))
        }

    var theme: Theme
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return Theme.parse(shared?.getString(themeKey, Theme.SYSTEM.toString()))
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

    val reminderFrequency: String
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getString(frequencyKey, durationEveryday)!!
        }

    var reminderTime: LocalTime?
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return DateTimeConverter.toTime(
                shared.getString(reminderTimeKey, "08:30") ?: "08:30")
        }
        set(value) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext).edit()

            editor?.putString(reminderTimeKey, DateTimeConverter.fromTime(value))
            editor?.apply()
        }

    val taskReminder: Boolean
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getBoolean(taskReminderKey, true) ?: true
        }

    val taskReminderInterval: String
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getString(taskIntervalKey, taskReminderIntervalThreeHours) ?: taskReminderIntervalThreeHours
        }

    val eventReminder: Boolean
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getBoolean(eventReminderKey, true) ?: true
        }

    val eventReminderInterval: String
        get() {
            val shared = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
            return shared?.getString(eventIntervalKey, eventReminderIntervalHalf) ?: eventReminderIntervalHalf
        }
}