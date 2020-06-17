package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
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
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.selector.SubjectSelectorActivity
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_event.*
import org.joda.time.LocalDateTime
import java.util.*

class EventEditor: BaseEditor() {

    private var requestCode = 0
    private var event = Event()
    private var subject: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_event)
        setPersistentActionBar(toolbar)

        // Check if the parent activity has passed some
        // extras so that we'll show it to the user
        requestCode = if (intent.hasExtra(extraEvent)) updateRequestCode
        else insertRequestCode

        if (requestCode == updateRequestCode) {
            event = intent.getParcelableExtra(extraEvent)!!
            subject = intent.getParcelableExtra(extraSubject)

            setTransitionName(nameEditText, EventAdapter.transitionEventName + event.eventID)
            setTransitionName(locationEditText, EventAdapter.transitionLocation + event.eventID)
        }

        prioritySwitch.changeTextColorWhenChecked()

        // The passed extras will be shown in their
        // corresponding fields
        if (requestCode == updateRequestCode) {
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
                    setTextColorFromResource(R.color.colorPrimaryText)
                    setCompoundDrawableAtStart(ContextCompat.getDrawable(this@EventEditor,
                        R.drawable.shape_color_holder)?.let { drawable -> it.tintDrawable(drawable) })
                }
                clearButton.isVisible = true
            }

            scheduleTextView.setTextColorFromResource(R.color.colorPrimaryText)

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
                        v.setTextColorFromResource(R.color.colorPrimaryText)
                    }
                }
            }
        }

        subjectTextView.setOnClickListener {
            startActivityForResult(Intent(this, SubjectSelectorActivity::class.java),
                SubjectSelectorActivity.requestCode)
            overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_nothing)
        }

        clearButton.setOnClickListener {
            it.isVisible = false
            event.subjectID = null
            with(subjectTextView) {
                removeCompoundDrawableAtStart()
                setText(R.string.field_not_set)
                setTextColorFromResource(R.color.colorSecondaryText)
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
            data.putExtra(extraEvent, event)
            setResult(RESULT_OK, data)
            supportFinishAfterTransition()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SubjectSelectorActivity.requestCode && resultCode == Activity.RESULT_OK) {
            data?.getParcelableExtra<Subject>(SubjectSelectorActivity.extraSubject)?.let { subject ->
                clearButton.isVisible = true
                event.subjectID = subject.id
                this.subject = subject

                with(subjectTextView) {
                    text = subject.code
                    setTextColorFromResource(R.color.colorPrimaryText)
                    ContextCompat.getDrawable(this.context, R.drawable.shape_color_holder)?.let {
                        setCompoundDrawableAtStart(subject.tintDrawable(it))
                    }
                }
                ContextCompat.getDrawable(this, R.drawable.shape_color_holder)?.let {
                    subjectTextView.setCompoundDrawableAtStart(subject.tintDrawable(it))
                }
                subjectTextView.setTextColorFromResource(R.color.colorPrimaryText)
                subjectTextView.text = subject.code
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if (requestCode == updateRequestCode)
            super.onCreateOptionsMenu(menu)
        else false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                MaterialDialog(this).show {
                    title(R.string.dialog_confirm_deletion_title)
                    message(R.string.dialog_confirm_deletion_summary)
                    positiveButton(R.string.button_delete) {
                        // Send the data back to the parent activity
                        val data = Intent()
                        data.putExtra(extraEvent, event)
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

    companion object {
        const val insertRequestCode = 24
        const val updateRequestCode = 56
        const val extraEvent = "extraEvent"
        const val extraSubject = "extraSubject"
    }
}