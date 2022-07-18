package com.isaiahvonrundstedt.fokus.components.utils

import android.content.ContentResolver
import android.content.Context
import androidx.preference.PreferenceManager
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.enums.SortDirection
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.subject.SubjectViewModel
import com.isaiahvonrundstedt.fokus.features.task.TaskViewModel
import java.time.LocalTime
import java.time.ZonedDateTime

class PreferenceManager(private val context: Context) {

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

    var theme: Theme
        get() = Theme.parse(
            sharedPreference.getString(
                PREFERENCE_THEME,
                Theme.SYSTEM.toString()
            )
        )
        set(value) {
            sharedPreference.edit().run {
                putString(PREFERENCE_THEME, value.toString())
                apply()
            }
        }

    var previousBackupDate: ZonedDateTime?
        get() = DateTimeConverter.toZonedDateTime(
            sharedPreference.getString(PREFERENCE_BACKUP, null)
        )
        set(value) {
            sharedPreference.edit().run {
                putString(
                    PREFERENCE_BACKUP,
                    DateTimeConverter.fromZonedDateTime(value)
                )
                apply()
            }
        }

    var reminderTime: LocalTime?
        get() = DateTimeConverter.toLocalTime(
            sharedPreference.getString(PREFERENCE_REMINDER_TIME, "08:30")
        )
        set(value) {
            sharedPreference.edit().run {
                putString(PREFERENCE_REMINDER_TIME, DateTimeConverter.fromLocalTime(value))
                apply()
            }
        }

    var taskConstraint: TaskViewModel.Constraint
        get() = TaskViewModel.Constraint.parse(
            sharedPreference.getString(
                PREFERENCE_TASK_FILTER_OPTION,
                TaskViewModel.Constraint.ALL.toString()
            ) ?: TaskViewModel.Constraint.ALL.toString()
        )
        set(value) {
            sharedPreference.edit().run {
                putString(PREFERENCE_TASK_FILTER_OPTION, value.toString())
                apply()
            }
        }

    var tasksSort: TaskViewModel.Sort
        get() = TaskViewModel.Sort.parse(
            sharedPreference.getString(
                PREFERENCE_TASK_SORT_OPTION,
                TaskViewModel.Sort.NAME.toString()
            ) ?: TaskViewModel.Sort.NAME.toString()
        )
        set(value) {
            sharedPreference.edit().run {
                putString(PREFERENCE_TASK_SORT_OPTION, value.toString())
                apply()
            }
        }

    var tasksSortDirection: SortDirection
        get() = SortDirection.parse(
            sharedPreference.getString(
                PREFERENCE_TASK_SORT_DIRECTION,
                SortDirection.ASCENDING.toString()
            )
        )
        set(value) {
            sharedPreference.edit().run {
                putString(PREFERENCE_TASK_SORT_DIRECTION, value.toString())
                apply()
            }
        }

    var subjectConstraint: SubjectViewModel.Constraint
        get() = SubjectViewModel.Constraint.parse(
            sharedPreference.getString(
                PREFERENCE_SUBJECT_FILTER_OPTION,
                SubjectViewModel.Constraint.ALL.toString()
            ) ?: SubjectViewModel.Constraint.ALL.toString()
        )
        set(value) {
            sharedPreference.edit().run {
                putString(PREFERENCE_SUBJECT_FILTER_OPTION, value.toString())
                apply()
            }
        }

    var subjectSort: SubjectViewModel.Sort
        get() = SubjectViewModel.Sort.parse(
            sharedPreference.getString(
                PREFERENCE_SUBJECT_SORT_OPTION,
                SubjectViewModel.Sort.CODE.toString()
            ) ?: SubjectViewModel.Sort.CODE.toString()
        )
        set(value) {
            sharedPreference.edit().run {
                putString(PREFERENCE_SUBJECT_SORT_OPTION, value.toString())
                apply()
            }
        }

    var subjectSortDirection: SortDirection
        get() = SortDirection.parse(
            sharedPreference.getString(
                PREFERENCE_SUBJECT_SORT_DIRECTION,
                SortDirection.ASCENDING.toString()
            ) ?: SubjectViewModel.Sort.CODE.toString()
        )
        set(value) {
            sharedPreference.edit().run {
                putString(PREFERENCE_SUBJECT_SORT_DIRECTION, value.toString())
                apply()
            }
        }

    val confetti: Boolean
        get() = sharedPreference.getBoolean(PREFERENCE_CONFETTI, true)

    val sounds: Boolean
        get() = sharedPreference.getBoolean(PREFERENCE_SOUND, true)

    val taskReminder: Boolean
        get() = sharedPreference.getBoolean(PREFERENCE_TASK_NOTIFICATION, true)

    val eventReminder: Boolean
        get() = sharedPreference.getBoolean(PREFERENCE_EVENT_NOTIFICATION, true)

    val subjectReminder: Boolean
        get() = sharedPreference.getBoolean(PREFERENCE_COURSE_NOTIFICATION, true)

    val useExternalBrowser: Boolean
        get() = sharedPreference.getBoolean(PREFERENCE_USE_EXTERNAL_BROWSER, false)

    val allowWeekNumbers: Boolean
        get() = sharedPreference.getBoolean(PREFERENCE_ALLOW_WEEK_NUMBERS, false)

