package com.isaiahvonrundstedt.fokus.features.event

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setTransitionName
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.changeTextColorWhenChecked
import com.isaiahvonrundstedt.fokus.components.extensions.android.removeCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.utils.JsonDataStreamer
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.selector.SubjectSelectorSheet
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_event.*
import kotlinx.android.synthetic.main.layout_editor_event.actionButton
import kotlinx.android.synthetic.main.layout_editor_event.clearButton
import kotlinx.android.synthetic.main.layout_editor_event.nameEditText
import kotlinx.android.synthetic.main.layout_editor_event.notesEditText
import kotlinx.android.synthetic.main.layout_editor_event.prioritySwitch
import kotlinx.android.synthetic.main.layout_editor_event.rootLayout
import kotlinx.android.synthetic.main.layout_editor_event.subjectTextView
import org.joda.time.LocalDateTime
import java.util.*

class EventEditor : BaseEditor() {

    private var requestCode = 0
    private var event = Event()
    private var subject: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_event)
        setPersistentActionBar(toolbar)

        // Check if the parent activity has passed some
        // extras so that we'll show it to the user
        requestCode = if (intent.hasExtra(EXTRA_EVENT)) REQUEST_CODE_UPDATE
            else REQUEST_CODE_INSERT

        if (requestCode == REQUEST_CODE_UPDATE) {
            event = intent.getParcelableExtra(EXTRA_EVENT)!!
            subject = intent.getParcelableExtra(EXTRA_SUBJECT)

            setTransitionName(nameEditText, EventAdapter.TRANSITION_EVENT_NAME + event.eventID)
        }

        prioritySwitch.changeTextColorWhenChecked()

        // The passed extras will be shown in their
        // corresponding fields
        if (requestCode == REQUEST_CODE_UPDATE) {
            with(event) {
                nameEditText.setText(name)
                notesEditText.setText(notes)
                locationEditText.setText(location)
                scheduleTextView.text = formatSchedule(this@EventEditor)
                prioritySwitch.isChecked = isImportant
            }

            subject?.let {
                with(subjectTextView) {
                    text = it.code
                    setTextColorFromResource(R.color.color_primary_text)
                    setCompoundDrawableAtStart(ContextCompat.getDrawable(this@EventEditor,
                        R.drawable.shape_color_holder)?.let { drawable -> it.tintDrawable(drawable) })
                }
                clearButton.isVisible = true
            }

            scheduleTextView.setTextColorFromResource(R.color.color_primary_text)

            window.decorView.rootView.clearFocus()
        }
    }

    override fun onStart() {
        super.onStart()

        scheduleTextView.setOnClickListener { v ->
            MaterialDialog(this).show {
                lifecycleOwner(this@EventEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = event.schedule?.toDateTime()?.toCalendar(
                        Locale.getDefault())) { _, datetime ->
                    event.schedule = LocalDateTime.fromCalendarFields(datetime).toDateTime()
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) {
                        v.text = event.formatSchedule(this@EventEditor)
                        v.setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        subjectTextView.setOnClickListener {
            SubjectSelectorSheet(supportFragmentManager).show {
                result { result ->
                    this@EventEditor.clearButton.isVisible = true
                    event.subject = result.subjectID
                    subject = result

                    with(this@EventEditor.subjectTextView) {
                        text = result.code
                        setTextColorFromResource(R.color.color_primary_text)
                        ContextCompat.getDrawable(this.context, R.drawable.shape_color_holder)?.let {
                            this.setCompoundDrawableAtStart(it)
                        }
                    }
                    ContextCompat.getDrawable(this@EventEditor, R.drawable.shape_color_holder)?.let {
                        this@EventEditor.subjectTextView
                            .setCompoundDrawableAtStart(result.tintDrawable(it))
                    }
                }
            }
        }

        clearButton.setOnClickListener {
            subjectTextView.startAnimation(animation)

            it.isVisible = false
            event.subject = null
            with(subjectTextView) {
                removeCompoundDrawableAtStart()
                setText(R.string.field_not_set)
                setTextColorFromResource(R.color.color_secondary_text)
            }
        }

        actionButton.setOnClickListener {

            // Conditions to check if the fields are null or blank
            // then if resulted true, show a feedback then direct
            // user focus to the field and stop code execution.
            if (nameEditText.text.isNullOrBlank()) {
                createSnackbar(rootLayout, R.string.feedback_event_empty_name).show()
                nameEditText.requestFocus()
                return@setOnClickListener
            }

            if (locationEditText.text.isNullOrBlank()) {
                createSnackbar(rootLayout, R.string.feedback_event_empty_location).show()
                locationEditText.requestFocus()
                return@setOnClickListener
            }

            if (event.schedule == null) {
                createSnackbar(rootLayout, R.string.feedback_event_empty_schedule).show()
                scheduleTextView.performClick()
                return@setOnClickListener
            }

            event.name = nameEditText.text.toString()
            event.notes = notesEditText.text.toString()
            event.location = locationEditText.text.toString()
            event.isImportant = prioritySwitch.isChecked

            // Send the data back to the parent activity
            val data = Intent()
            data.putExtra(EXTRA_EVENT, event)
            setResult(RESULT_OK, data)
            supportFinishAfterTransition()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if (requestCode == REQUEST_CODE_UPDATE)
            super.onCreateOptionsMenu(menu)
        else false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                MaterialDialog(this).show {
                    title(text = String.format(getString(R.string.dialog_confirm_deletion_title),
                        event.name))
                    message(R.string.dialog_confirm_deletion_summary)
                    positiveButton(R.string.button_delete) {
                        // Send the data back to the parent activity
                        val data = Intent()
                        data.putExtra(EXTRA_EVENT, event)
                        setResult(RESULT_DELETE, data)
                        finish()
                    }
                    negativeButton(R.string.button_cancel)
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        with(outState) {
            putParcelable(EXTRA_EVENT, event)
            putParcelable(EXTRA_SUBJECT, subject)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        with(savedInstanceState) {
            getParcelable<Event>(EXTRA_EVENT)?.let {
                this@EventEditor.event = it
            }
            getParcelable<Subject>(EXTRA_SUBJECT)?.let {
                this@EventEditor.subject = it
            }
        }
    }

    companion object {
        const val REQUEST_CODE_INSERT = 24
        const val REQUEST_CODE_UPDATE = 56

        const val EXTRA_EVENT = "extra:event"
        const val EXTRA_SUBJECT = "extra:subject"
    }
}