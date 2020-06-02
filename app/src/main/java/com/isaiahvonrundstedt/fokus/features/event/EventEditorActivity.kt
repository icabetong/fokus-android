package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.extensions.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_event.*
import org.joda.time.LocalDateTime
import java.util.*

class EventEditorActivity: BaseEditor() {

    private var requestCode = 0
    private var event = Event()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_event)
        setPersistentActionBar(toolbar)

        // Check if the parent activity has passed some
        // extras so that we'll show it to the user
        requestCode = if (intent.hasExtra(extraEvent)) updateRequestCode
            else insertRequestCode
        if (requestCode == updateRequestCode)
            event = intent.getParcelableExtra(extraEvent)!!

        // The passed extras will be shown in their
        // corresponding fields
        if (requestCode == updateRequestCode) {
            nameEditText.setText(event.name)
            notesEditText.setText(event.notes)
            locationEditText.setText(event.location)
            scheduleTextView.text = event.formatSchedule(this)
            scheduleTextView.setTextColorFromResource(R.color.colorPrimaryText)
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
                    if (v is AppCompatTextView) {
                        v.text = event.formatSchedule(this@EventEditorActivity)
                        v.setTextColorFromResource(R.color.colorPrimaryText)
                    }
                }
            }
        }

        actionButton.setOnClickListener {

            // Conditions to check if the fields are null or blank
            // then if resulted true, show a feedback then direct
            // user focus to the field and stop code execution.
            if (nameEditText.text.isNullOrBlank()) {
                createSnackbar(rootLayout, R.string.feedback_event_empty_name)
                nameEditText.requestFocus()
                return@setOnClickListener
            }

            if (locationEditText.text.isNullOrBlank()) {
                createSnackbar(rootLayout, R.string.feedback_event_empty_location)
                locationEditText.requestFocus()
                return@setOnClickListener
            }

            if (event.schedule == null) {
                createSnackbar(rootLayout, R.string.feedback_event_empty_schedule)
                scheduleTextView.performClick()
                return@setOnClickListener
            }

            event.name = nameEditText.text.toString()
            event.notes = notesEditText.text.toString()
            event.location = locationEditText.text.toString()

            // Send the data back to the parent activity
            val data = Intent()
            data.putExtra(extraEvent, event)
            setResult(Activity.RESULT_OK, data)
            supportFinishAfterTransition()
        }
    }

    companion object {
        const val insertRequestCode = 24
        const val updateRequestCode = 56
        const val extraEvent = "extraEvent"
    }
}