    val reminderFrequency: String
        get() = sharedPreference.getString(
            PREFERENCE_REMINDER_FREQUENCY,
            DURATION_EVERYDAY
        ) ?: DURATION_EVERYDAY

    val taskReminderInterval: String
        get() = sharedPreference.getString(
            PREFERENCE_TASK_NOTIFICATION_INTERVAL,
            TASK_REMINDER_INTERVAL_3_HOURS
        ) ?: TASK_REMINDER_INTERVAL_3_HOURS

    val eventReminderInterval: String
        get() = sharedPreference.getString(
            PREFERENCE_EVENT_NOTIFICATION_INTERVAL,
            EVENT_REMINDER_INTERVAL_30_MINUTES
        ) ?: EVENT_REMINDER_INTERVAL_30_MINUTES

    val subjectReminderInterval: String
        get() = sharedPreference.getString(
            PREFERENCE_COURSE_NOTIFICATION_INTERVAL,
            SUBJECT_REMINDER_INTERVAL_30_MINUTES
        ) ?: SUBJECT_REMINDER_INTERVAL_30_MINUTES


    /* User-Defined Settings */
    var noConfirmImport: Boolean
        get() = sharedPreference.getBoolean(
            PREFERENCE_NO_CONFIRM_IMPORT,
            false
        )
        set(value) {
            sharedPreference.edit().run {
                putBoolean(PREFERENCE_NO_CONFIRM_IMPORT, value)
                commit()
            }
        }

    private val sharedPreference by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    companion object {
        const val DEFAULT_SOUND =
            "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${BuildConfig.APPLICATION_ID}/${R.raw.fokus}"

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

        // Preferences that are visible in the SettingsActivity
        const val PREFERENCE_THEME = "KEY_THEME"
        const val PREFERENCE_CONFETTI = "KEY_CONFETTI"
        const val PREFERENCE_SOUND = "KEY_SOUND"
        const val PREFERENCE_REMINDER_FREQUENCY = "KEY_REMINDER_FREQUENCY"
        const val PREFERENCE_REMINDER_TIME = "KEY_REMINDER_TIME"
        const val PREFERENCE_TASK_NOTIFICATION = "KEY_TASK_NOTIFICATION"
        const val PREFERENCE_TASK_NOTIFICATION_INTERVAL = "KEY_TASK_NOTIFICATION_INTERVAL"
        const val PREFERENCE_EVENT_NOTIFICATION = "KEY_EVENT_NOTIFICATION"
        const val PREFERENCE_EVENT_NOTIFICATION_INTERVAL = "KEY_EVENT_NOTIFICATION_INTERVAL"
        const val PREFERENCE_COURSE_NOTIFICATION = "KEY_COURSE_NOTIFICATION"
        const val PREFERENCE_COURSE_NOTIFICATION_INTERVAL = "KEY_COURSE_NOTIFICATION_INTERVAL"
        const val PREFERENCE_SYSTEM_NOTIFICATION = "KEY_SYSTEM_NOTIFICATION"
        const val PREFERENCE_ALLOW_WEEK_NUMBERS = "KEY_ALLOW_WEEK_NUMBERS"
        const val PREFERENCE_BACKUP_RESTORE = "KEY_BACKUP_RESTORE"
        const val PREFERENCE_BACKUP = "KEY_BACKUP"
        const val PREFERENCE_RESTORE = "KEY_RESTORE"
        const val PREFERENCE_USE_EXTERNAL_BROWSER = "KEY_USE_EXTERNAL_BROWSER"
        const val PREFERENCE_BATTERY_OPTIMIZATION = "KEY_BATTERY_OPTIMIZATION"

        // Preferences that are visible in AboutActivity
        const val PREFERENCE_REPORT_ISSUE = "KEY_REPORT_ISSUE"
        const val PREFERENCE_TRANSLATE = "KEY_TRANSLATE"
        const val PREFERENCE_NOTICES = "KEY_NOTICES"
        const val PREFERENCE_VERSION = "KEY_VERSION"

        // Preferences that are visible in NoticesActivity
        const val PREFERENCE_LIBRARIES = "KEY_LIBRARIES"
        const val PREFERENCE_NOTIFICATION_SOUND = "KEY_NOTIFICATION_SOUND"
        const val PREFERENCE_LAUNCHER_ICON = "KEY_LAUNCHER_ICON"
        const val PREFERENCE_UI_ICONS = "KEY_UI_ICONS"

        // Preferences that are not visible in anywhere and are only
        // used for remembering user choices in the UI
        const val PREFERENCE_NO_CONFIRM_IMPORT = "KEY_NO_CONFIRM_IMPORT"
        const val PREFERENCE_TASK_FILTER_OPTION = "KEY_TASKS_FILTER_OPTION"
        const val PREFERENCE_TASK_SORT_OPTION = "KEY_TASK_SORT_OPTION"
        const val PREFERENCE_TASK_SORT_DIRECTION = "KEY_TASK_SORT_DIRECTION"
        const val PREFERENCE_SUBJECT_FILTER_OPTION = "KEY_SUBJECT_FILTER_OPTION"
        const val PREFERENCE_SUBJECT_SORT_OPTION = "KEY_SUBJECT_SORT_OPTION"
        const val PREFERENCE_SUBJECT_SORT_DIRECTION = "KEY_SUBJECT_SORT_DIRECTION"
    }
}