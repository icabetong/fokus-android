package com.isaiahvonrundstedt.fokus.features.event

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setTransitionName
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.selector.SubjectSelectorSheet
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_event.*
import org.joda.time.LocalDateTime
import java.util.*

class EventEditor : BaseEditor() {

    private var requestCode = 0
    private var event = Event()
    private var subject: Subject? = null
    private var hasFieldChange = false

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

            setTransitionName(eventNameTextInput, TRANSITION_ID_NAME + event.eventID)
        }

        prioritySwitch.changeTextColorWhenChecked()

        // The passed extras will be shown in their
        // corresponding fields
        if (requestCode == REQUEST_CODE_UPDATE) {
            with(event) {
                eventNameTextInput.setText(name)
                notesTextInput.setText(notes)
                locationTextInput.setText(location)
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
                    hasFieldChange = true
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
                    hasFieldChange = true
                }
            }
        }

        clearButton.setOnClickListener {
            hasFieldChange = true
            subjectTextView.startAnimation(animation)

            it.isVisible = false
            event.subject = null
            with(subjectTextView) {
                removeCompoundDrawableAtStart()
                setText(R.string.field_not_set)
                setTextColorFromResource(R.color.color_secondary_text)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                // Conditions to check if the fields are null or blank
                // then if resulted true, show a feedback then direct
                // user focus to the field and stop code execution.
                if (eventNameTextInput.text.isNullOrBlank()) {
                    createSnackbar(R.string.feedback_event_empty_name, rootLayout)
                    eventNameTextInput.requestFocus()
                    return false
                }

                if (locationTextInput.text.isNullOrBlank()) {
                    createSnackbar(R.string.feedback_event_empty_location, rootLayout)
                    locationTextInput.requestFocus()
                    return false
                }

                if (event.schedule == null) {
                    createSnackbar(R.string.feedback_event_empty_schedule, rootLayout)
                    scheduleTextView.performClick()
                    return false
                }

                event.name = eventNameTextInput.text.toString()
                event.notes = notesTextInput.text.toString()
                event.location = locationTextInput.text.toString()
                event.isImportant = prioritySwitch.isChecked

                // Send the data back to the parent activity
                val data = Intent()
                data.putExtra(EXTRA_EVENT, event)
                setResult(RESULT_OK, data)
                supportFinishAfterTransition()
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

    override fun onBackPressed() {
        if (hasFieldChange) {
            MaterialDialog(this).show {
                title(R.string.dialog_discard_changes)
                positiveButton(R.string.button_discard) { super.onBackPressed() }
                negativeButton(R.string.button_cancel)
            }
        } else super.onBackPressed()
    }

    companion object {
        const val REQUEST_CODE_INSERT = 24
        const val REQUEST_CODE_UPDATE = 56

        const val EXTRA_EVENT = "extra:event"
        const val EXTRA_SUBJECT = "extra:subject"

        const val TRANSITION_ID_NAME = "transition:event:name"
    }
}