package com.isaiahvonrundstedt.fokus.components

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

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

    var theme: Theme
        get() = Theme.parse(sharedPreference.getString(R.string.key_theme,
            Theme.SYSTEM.toString()))
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
        get() = Uri.parse(sharedPreference.getString(R.string.key_custom_sound_uri,
            DEFAULT_SOUND
        ))
        set(value) {
            sharedPreference.edit().run {
                putString(R.string.key_custom_sound_uri, value.toString())
                apply()
            }
        }

    var backupDate: DateTime?
        get() {
            return DateTimeConverter.toDateTime(sharedPreference
                .getString(R.string.key_backup, null))
        }
        set(value) {
            sharedPreference.edit().run {
                putString(R.string.key_backup,
                    DateTimeConverter.fromDateTime(value))
                apply()
            }
        }


    val reminderFrequency: String
        get() = sharedPreference.getString(R.string.key_reminder_frequency,
            DURATION_EVERYDAY) ?: DURATION_EVERYDAY

    var reminderTime: LocalTime?
        get() = DateTimeConverter
            .toTime(sharedPreference.getString(R.string.key_reminder_time, "08:30")
                ?: "08:30")
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
            TASK_REMINDER_INTERVAL_3_HOURS
        ) ?: TASK_REMINDER_INTERVAL_3_HOURS

    val eventReminder: Boolean
        get() = sharedPreference.getBoolean(R.string.key_event_reminder, true)

    val eventReminderInterval: String
        get() = sharedPreference.getString(R.string.key_event_reminder_interval,
            EVENT_REMINDER_INTERVAL_30_MINUTES
        ) ?: EVENT_REMINDER_INTERVAL_30_MINUTES

    val subjectReminder: Boolean
        get() = sharedPreference.getBoolean(R.string.key_subject_reminder, true)

    val subjectReminderInterval: String
        get() = sharedPreference.getString(R.string.key_subject_reminder_interval,
            SUBJECT_REMINDER_INTERVAL_30_MINUTES
        )
            ?: SUBJECT_REMINDER_INTERVAL_5_MINUTES

    val noImport: Boolean
        get() = sharedPreference.getBoolean(R.string.key_no_import, false)

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
        const val DEFAULT_SOUND = "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${BuildConfig.APPLICATION_ID}/${R.raw.fokus}"
        val DEFAULT_SOUND_URI: Uri
            get() = Uri.parse(DEFAULT_SOUND)

        const val DURATION_EVERYDAY = "EVERYDAY"
        const val DURATION_WEEKENDS = "WEEKENDS"

        const val TASK_REMINDER_INTERVAL_1_HOUR = "1"
        const val TASK_REMINDER_INTERVAL_3_HOURS = "3"
        const val TASK_REMINDER_INTERVAL_24_HOURS = "24"

        const val EVENT_REMINDER_INTERVAL_15_MINUTES = "15"
        const val EVENT_REMINDER_INTERVAL_30_MINUTES = "30"
        const val EVENT_REMINDER_INTERVAL_60_MINUTES = "60"

        const val SUBJECT_REMINDER_INTERVAL_5_MINUTES = "5"
        const val SUBJECT_REMINDER_INTERVAL_15_MINUTES = "15"
        const val SUBJECT_REMINDER_INTERVAL_30_MINUTES = "30"

    }
}