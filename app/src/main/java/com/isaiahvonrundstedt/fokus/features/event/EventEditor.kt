package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setTransitionName
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.customListAdapter
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.extensions.setCompoundDrawableStart
import com.isaiahvonrundstedt.fokus.features.core.extensions.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.components.adapters.SubjectListAdapter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectEditor
import com.isaiahvonrundstedt.fokus.features.subject.SubjectViewModel
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_event.*
import org.joda.time.LocalDateTime
import java.util.*

class EventEditor: BaseEditor(), SubjectListAdapter.ItemSelected {

    private var requestCode = 0
    private var event = Event()
    private var subject: Subject? = null
    private var subjectDialog: MaterialDialog? = null

    private val adapter = SubjectListAdapter(this)
    private val subjectViewModel: SubjectViewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Check if the parent activity has passed some
        // extras so that we'll show it to the user
        requestCode = if (intent.hasExtra(extraEvent)) updateRequestCode
        else insertRequestCode

        if (requestCode == insertRequestCode)
            findViewById<View>(android.R.id.content).transitionName = transition

        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_event)
        setPersistentActionBar(toolbar)

        if (requestCode == updateRequestCode) {
            event = intent.getParcelableExtra(extraEvent)!!
            subject = intent.getParcelableExtra(extraSubject)!!

            setTransitionName(nameEditText, EventAdapter.transitionEventName + event.eventID)
            setTransitionName(locationEditText, EventAdapter.transitionLocation + event.eventID)
        }

        // The passed extras will be shown in their
        // corresponding fields
        if (requestCode == updateRequestCode) {
            nameEditText.setText(event.name)
            notesEditText.setText(event.notes)
            locationEditText.setText(event.location)
            scheduleTextView.text = event.formatSchedule(this)
            subjectTextView.text = subject!!.code

            scheduleTextView.setTextColorFromResource(R.color.colorPrimaryText)
            subjectTextView.setTextColorFromResource(R.color.colorPrimaryText)

            window.decorView.rootView.clearFocus()
        }

        subjectViewModel.fetch()?.observe(this, Observer { items ->
            adapter.submitList(items)
        })
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
            subjectDialog = MaterialDialog(it.context).show {
                lifecycleOwner(this@EventEditor)
                title(R.string.dialog_select_subject_title)
                customListAdapter(adapter)
                positiveButton(R.string.button_new_subject) {
                    startActivity(Intent(this@EventEditor, SubjectEditor::class.java))
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

    override fun onItemSelected(subject: Subject) {
        event.subjectID = subject.id
        this.subject = subject

        ContextCompat.getDrawable(this, R.drawable.shape_color_holder)?.let {
            subjectTextView.setCompoundDrawableStart(subject.tintDrawable(it))
        }
        subjectTextView.setTextColorFromResource(R.color.colorPrimaryText)
        subjectTextView.text = subject.code
        subjectDialog?.dismiss()
    }

    companion object {
        const val insertRequestCode = 24
        const val updateRequestCode = 56
        const val extraEvent = "extraEvent"
        const val extraSubject = "extraSubject"
    }
}