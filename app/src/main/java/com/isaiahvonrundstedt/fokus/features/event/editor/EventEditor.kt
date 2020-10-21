package com.isaiahvonrundstedt.fokus.features.event.editor

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.ShareOptionsBottomSheet
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.removeCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toCalendar
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toZonedDateTime
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.DataExporterService
import com.isaiahvonrundstedt.fokus.components.service.DataImporterService
import com.isaiahvonrundstedt.fokus.components.views.TwoLineRadioButton
import com.isaiahvonrundstedt.fokus.databinding.ActivityEditorEventBinding
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import com.isaiahvonrundstedt.fokus.features.schedule.picker.SchedulePickerSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.picker.SubjectPickerSheet
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

class EventEditor : BaseEditor() {

    private var requestCode = 0
    private var hasFieldChange = false

    private lateinit var binding: ActivityEditorEventBinding

    private val viewModel by lazy {
        ViewModelProvider(this).get(EventEditorViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBarLayout.toolbar)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

        // Check if the parent activity has passed some
        // extras so that we'll show it to the user
        requestCode = if (intent.hasExtra(EXTRA_EVENT)) REQUEST_CODE_UPDATE
            else REQUEST_CODE_INSERT

        if (requestCode == REQUEST_CODE_UPDATE) {
            viewModel.setEvent(intent.getParcelableExtra(EXTRA_EVENT))
            viewModel.setSubject(intent.getParcelableExtra(EXTRA_SUBJECT))

            binding.eventNameTextInput.transitionName = TRANSITION_ID_NAME +
                    viewModel.getEvent()?.eventID
        }

        var currentScrollPosition = 0
        binding.contentView.viewTreeObserver.addOnScrollChangedListener {
            if (binding.contentView.scrollY > currentScrollPosition)
                binding.actionButton.hide()
            else binding.actionButton.show()
            currentScrollPosition = binding.contentView.scrollY
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.event.observe(this) {
            if (requestCode == REQUEST_CODE_UPDATE && it != null) {
                with(it) {
                    binding.eventNameTextInput.setText(name)
                    binding.notesTextInput.setText(notes)
                    binding.locationTextInput.setText(location)
                    binding.scheduleTextView.text = formatSchedule(this@EventEditor)
                    binding.prioritySwitch.isChecked = isImportant
                }
            }
        }

        viewModel.subject.observe(this) {
            binding.removeButton.isVisible = it != null
            binding.scheduleTextView.isVisible = it == null
            binding.dateTimeRadioGroup.isVisible = it != null

            if (it != null) {
                with(binding.subjectTextView) {
                    text = it.code
                    setTextColorFromResource(R.color.color_primary_text)
                    ContextCompat.getDrawable(context, R.drawable.shape_color_holder)?.also { shape ->
                        this.setCompoundDrawableAtStart(it.tintDrawable(shape))
                    }
                }

                if (viewModel.getEvent()?.schedule != null) {
                    with(binding.customDateTimeRadio) {
                        isChecked = true
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getEvent()?.formatSchedule(context) ?: ""
                    }
                }
            } else {
                with(binding.subjectTextView) {
                    removeCompoundDrawableAtStart()
                    setText(R.string.field_not_set)
                    setTextColorFromResource(R.color.color_secondary_text)
                }

                if (viewModel.getEvent()?.schedule != null) {
                    with(binding.scheduleTextView) {
                        text = viewModel.getEvent()?.formatSchedule(context)
                        setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.eventNameTextInput.addTextChangedListener {
            viewModel.getEvent()?.name = it.toString()
            hasFieldChange = true
        }

        binding.locationTextInput.addTextChangedListener {
            viewModel.getEvent()?.location = it.toString()
            hasFieldChange = true
        }

        binding.notesTextInput.addTextChangedListener {
            viewModel.getEvent()?.notes = it.toString()
            hasFieldChange = true
        }

        binding.scheduleTextView.setOnClickListener { v ->
            MaterialDialog(this).show {
                lifecycleOwner(this@EventEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getEventSchedule()?.toCalendar()) { _, datetime ->
                    viewModel.getEvent()?.schedule = datetime.toZonedDateTime()
                }
                positiveButton(R.string.button_done) {
                    hasFieldChange = true
                    if (v is AppCompatTextView) {
                        v.text = viewModel.getEvent()?.formatSchedule(context)
                        v.setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.subjectTextView.setOnClickListener {
            SubjectPickerSheet(supportFragmentManager).show {
                waitForResult { result ->
                    viewModel.setSubject(result.subject)
                    viewModel.setSchedules(result.schedules)
                    hasFieldChange = true
                }
            }
        }

        binding.removeButton.setOnClickListener {
            hasFieldChange = true
            binding.subjectTextView.startAnimation(animation)
            viewModel.setSubject(null)
        }

        binding.dateTimeRadioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            for (v: View in radioGroup.children) {
                if (v is TwoLineRadioButton && !v.isChecked) {
                    v.titleTextColor = ContextCompat.getColor(v.context,
                        R.color.color_secondary_text)
                    v.subtitle = ""
                }
            }
        }

        binding.inNextMeetingRadio.setOnClickListener {
            viewModel.setNextMeetingForDueDate()
            hasFieldChange = true

            with(binding.inNextMeetingRadio) {
                titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                subtitle = viewModel.getEvent()?.formatSchedule(context) ?: ""
            }
        }

        binding.pickDateTimeRadio.setOnClickListener {
            SchedulePickerSheet(viewModel.getSchedules(), supportFragmentManager).show {
                waitForResult { schedule ->
                    viewModel.setClassScheduleAsDueDate(schedule)
                    with(binding.pickDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getEvent()?.formatSchedule(context) ?: ""
                    }

                    hasFieldChange = true
                    this.dismiss()
                }
            }
        }

        binding.customDateTimeRadio.setOnClickListener {
            MaterialDialog(this).show {
                lifecycleOwner(this@EventEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getEventSchedule()?.toCalendar()) { _, datetime ->
                    viewModel.getEvent()?.schedule = ZonedDateTime.ofInstant(datetime.toInstant(),
                        ZoneId.systemDefault())
                }
                positiveButton(R.string.button_done) {
                    hasFieldChange = true

                    with(binding.customDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getEvent()?.formatSchedule(context) ?: ""
                    }
                }
                negativeButton { binding.customDateTimeRadio.isChecked = false }
            }
        }

        binding.actionButton.setOnClickListener {
            // Conditions to check if the fields are null or blank
            // then if resulted true, show a feedback then direct
            // user focus to the field and stop code execution.
            if (!viewModel.hasEventName) {
                createSnackbar(R.string.feedback_event_empty_name, binding.root)
                binding.eventNameTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!viewModel.hasLocation) {
                createSnackbar(R.string.feedback_event_empty_location, binding.root)
                binding.locationTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!viewModel.hasSchedule) {
                createSnackbar(R.string.feedback_event_empty_schedule, binding.root)
                binding.scheduleTextView.performClick()
                return@setOnClickListener
            }

            viewModel.getEvent()?.isImportant = binding.prioritySwitch.isChecked

            // Send the data back to the parent activity
            setResult(RESULT_OK, Intent().apply {
                putExtra(EXTRA_EVENT, viewModel.getEvent())
            })

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
                        createSnackbar(R.string.feedback_export_ongoing, binding.root,
                            Snackbar.LENGTH_INDEFINITE)
                    }
                    DataExporterService.BROADCAST_EXPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_export_completed, binding.root)

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
                        createSnackbar(R.string.feedback_export_failed, binding.root)
                    }
                    DataImporterService.BROADCAST_IMPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_import_ongoing, binding.root)
                    }
                    DataImporterService.BROADCAST_IMPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_import_completed, binding.root)

                        val data: EventPackage? = intent.getParcelableExtra(BaseService.EXTRA_BROADCAST_DATA)
                        viewModel.setEvent(data?.event)
                    }
                    DataImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, binding.root)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share_options -> {

                var fileName = viewModel.getEvent()?.name ?: Streamable.ARCHIVE_NAME_GENERIC
                when (requestCode) {
                    REQUEST_CODE_INSERT -> {
                        if (!viewModel.hasEventName || !viewModel.hasLocation ||
                                !viewModel.hasSchedule) {
                            MaterialDialog(this).show {
                                title(R.string.feedback_unable_to_share_title)
                                message(R.string.feedback_unable_to_share_message)
                                positiveButton(R.string.button_dismiss) { dismiss() }
                            }
                            return false
                        }

                        fileName = viewModel.getEvent()?.name ?: Streamable.ARCHIVE_NAME_GENERIC
                    }
                    REQUEST_CODE_UPDATE -> {
                        fileName = viewModel.getEvent()?.name ?: Streamable.ARCHIVE_NAME_GENERIC
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
                                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE,
                                        viewModel.getEvent())
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
            putParcelable(EXTRA_EVENT, viewModel.getEvent())
            putParcelable(EXTRA_SUBJECT, viewModel.getSubject())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        with(savedInstanceState) {
            viewModel.setEvent(getParcelable(EXTRA_EVENT))
            viewModel.setSubject(getParcelable(EXTRA_SUBJECT))
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
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.getEvent())
                })
        }
    }

    companion object {
        const val REQUEST_CODE_INSERT = 24
        const val REQUEST_CODE_UPDATE = 56

        const val EXTRA_EVENT = "extra:event"
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_SCHEDULE = "extra:schedule"

        const val TRANSITION_ID_NAME = "transition:event:name"

        private const val REQUEST_CODE_EXPORT = 58
        private const val REQUEST_CODE_IMPORT = 57
    }
}