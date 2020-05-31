package com.isaiahvonrundstedt.fokus.features.shared

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.isaiahvonrundstedt.fokus.BuildConfig
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

    private val sharedPreference by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    var isFirstRun: Boolean
        get() = sharedPreference?.getBoolean(getKey(R.string.key_is_first_run), true) ?: true
        set(value) {
            sharedPreference.edit().run {
                putBoolean(getKey(R.string.key_is_first_run), value)
                apply()
            }
        }

    val name: String?
        get() = sharedPreference.getString(getKey(R.string.key_username), context?.getString(R.string.app_name))

    var theme: Theme
        get() = Theme.parse(sharedPreference.getString(getKey(R.string.key_theme), Theme.SYSTEM.toString()))
        set(value) {
            sharedPreference.edit().run {
                putString(getKey(R.string.key_theme), value.toString())
                apply()
            }
        }

    val soundEnabled: Boolean
        get() = sharedPreference.getBoolean(getKey(R.string.key_sound), true)

    val customSoundEnabled: Boolean
        get() = sharedPreference.getBoolean(getKey(R.string.key_custom_sound), false)

    var soundUri: Uri
        get() = Uri.parse(sharedPreference.getString(getKey(R.string.key_sound_uri), defaultSound))
        set(value) {
            sharedPreference.edit().run {
                putString(getKey(R.string.key_sound_uri), value.toString())
                apply()
            }
        }

    val reminderFrequency: String
        get() = sharedPreference.getString(getKey(R.string.key_reminder_frequency), durationEveryday)
            ?: durationEveryday

    var reminderTime: LocalTime?
        get() = DateTimeConverter.toTime(
            sharedPreference.getString(getKey(R.string.key_reminder_time), "08:30") ?: "08:30")
        set(value) {
            sharedPreference.edit().run {
                putString(getKey(R.string.key_reminder_time), DateTimeConverter.fromTime(value))
                apply()
            }
        }

    val taskReminder: Boolean
        get() = sharedPreference.getBoolean(getKey(R.string.key_task_reminder), true)

    val taskReminderInterval: String
        get() = sharedPreference.getString(getKey(R.string.key_task_reminder_interval),
                                           taskReminderIntervalThreeHours) ?: taskReminderIntervalThreeHours

    val eventReminder: Boolean
        get() = sharedPreference.getBoolean(getKey(R.string.key_event_reminder), true)

    val eventReminderInterval: String
        get() = sharedPreference.getString(getKey(R.string.key_event_reminder_interval),
                                           eventReminderIntervalHalf) ?: eventReminderIntervalHalf


    /**
     *   Function to retrieve the Preference Key
     *   in the string resource
     *   @param id - Resource ID of the String resource
     *   @return the Preference Key in String format
     */
    private fun getKey(@StringRes id: Int): String? = context?.getString(id)

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
    }
}