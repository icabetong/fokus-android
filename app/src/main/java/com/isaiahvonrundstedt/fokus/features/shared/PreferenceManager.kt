package com.isaiahvonrundstedt.fokus.features.shared

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
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

    private val sharedPreference by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    val name: String?
        get() = sharedPreference.getString(R.string.key_username, context?.getString(R.string.app_name))

    var theme: Theme
        get() = Theme.parse(sharedPreference.getString(R.string.key_theme, Theme.SYSTEM.toString()))
        set(value) {
            sharedPreference.edit().run {
                putString(R.string.key_theme, value.toString())
                apply()
            }
        }

    val confettiEnabled: Boolean
        get() = sharedPreference.getBoolean(R.string.key_confetti, true)

    val soundEnabled: Boolean
        get() = sharedPreference.getBoolean(R.string.key_sound, true)

    val customSoundEnabled: Boolean
        get() = sharedPreference.getBoolean(R.string.key_custom_sound, false)

    var customSoundUri: Uri
        get() = Uri.parse(sharedPreference.getString(R.string.key_custom_sound_uri, defaultSound))
        set(value) {
            sharedPreference.edit().run {
                putString(R.string.key_custom_sound_uri, value.toString())
                apply()
            }
        }

    val reminderFrequency: String
        get() = sharedPreference.getString(R.string.key_reminder_frequency, durationEveryday)
            ?: durationEveryday

    var reminderTime: LocalTime?
        get() = DateTimeConverter.toTime(
            sharedPreference.getString(R.string.key_reminder_time, "08:30") ?: "08:30")
        set(value) {
            sharedPreference.edit().run {
                putString(R.string.key_reminder_time, DateTimeConverter.fromTime(value))
                apply()
            }
        }

    val taskReminder: Boolean
        get() = sharedPreference.getBoolean(R.string.key_task_reminder, true)

    val taskReminderInterval: String
        get() = sharedPreference.getString(R.string.key_task_reminder_interval,
                                           taskReminderIntervalThreeHours) ?: taskReminderIntervalThreeHours

    val eventReminder: Boolean
        get() = sharedPreference.getBoolean(R.string.key_event_reminder, true)

    val eventReminderInterval: String
        get() = sharedPreference.getString(R.string.key_event_reminder_interval,
                                           eventReminderIntervalHalf) ?: eventReminderIntervalHalf

    /**
     *  Extension functions for SharedPreference object
     *  to accept String Resource ID as Keys
     */
    private fun SharedPreferences.getString(@StringRes id: Int, default: String?): String? {
        return this.getString(context?.getString(id), default)
    }

    private fun SharedPreferences.getBoolean(@StringRes id: Int, default: Boolean): Boolean {
        return this.getBoolean(context?.getString(id), default)
    }

    private fun SharedPreferences.Editor.putString(@StringRes id: Int, value: String?) {
        this.putString(context?.getString(id), value)
    }

    companion object {
        const val defaultSound = "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${BuildConfig.APPLICATION_ID}/${R.raw.fokus}"

        const val durationEveryday = "EVERYDAY"
        const val durationWeekends = "WEEKENDS"

        const val taskReminderIntervalHour = "1"
        const val taskReminderIntervalThreeHours = "3"
        const val taskReminderIntervalDay = "24"

        const val eventReminderIntervalQuarter = "15"
        const val eventReminderIntervalHalf = "30"
        const val eventReminderIntervalFull = "60"

        val defaultSoundUri: Uri
            get() = Uri.parse(defaultSound)
    }
}