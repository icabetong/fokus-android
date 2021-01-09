package com.isaiahvonrundstedt.fokus.features.event.editor

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
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
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import com.isaiahvonrundstedt.fokus.features.subject.picker.SubjectPickerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_editor_event.*
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

@AndroidEntryPoint
class EventEditor : BaseEditor() {
    private lateinit var binding: ActivityEditorEventBinding

    private var requestCode = 0

    private val viewModel: EventEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBarLayout.toolbar)
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        // Check if the parent activity has passed some
        // extras so that we'll show it to the user
        requestCode = if (intent.hasExtra(EXTRA_EVENT)) REQUEST_CODE_UPDATE
        else REQUEST_CODE_INSERT

        if (requestCode == REQUEST_CODE_UPDATE) {
            viewModel.event = intent.getParcelableExtra(EXTRA_EVENT)
            viewModel.subject = intent.getParcelableExtra(EXTRA_SUBJECT)

            binding.root.transitionName = TRANSITION_ELEMENT_ROOT + viewModel.event?.eventID

            window.sharedElementEnterTransition = buildContainerTransform(binding.root)
            window.sharedElementReturnTransition = buildContainerTransform(binding.root,
                transitionDuration = TRANSITION_SHORT_DURATION)
        } else {
            binding.root.transitionName = TRANSITION_ELEMENT_ROOT

            window.sharedElementEnterTransition = buildContainerTransform(binding.root,
                withMotion = true)
            window.sharedElementReturnTransition = buildContainerTransform(binding.root,
                TRANSITION_SHORT_DURATION, true)
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

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

        viewModel.eventObservable.observe(this) {
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

        viewModel.subjectObservable.observe(this) {
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

                if (viewModel.hasSchedule()) {
                    with(binding.customDateTimeRadio) {
                        isChecked = true
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getFormattedSchedule()
                    }
                }
            } else {
                with(binding.subjectTextView) {
                    removeCompoundDrawableAtStart()
                    setText(R.string.field_not_set)
                    setTextColorFromResource(R.color.color_secondary_text)
                }

                if (viewModel.hasSchedule()) {
                    with(binding.scheduleTextView) {
                        text = viewModel.getFormattedSchedule()
                        setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.eventNameTextInput.addTextChangedListener {
            viewModel.setEventName(it.toString())
        }

        binding.locationTextInput.addTextChangedListener {
            viewModel.setLocation(it.toString())
        }

        binding.notesTextInput.addTextChangedListener {
            viewModel.setNotes(it.toString())
        }

        binding.scheduleTextView.setOnClickListener { v ->
            MaterialDialog(this).show {
                lifecycleOwner(this@EventEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getSchedule()?.toCalendar()) { _, datetime ->
                    viewModel.setSchedule(datetime.toZonedDateTime())
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) {
                        v.text = viewModel.getFormattedSchedule()
                        v.setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.subjectTextView.setOnClickListener {
            startActivityForResult(Intent(this, SubjectPickerActivity::class.java),
                SubjectPickerActivity.REQUEST_CODE_PICK)
        }

        binding.removeButton.setOnClickListener {
            binding.subjectTextView.startAnimation(animation)
            viewModel.subject = null
        }

        binding.dateTimeRadioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            for (v: View in radioGroup.children) {
                if (v is TwoLineRadioButton && !v.isChecked) {
                    v.titleTextColor = ContextCompat.getColor(v.context,
                        R.color.color_secondary_text)
                    v.subtitle = null
                }
            }
        }

        binding.inNextMeetingRadio.setOnClickListener {
            viewModel.setNextMeetingForDueDate()

            with(binding.inNextMeetingRadio) {
                titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                subtitle = viewModel.getFormattedSchedule()
            }
        }

        binding.pickDateTimeRadio.setOnClickListener {
            SchedulePickerSheet(viewModel.schedules, supportFragmentManager).show {
                waitForResult { schedule ->
                    viewModel.setClassScheduleAsDueDate(schedule)
                    with(binding.pickDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getFormattedSchedule()
                    }

                    this.dismiss()
                }
            }
        }

        binding.customDateTimeRadio.setOnClickListener {
            MaterialDialog(this).show {
                lifecycleOwner(this@EventEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getSchedule()?.toCalendar()) { _, datetime ->
                    viewModel.event?.schedule = ZonedDateTime.ofInstant(datetime.toInstant(),
                        ZoneId.systemDefault())
                }
                positiveButton(R.string.button_done) {

                    with(binding.customDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getFormattedSchedule()
                    }
                }
                negativeButton { binding.customDateTimeRadio.isChecked = false }
            }
        }

        binding.actionButton.setOnClickListener {
            // Conditions to check if the fields are null or blank
            // then if resulted true, show a feedback then direct
            // user focus to the field and stop code execution.
            if (!viewModel.hasEventName()) {
                createSnackbar(R.string.feedback_event_empty_name, binding.root)
                binding.eventNameTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!viewModel.hasLocation()) {
                createSnackbar(R.string.feedback_event_empty_location, binding.root)
                binding.locationTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!viewModel.hasSchedule()) {
                createSnackbar(R.string.feedback_event_empty_schedule, binding.root)
                binding.scheduleTextView.performClick()
                return@setOnClickListener
            }

            viewModel.setIsImportant(prioritySwitch.isChecked)

            // Send the data back to the parent activity
            setResult(RESULT_OK, Intent().apply {
                putExtra(EXTRA_EVENT, viewModel.event)
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
                        viewModel.event = data?.event
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

                var fileName = viewModel.getEventName() ?: Streamable.ARCHIVE_NAME_GENERIC
                when (requestCode) {
                    REQUEST_CODE_INSERT -> {
                        if (!viewModel.hasEventName() || !viewModel.hasLocation() ||
                                !viewModel.hasSchedule()) {
                            MaterialDialog(this).show {
                                title(R.string.feedback_unable_to_share_title)
                                message(R.string.feedback_unable_to_share_message)
                                positiveButton(R.string.button_dismiss) { dismiss() }
                            }
                            return false
                        }

                        fileName = viewModel.getEventName() ?: Streamable.ARCHIVE_NAME_GENERIC
                    }
                    REQUEST_CODE_UPDATE -> {
                        fileName = viewModel.getEventName() ?: Streamable.ARCHIVE_NAME_GENERIC
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
                                        viewModel.event)
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
            putParcelable(EXTRA_EVENT, viewModel.event)
            putParcelable(EXTRA_SUBJECT, viewModel.subject)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        with(savedInstanceState) {
            viewModel.event = getParcelable(EXTRA_EVENT)
            viewModel.subject = getParcelable(EXTRA_SUBJECT)
        }
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
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.event)
                })
            SubjectPickerActivity.REQUEST_CODE_PICK -> {
                data?.getParcelableExtra<SubjectPackage>(SubjectPickerActivity.EXTRA_SELECTED_SUBJECT)
                    ?.also {
                        viewModel.subject = it.subject
                        viewModel.schedules = it.schedules
                    }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_INSERT = 24
        const val REQUEST_CODE_UPDATE = 56

        const val EXTRA_EVENT = "extra:event"
        const val EXTRA_SUBJECT = "extra:subject"

        private const val REQUEST_CODE_EXPORT = 58
        private const val REQUEST_CODE_IMPORT = 57
    }
}