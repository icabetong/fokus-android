package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.AppCompatTextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_event.*
import kotlinx.android.synthetic.main.layout_editor_event.actionButton
import org.joda.time.LocalDateTime
import java.util.*

class EventEditorActivity: BaseActivity() {

    private var requestCode = 0
    private var event = Event()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_event)
        setPersistentActionBar(toolbar)

        requestCode = if (intent.hasExtra(extraEvent)) updateRequestCode
            else insertRequestCode
        if (requestCode == updateRequestCode) {
            event = intent.getParcelableExtra(extraEvent)!!
        }

        if (requestCode == updateRequestCode) {
            nameEditText.setText(event.name)
            notesEditText.setText(event.notes)
            locationEditText.setText(event.location)
            scheduleTextView.text = event.formatSchedule(this)
        }
    }

    override fun onStart() {
        super.onStart()

        scheduleTextView.setOnClickListener { v ->
            MaterialDialog(this).show {
                lifecycleOwner(this@EventEditorActivity)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = event.schedule?.toDateTime()?.toCalendar(Locale.getDefault())) { _, datetime ->
                    event.schedule = LocalDateTime.fromCalendarFields(datetime).toDateTime()
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView)
                        v.text = event.formatSchedule(this@EventEditorActivity)
                }
            }
        }

        actionButton.setOnClickListener {
            if (nameEditText.text.isNullOrBlank()) {
                Snackbar.make(recyclerView, R.string.feedback_event_empty_name,
                    Snackbar.LENGTH_SHORT).show()
                nameEditText.requestFocus()
                return@setOnClickListener
            }

            if (locationEditText.text.isNullOrBlank()) {
                Snackbar.make(recyclerView, R.string.feedback_event_empty_location,
                    Snackbar.LENGTH_SHORT).show()
                locationEditText.requestFocus()
                return@setOnClickListener
            }

            if (event.schedule == null) {
                Snackbar.make(recyclerView, R.string.feedback_event_empty_schedule,
                    Snackbar.LENGTH_SHORT).show()
                scheduleTextView.performClick()
                return@setOnClickListener
            }

            event.name = nameEditText.text.toString()
            event.notes = notesEditText.text.toString()
            event.location = locationEditText.text.toString()

            val data = Intent()
            data.putExtra(extraEvent, event)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    companion object {
        const val insertRequestCode = 24
        const val updateRequestCode = 56
        const val extraEvent = "extraEvent"
    }
}