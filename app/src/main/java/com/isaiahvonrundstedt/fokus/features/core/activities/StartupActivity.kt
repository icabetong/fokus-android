package com.isaiahvonrundstedt.fokus.features.core.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.isaiahvonrundstedt.fokus.features.core.work.ReminderWorker
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ReminderWorker.Scheduler()
            .setTargetTime(PreferenceManager(this).reminderTime?.toDateTimeToday())
            .removePrevious(true)
            .schedule(this)

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}