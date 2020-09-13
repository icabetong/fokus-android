package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setTransitionName
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.ShareOptionsBottomSheet
import com.isaiahvonrundstedt.fokus.components.custom.TwoLineRadioButton
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.DataExporterService
import com.isaiahvonrundstedt.fokus.components.service.DataImporterService
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.picker.SchedulePickerSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.picker.SubjectPickerSheet
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_event.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.io.File
import java.util.*

class EventEditor : BaseEditor() {

    private var requestCode = 0
    private var event = Event()
    private var subject: Subject? = null
    private var schedules: List<Schedule> = emptyList()
    private var hasFieldChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_event)
        setPersistentActionBar(toolbar)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

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
            onValueChanged()
            window.decorView.rootView.clearFocus()
        }

        var currentScrollPosition = 0
        contentView.viewTreeObserver.addOnScrollChangedListener {
            if (contentView.scrollY > currentScrollPosition) actionButton.hide()
            else actionButton.show()
            currentScrollPosition = contentView.scrollY
        }
    }

    override fun onStart() {
        super.onStart()

        eventNameTextInput.addTextChangedListener { hasFieldChange = true }
        locationTextInput.addTextChangedListener { hasFieldChange = true }

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
            SubjectPickerSheet(supportFragmentManager).show {
                waitForResult { result ->
                    with(this@EventEditor) {
                        removeButton.isVisible = true
                        event.subject = result.subject.subjectID
                        subject = result.subject
                        schedules = result.schedules

                        with(subjectTextView) {
                            text = result.subject.code
                            setTextColorFromResource(R.color.color_primary_text)
                            ContextCompat.getDrawable(this.context, R.drawable.shape_color_holder)?.let {
                                this.setCompoundDrawableAtStart(result.subject.tintDrawable(it))
                            }
                        }

                        scheduleTextView.visibility = View.GONE
                        dateTimeRadioGroup.visibility = View.VISIBLE
                        if (event.schedule != null) {
                            with(customDateTimeRadio) {
                                isChecked = true
                                titleTextColor = ContextCompat.getColor(context,
                                    R.color.color_primary_text)
                                subtitle = event.formatSchedule(context)
                            }
                        }
                    }
                    hasFieldChange = true
                }
            }
        }

        removeButton.setOnClickListener {
            hasFieldChange = true
            subjectTextView.startAnimation(animation)

            it.visibility = View.INVISIBLE
            event.subject = null
            with(subjectTextView) {
                removeCompoundDrawableAtStart()
                setText(R.string.field_not_set)
                setTextColorFromResource(R.color.color_secondary_text)
            }

            dateTimeRadioGroup.visibility = View.GONE
            scheduleTextView.visibility = View.VISIBLE
            if (event.schedule != null) {
                scheduleTextView.text = event.formatSchedule(this)
                scheduleTextView.setTextColorFromResource(R.color.color_primary_text)
            }
        }

        dateTimeRadioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            for (v: View in radioGroup.children) {
                if (v is TwoLineRadioButton && !v.isChecked) {
                    v.titleTextColor = ContextCompat.getColor(v.context,
                        R.color.color_secondary_text)
                    v.subtitle = ""
                }
            }
        }

        inNextMeetingRadio.setOnClickListener {
            val currentDate = LocalDate.now()
            val items = mutableListOf<Schedule>()
            schedules.forEach {
                it.getDaysAsList().forEach { day ->
                    val newSchedule = Schedule(startTime = it.startTime,
                        endTime = it.endTime)
                    newSchedule.daysOfWeek = day
                    items.add(newSchedule)
                }
            }

            val dates = items.map { Schedule.getNearestDateTime(it.daysOfWeek, it.startTime) }
            if (dates.isEmpty())
                return@setOnClickListener

            var targetDate: DateTime = dates[0]
            dates.forEach {
                if (currentDate.isAfter(it.toLocalDate()) && targetDate.isBefore(it))
                    targetDate = it
            }

            hasFieldChange = true
            event.schedule = targetDate
            with(inNextMeetingRadio) {
                titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                subtitle = event.formatSchedule(context)
            }
        }

        pickDateTimeRadio.setOnClickListener {
            SchedulePickerSheet(schedules, supportFragmentManager).show {
                waitForResult {
                    event.schedule = Schedule.getNearestDateTime(it.daysOfWeek,
                        it.startTime)
                    with(this@EventEditor.pickDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = event.formatSchedule(this@EventEditor)
                    }

                    hasFieldChange = true
                    this.dismiss()
                }
            }
        }

        customDateTimeRadio.setOnClickListener {
            MaterialDialog(this).show {
                lifecycleOwner(this@EventEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = event.schedule?.toDateTime()?.toCalendar(
                        Locale.getDefault())) { _, datetime ->
                    event.schedule = LocalDateTime.fromCalendarFields(datetime).toDateTime()
                }
                positiveButton(R.string.button_done) {
                    hasFieldChange = true

                    with(this@EventEditor.customDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = event.formatSchedule(context)
                    }
                }
                negativeButton { this@EventEditor.customDateTimeRadio.isChecked = false }
            }
        }

        actionButton.setOnClickListener {
            // Conditions to check if the fields are null or blank
            // then if resulted true, show a feedback then direct
            // user focus to the field and stop code execution.
            if (eventNameTextInput.text.isNullOrBlank()) {
                createSnackbar(R.string.feedback_event_empty_name, rootLayout)
                eventNameTextInput.requestFocus()
                return@setOnClickListener
            }

            if (locationTextInput.text.isNullOrBlank()) {
                createSnackbar(R.string.feedback_event_empty_location, rootLayout)
                locationTextInput.requestFocus()
                return@setOnClickListener
            }

            if (event.schedule == null) {
                createSnackbar(R.string.feedback_event_empty_schedule, rootLayout)
                scheduleTextView.performClick()
                return@setOnClickListener
            }

            event.name = eventNameTextInput.text.toString()
            event.notes = notesTextInput.text.toString()
            event.location = locationTextInput.text.toString()
            event.isImportant = prioritySwitch.isChecked

            // Send the data back to the parent activity
            val data = Intent()
            data.putExtra(EXTRA_EVENT, event)
            setResult(RESULT_OK, data)
            if (requestCode == REQUEST_CODE_UPDATE)
                supportFinishAfterTransition()
            else finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)
    }

    private var receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BaseService.ACTION_SERVICE_BROADCAST) {
                when (intent.getStringExtra(BaseService.EXTRA_BROADCAST_STATUS)) {
                    DataExporterService.BROADCAST_EXPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_export_ongoing, rootLayout,
                            Snackbar.LENGTH_INDEFINITE)
                    }
                    DataExporterService.BROADCAST_EXPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_export_completed, rootLayout)

                        intent.getStringExtra(BaseService.EXTRA_BROADCAST_DATA)?.also {
                            val uri = CoreApplication.obtainUriForFile(this@EventEditor, File(it))

                            startActivity(ShareCompat.IntentBuilder.from(this@EventEditor)
                                .addStream(uri)
                                .setType(Streamable.MIME_TYPE_ZIP)
                                .setChooserTitle(R.string.dialog_send_to)
                                .intent)
                        }
                    }
                    DataExporterService.BROADCAST_EXPORT_FAILED -> {
                        createSnackbar(R.string.feedback_export_failed, rootLayout)
                    }
                    DataImporterService.BROADCAST_IMPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_import_ongoing, rootLayout)
                    }
                    DataImporterService.BROADCAST_IMPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_import_completed, rootLayout)

                        intent.getParcelableExtra<EventPackage>(BaseService.EXTRA_BROADCAST_DATA)?.also {
                            this@EventEditor.event = it.event
                            onValueChanged()
                        }
                    }
                    DataImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, rootLayout)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share_options -> {

                var fileName = event.name ?: Streamable.ARCHIVE_NAME_GENERIC
                when (requestCode) {
                    REQUEST_CODE_INSERT -> {
                        if (eventNameTextInput.text.isNullOrEmpty() || event.schedule == null ||
                                locationTextInput.text.isNullOrEmpty()) {
                            MaterialDialog(this).show {
                                title(R.string.feedback_unable_to_share_title)
                                message(R.string.feedback_unable_to_share_message)
                                positiveButton(R.string.button_dismiss) { dismiss() }
                            }
                            return false
                        }

                        fileName = eventNameTextInput.text.toString()
                    }
                    REQUEST_CODE_UPDATE -> {
                        fileName = event.name ?: Streamable.ARCHIVE_NAME_GENERIC

                        if (!eventNameTextInput.text.isNullOrEmpty())
                            event.name = eventNameTextInput.text.toString()
                        if (!locationTextInput.text.isNullOrEmpty())
                            event.location = locationTextInput.text.toString()
                    }
                }

                ShareOptionsBottomSheet(supportFragmentManager).show {
                    waitForResult { id ->
                        when (id) {
                            R.id.action_export -> {
                                val export = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    putExtra(Intent.EXTRA_TITLE, fileName)
                                    type = Streamable.MIME_TYPE_ZIP
                                }

                                this@EventEditor.startActivityForResult(export, REQUEST_CODE_EXPORT)
                            }
                            R.id.action_share -> {
                                val serviceIntent = Intent(this@EventEditor, DataExporterService::class.java).apply {
                                    action = DataExporterService.ACTION_EXPORT_EVENT
                                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, event)
                                }

                                this@EventEditor.startService(serviceIntent)
                            }
                        }
                    }
                }
            }
            R.id.action_import -> {
                val chooser = Intent.createChooser(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = Streamable.MIME_TYPE_ZIP
                }, getString(R.string.dialog_select_file_import))

                startActivityForResult(chooser, REQUEST_CODE_IMPORT)
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

    override fun onValueChanged() {
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

            scheduleTextView.visibility = View.GONE
            dateTimeRadioGroup.visibility = View.VISIBLE
            customDateTimeRadio.isChecked = true
            customDateTimeRadio.subtitle = event.formatSchedule(this)
            customDateTimeRadio.titleTextColor = ContextCompat.getColor(this,
                R.color.color_primary_text)
            removeButton.isVisible = true
        }

        scheduleTextView.setTextColorFromResource(R.color.color_primary_text)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            REQUEST_CODE_IMPORT ->
                startService(Intent(this, DataImporterService::class.java).apply {
                    this.data = data?.data
                    action = DataImporterService.ACTION_IMPORT_EVENT
                })
            REQUEST_CODE_EXPORT ->
                startService(Intent(this, DataExporterService::class.java).apply {
                    this.data = data?.data
                    action = DataExporterService.ACTION_EXPORT_EVENT
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, event)
                })
        }
    }

    companion object {
        const val REQUEST_CODE_INSERT = 24
        const val REQUEST_CODE_UPDATE = 56

        const val EXTRA_EVENT = "extra:event"
        const val EXTRA_SUBJECT = "extra:subject"

        const val TRANSITION_ID_NAME = "transition:event:name"

        private const val REQUEST_CODE_EXPORT = 58
        private const val REQUEST_CODE_IMPORT = 57
    }
}