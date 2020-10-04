package com.isaiahvonrundstedt.fokus.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.isaiahvonrundstedt.fokus.database.converter.ColorConverter
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.database.dao.*
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.log.Log
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task

@Database(entities = [Subject::class, Task::class, Attachment::class, Log::class,
    Event::class, Schedule::class], version = AppDatabase.DATABASE_VERSION, exportSchema = false)
@TypeConverters(DateTimeConverter::class, ColorConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjects(): SubjectDAO
    abstract fun schedules(): ScheduleDAO
    abstract fun tasks(): TaskDAO
    abstract fun attachments(): AttachmentDAO
    abstract fun events(): EventDAO
    abstract fun logs(): LogDAO

    companion object {
        const val DATABASE_VERSION = 4
        private const val DATABASE_NAME = "fokus"

        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, DATABASE_NAME)
                        .addMigrations(*migrations)
                        .build()
                }
            }
            return instance!!
        }

        private var migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    // attachments
                    execSQL("CREATE TABLE IF NOT EXISTS `attachments_new` (`attachmentID` TEXT NOT NULL, `uri` TEXT, `task` TEXT NOT NULL, `dateAttached` TEXT, PRIMARY KEY(`attachmentID`), FOREIGN KEY(`task`) REFERENCES `tasks`(`taskID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("INSERT INTO attachments_new SELECT * FROM attachments")
                    execSQL("DROP TABLE attachments")
                    execSQL("ALTER TABLE attachments_new RENAME TO attachments")

                    // tasks
                    execSQL("CREATE TABLE IF NOT EXISTS `tasks_new` (`taskID` TEXT NOT NULL, `name` TEXT, `notes` TEXT, `subject` TEXT, `isImportant` INTEGER NOT NULL, `dateAdded` TEXT, `dueDate` TEXT, `isFinished` INTEGER NOT NULL, PRIMARY KEY(`taskID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                    execSQL("INSERT INTO tasks_new SELECT * FROM tasks")
                    execSQL("DROP TABLE tasks")
                    execSQL("ALTER TABLE tasks_new RENAME TO tasks")
                    execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_taskID` ON `tasks` (`taskID`)")

                    // events
                    execSQL("CREATE TABLE IF NOT EXISTS `events_new` (`eventID` TEXT NOT NULL, `name` TEXT, `notes` TEXT, `location` TEXT, `subject` TEXT, `isImportant` INTEGER NOT NULL, `schedule` TEXT, `dateAdded` TEXT NOT NULL, PRIMARY KEY(`eventID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                    execSQL("INSERT INTO events_new SELECT * FROM events")
                    execSQL("DROP TABLE events")
                    execSQL("ALTER TABLE events_new RENAME TO events")

                    // subjects
                    execSQL("CREATE TABLE IF NOT EXISTS `subjects_new` (`subjectID` TEXT NOT NULL, `code` TEXT, `description` TEXT, `tag` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`subjectID`))")
                    execSQL("INSERT INTO subjects_new (`subjectID`, `code`, `description`,`tag`) SELECT `id`, `code`, `description`, `tag` FROM `subjects`")
                    execSQL("ALTER TABLE subjects RENAME TO subjects_old")
                    execSQL("ALTER TABLE subjects_new RENAME TO subjects")
                    execSQL("CREATE INDEX IF NOT EXISTS `index_subjects_subjectID` ON `subjects` (`subjectID`)")

                    // schedules
                    execSQL("CREATE TABLE IF NOT EXISTS `schedules` (`scheduleID` TEXT NOT NULL, `daysOfWeek` INTEGER NOT NULL, `startTime` TEXT, `endTime` TEXT, `subject` TEXT, PRIMARY KEY(`scheduleID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("INSERT INTO schedules (`scheduleID`, `daysOfWeek`, `startTime`, `endTime`, `subject`) SELECT `id` AS scheduleID,`daysOfWeek`, `startTime`, `endTime`, `id` AS subject FROM subjects_old")

                    // logs
                    execSQL("CREATE TABLE IF NOT EXISTS `logs` (`logID` TEXT NOT NULL, `title` TEXT, `content` TEXT, `data` TEXT, `type` INTEGER NOT NULL, `isImportant` INTEGER NOT NULL, `dateTimeTriggered` TEXT, PRIMARY KEY(`logID`))")
                    execSQL("INSERT INTO logs  (`logID`, `title`, `content`, `data`, `type`, `isImportant`, `dateTimeTriggered`) SELECT `id`, `title`, `content`, `data`, `type`, `isPersistent`, `dateTimeTriggered` FROM histories")
                    execSQL("DROP TABLE histories")

                    execSQL("DROP TABLE subjects_old")
                }
            }
        }

        private var migration_1_3 = object: Migration(1, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    // attachments
                    execSQL("CREATE TABLE IF NOT EXISTS `attachments_new` (`attachmentID` TEXT NOT NULL, `target` TEXT, `task` TEXT NOT NULL, `dateAttached` TEXT, PRIMARY KEY(`attachmentID`), FOREIGN KEY(`task`) REFERENCES `tasks`(`taskID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("INSERT INTO attachments_new SELECT * FROM attachments")
                    execSQL("DROP TABLE attachments")
                    execSQL("ALTER TABLE attachments_new RENAME TO attachments")

                    // tasks
                    execSQL("CREATE TABLE IF NOT EXISTS `tasks_new` (`taskID` TEXT NOT NULL, `name` TEXT, `notes` TEXT, `subject` TEXT, `isImportant` INTEGER NOT NULL, `dateAdded` TEXT, `dueDate` TEXT, `isFinished` INTEGER NOT NULL, PRIMARY KEY(`taskID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                    execSQL("INSERT INTO tasks_new SELECT * FROM tasks")
                    execSQL("DROP TABLE tasks")
                    execSQL("ALTER TABLE tasks_new RENAME TO tasks")
                    execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_taskID` ON `tasks` (`taskID`)")

                    // events
                    execSQL("CREATE TABLE IF NOT EXISTS `events_new` (`eventID` TEXT NOT NULL, `name` TEXT, `notes` TEXT, `location` TEXT, `subject` TEXT, `isImportant` INTEGER NOT NULL, `schedule` TEXT, `dateAdded` TEXT NOT NULL, PRIMARY KEY(`eventID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                    execSQL("INSERT INTO events_new SELECT * FROM events")
                    execSQL("DROP TABLE events")
                    execSQL("ALTER TABLE events_new RENAME TO events")

                    // subjects
                    execSQL("CREATE TABLE IF NOT EXISTS `subjects_new` (`subjectID` TEXT NOT NULL, `code` TEXT, `description` TEXT, `tag` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`subjectID`))")
                    execSQL("INSERT INTO subjects_new (`subjectID`, `code`, `description`,`tag`) SELECT `id`, `code`, `description`, `tag` FROM `subjects`")
                    execSQL("ALTER TABLE subjects RENAME TO subjects_old")
                    execSQL("ALTER TABLE subjects_new RENAME TO subjects")
                    execSQL("CREATE INDEX IF NOT EXISTS `index_subjects_subjectID` ON `subjects` (`subjectID`)")

                    // schedules
                    execSQL("CREATE TABLE IF NOT EXISTS `schedules` (`scheduleID` TEXT NOT NULL, `daysOfWeek` INTEGER NOT NULL, `startTime` TEXT, `endTime` TEXT, `subject` TEXT, PRIMARY KEY(`scheduleID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("INSERT INTO schedules (`scheduleID`, `daysOfWeek`, `startTime`, `endTime`, `subject`) SELECT `id` AS scheduleID,`daysOfWeek`, `startTime`, `endTime`, `id` AS subject FROM subjects_old")

                    // logs
                    execSQL("CREATE TABLE IF NOT EXISTS `logs` (`logID` TEXT NOT NULL, `title` TEXT, `content` TEXT, `data` TEXT, `type` INTEGER NOT NULL, `isImportant` INTEGER NOT NULL, `dateTimeTriggered` TEXT, PRIMARY KEY(`logID`))")
                    execSQL("INSERT INTO logs  (`logID`, `title`, `content`, `data`, `type`, `isImportant`, `dateTimeTriggered`) SELECT `id`, `title`, `content`, `data`, `type`, `isPersistent`, `dateTimeTriggered` FROM histories")
                    execSQL("DROP TABLE histories")

                    execSQL("DROP TABLE subjects_old")
                }
            }
        }

        private var migration_2_3 = object: Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    // attachments
                    execSQL("CREATE TABLE IF NOT EXISTS `attachments_new` (`attachmentID` TEXT NOT NULL, `target` TEXT, `task` TEXT NOT NULL, `dateAttached` TEXT, PRIMARY KEY(`attachmentID`), FOREIGN KEY(`task`) REFERENCES `tasks`(`taskID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("INSERT INTO attachments_new SELECT * FROM attachments")
                    execSQL("DROP TABLE attachments")
                    execSQL("ALTER TABLE attachments_new RENAME TO attachments")
                }
            }
        }

        private var migration_1_4 = object: Migration(1, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    // attachments
                    execSQL("CREATE TABLE IF NOT EXISTS `attachments_new` (`attachmentID` TEXT NOT NULL, `name` TEXT, `target` TEXT, `task` TEXT NOT NULL, `type` INT NOT NULL DEFAULT 1,`dateAttached` TEXT, PRIMARY KEY(`attachmentID`), FOREIGN KEY(`task`) REFERENCES `tasks`(`taskID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("INSERT INTO attachments_new (`attachmentID`, `target`, `task`, `dateAttached`) SELECT * FROM attachments")
                    execSQL("DROP TABLE attachments")
                    execSQL("ALTER TABLE attachments_new RENAME TO attachments")

                    // tasks
                    execSQL("CREATE TABLE IF NOT EXISTS `tasks_new` (`taskID` TEXT NOT NULL, `name` TEXT, `notes` TEXT, `subject` TEXT, `isImportant` INTEGER NOT NULL, `dateAdded` TEXT, `dueDate` TEXT, `isFinished` INTEGER NOT NULL, PRIMARY KEY(`taskID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                    execSQL("INSERT INTO tasks_new SELECT * FROM tasks")
                    execSQL("DROP TABLE tasks")
                    execSQL("ALTER TABLE tasks_new RENAME TO tasks")
                    execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_taskID` ON `tasks` (`taskID`)")

                    // events
                    execSQL("CREATE TABLE IF NOT EXISTS `events_new` (`eventID` TEXT NOT NULL, `name` TEXT, `notes` TEXT, `location` TEXT, `subject` TEXT, `isImportant` INTEGER NOT NULL, `schedule` TEXT, `dateAdded` TEXT NOT NULL, PRIMARY KEY(`eventID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                    execSQL("INSERT INTO events_new SELECT * FROM events")
                    execSQL("DROP TABLE events")
                    execSQL("ALTER TABLE events_new RENAME TO events")

                    // subjects
                    execSQL("CREATE TABLE IF NOT EXISTS `subjects_new` (`subjectID` TEXT NOT NULL, `code` TEXT, `description` TEXT, `tag` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`subjectID`))")
                    execSQL("INSERT INTO subjects_new (`subjectID`, `code`, `description`,`tag`) SELECT `id`, `code`, `description`, `tag` FROM `subjects`")
                    execSQL("ALTER TABLE subjects RENAME TO subjects_old")
                    execSQL("ALTER TABLE subjects_new RENAME TO subjects")
                    execSQL("CREATE INDEX IF NOT EXISTS `index_subjects_subjectID` ON `subjects` (`subjectID`)")

                    // schedules
                    execSQL("CREATE TABLE IF NOT EXISTS `schedules` (`scheduleID` TEXT NOT NULL, `daysOfWeek` INTEGER NOT NULL, `startTime` TEXT, `endTime` TEXT, `subject` TEXT, PRIMARY KEY(`scheduleID`), FOREIGN KEY(`subject`) REFERENCES `subjects`(`subjectID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("INSERT INTO schedules (`scheduleID`, `daysOfWeek`, `startTime`, `endTime`, `subject`) SELECT `id` AS scheduleID,`daysOfWeek`, `startTime`, `endTime`, `id` AS subject FROM subjects_old")

                    // logs
                    execSQL("CREATE TABLE IF NOT EXISTS `logs` (`logID` TEXT NOT NULL, `title` TEXT, `content` TEXT, `data` TEXT, `type` INTEGER NOT NULL, `isImportant` INTEGER NOT NULL, `dateTimeTriggered` TEXT, PRIMARY KEY(`logID`))")
                    execSQL("INSERT INTO logs  (`logID`, `title`, `content`, `data`, `type`, `isImportant`, `dateTimeTriggered`) SELECT `id`, `title`, `content`, `data`, `type`, `isPersistent`, `dateTimeTriggered` FROM histories")
                    execSQL("DROP TABLE histories")

                    execSQL("DROP TABLE subjects_old")
                }
            }
        }

        private var migration_2_4 = object: Migration(2, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    // attachments
                    execSQL("CREATE TABLE IF NOT EXISTS `attachments_new` (`attachmentID` TEXT NOT NULL, `name` TEXT, `target` TEXT, `task` TEXT NOT NULL, `type` INT NOT NULL DEFAULT 1,`dateAttached` TEXT, PRIMARY KEY(`attachmentID`), FOREIGN KEY(`task`) REFERENCES `tasks`(`taskID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("INSERT INTO attachments_new (`attachmentID`, `target`, `task`, `dateAttached`) SELECT * FROM attachments")
                    execSQL("DROP TABLE attachments")
                    execSQL("ALTER TABLE attachments_new RENAME TO attachments")
                }
            }
        }

        private var migration_3_4 = object: Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                with(database) {
                    // attachments
                    execSQL("CREATE TABLE IF NOT EXISTS `attachments_new` (`attachmentID` TEXT NOT NULL, `name` TEXT, `target` TEXT, `task` TEXT NOT NULL, `type` INT NOT NULL DEFAULT 2,`dateAttached` TEXT, PRIMARY KEY(`attachmentID`), FOREIGN KEY(`task`) REFERENCES `tasks`(`taskID`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    execSQL("INSERT INTO attachments_new (`attachmentID`, `target`, `task`, `dateAttached`) SELECT * FROM attachments")
                    execSQL("DROP TABLE attachments")
                    execSQL("ALTER TABLE attachments_new RENAME TO attachments")
                }
            }
        }

        private val migrations = arrayOf(migration_1_2, migration_1_3, migration_2_3, migration_1_4,
            migration_2_4, migration_3_4)
    }
}