package com.isaiahvonrundstedt.fokus.features.core

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.isaiahvonrundstedt.fokus.features.core.work.TaskReminderNotifier
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.task.TaskFragment

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TaskReminderNotifier.Scheduler()
            .setTargetTime(PreferenceManager(this).reminderTime?.toDateTimeToday())
            .removePrevious(true)
            .schedule(this)

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}