package com.isaiahvonrundstedt.fokus.features.core.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.isaiahvonrundstedt.fokus.features.core.work.ReminderWorker
import com.isaiahvonrundstedt.fokus.components.PreferenceManager

class StartupActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Schedule a reminder based on the reminder frequency
        // preference.
        ReminderWorker.reschedule(this.applicationContext)

        val mainIntent = Intent(this, MainActivity::class.java)
        if (intent?.action == SHORTCUT_ACTION_TASK || intent?.action == SHORTCUT_ACTION_EVENT)
            mainIntent.putExtra(EXTRA_SHORTCUT_ACTION, intent?.action)

        startActivity(mainIntent)
        finish()
    }

    companion object {
        const val EXTRA_SHORTCUT_ACTION = "extra:action"
        const val SHORTCUT_ACTION_TASK = "shortcut:task"
        const val SHORTCUT_ACTION_EVENT = "shortcut:event"
    }
}