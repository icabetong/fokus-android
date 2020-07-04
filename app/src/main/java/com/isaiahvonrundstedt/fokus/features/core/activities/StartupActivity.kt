package com.isaiahvonrundstedt.fokus.features.core.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.isaiahvonrundstedt.fokus.features.core.work.ReminderWorker
import com.isaiahvonrundstedt.fokus.components.PreferenceManager

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Schedule a reminder based on the reminder frequency
        // preference.
        ReminderWorker.reschedule(this.applicationContext)

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}