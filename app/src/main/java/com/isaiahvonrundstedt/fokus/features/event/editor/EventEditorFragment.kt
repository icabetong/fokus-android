package com.isaiahvonrundstedt.fokus.features.event.editor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.isaiahvonrundstedt.fokus.Fokus
import com.isaiahvonrundstedt.fokus.R
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
import com.isaiahvonrundstedt.fokus.databinding.FragmentEditorEventBinding
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventPackage
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.picker.SchedulePickerSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditorFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import com.isaiahvonrundstedt.fokus.features.subject.picker.SubjectPickerFragment
import dagger.hilt.android.AndroidEntryPoint
import me.saket.cascade.overrideOverflowMenu
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

@AndroidEntryPoint
class EventEditorFragment : BaseEditorFragment(), FragmentResultListener {
    private var _binding: FragmentEditorEventBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_INSERT

    private val viewModel: EventEditorViewModel by viewModels()
    private val binding get() = _binding!!

    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private lateinit var importLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()

        exportLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                context?.startService(Intent(context, DataExporterService::class.java).apply {
                    this.data = it.data?.data
                    action = DataExporterService.ACTION_EXPORT_EVENT
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.getEvent())
                })
            }

        importLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                context?.startService(Intent(context, DataImporterService::class.java).apply {
                    this.data = it.data?.data
                    action = DataImporterService.ACTION_IMPORT_EVENT
                })
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.transitionName = TRANSITION_ELEMENT_ROOT
        setInsets(binding.root, binding.appBarLayout.toolbar, arrayOf(binding.contentView),
            binding.actionButton)
        controller = Navigation.findNavController(view)

        with(binding.appBarLayout.toolbar) {
            inflateMenu(R.menu.menu_editor)
            setNavigationOnClickListener {
                if (controller?.graph?.id == R.id.navigation_container_event)
                    requireActivity().finish()
                else controller?.navigateUp()
            }

            overrideOverflowMenu(::customPopupProvider)
            setOnMenuItemClickListener(::onMenuItemClicked)
        }

        arguments?.getBundle(EXTRA_EVENT)?.also {
            requestKey = REQUEST_KEY_UPDATE

            Event.fromBundle(it)?.also { event ->
                viewModel.setEvent(event)
                binding.root.transitionName = TRANSITION_ELEMENT_ROOT + event.eventID
                binding.priorityCard.isVisible = event.isImportant
            }
        }
        arguments?.getBundle(EXTRA_SUBJECT)?.also {
            viewModel.setSubject(Subject.fromBundle(it))
        }

        registerForFragmentResult(
            arrayOf(
                SubjectPickerFragment.REQUEST_KEY_PICK,
                SchedulePickerSheet.REQUEST_KEY
            ), this
        )
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

        if (requestKey == REQUEST_KEY_UPDATE) {
            binding.eventNameTextInput.setText(viewModel.getName())
            binding.locationTextInput.setText(viewModel.getLocation())
            binding.notesTextInput.setText(viewModel.getNotes())
        }

        viewModel.event.observe(viewLifecycleOwner) {
            if (requestKey == REQUEST_KEY_UPDATE && it != null) {
                with(it) {
                    binding.scheduleTextView.text = formatSchedule(requireContext())
                    binding.prioritySwitch.isChecked = isImportant
                }
            }
        }

        viewModel.subject.observe(viewLifecycleOwner) {
            binding.removeButton.isVisible = it != null
            binding.scheduleTextView.isVisible = it == null
            binding.dateTimeRadioGroup.isVisible = it != null

            if (it != null) {
                with(binding.subjectTextView) {
                    text = it.code
                    setTextColorFromResource(R.color.color_primary_text)
                    ContextCompat.getDrawable(context, R.drawable.shape_color_holder)
                        ?.also { shape ->
                            this.setCompoundDrawableAtStart(it.tintDrawable(shape))
                        }
                }

                if (viewModel.schedules.isNotEmpty()) {
                    with(binding.customDateTimeRadio) {
                        isChecked = true
                        titleTextColor = ContextCompat.getColor(
                            context,
                            R.color.color_primary_text
                        )
                        subtitle = viewModel.getEvent()?.formatSchedule(context)
                    }
                }
            } else {
                with(binding.subjectTextView) {
                    removeCompoundDrawableAtStart()
                    setText(R.string.field_subject)
                    setTextColorFromResource(R.color.color_secondary_text)
                }

                if (viewModel.schedules.isNotEmpty()) {
                    with(binding.scheduleTextView) {
                        text = viewModel.getEvent()?.formatSchedule(context)
                        setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        viewModel.isNameTaken.observe(this) {
            binding.eventNameTextInputLayout.error =
                if (it) getString(R.string.feedback_event_name_exists)
                else null
        }

        binding.eventNameTextInput.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is TextInputEditText) {
                viewModel.setName(v.text.toString())
            }
        }

        binding.locationTextInput.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is TextInputEditText) {
                viewModel.setLocation(v.text.toString())
            }
        }

        binding.notesTextInput.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is TextInputEditText) {
                viewModel.setNotes(v.text.toString())
            }
        }

        binding.prioritySwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setImportant(isChecked)
            binding.priorityCard.isVisible = isChecked
        }

        binding.scheduleTextView.setOnClickListener { v ->
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                dateTimePicker(
                    requireFutureDateTime = true,
                    currentDateTime = viewModel.getSchedule()?.toCalendar(),
                    show24HoursView = is24HourFormat(requireContext())
                ) { _, datetime ->
                    viewModel.setSchedule(datetime.toZonedDateTime())
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) {
                        v.text = viewModel.getEvent()?.formatSchedule(context)
                        v.setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.subjectTextView.setOnClickListener {
            SubjectPickerFragment(childFragmentManager)
                .show()
        }

        binding.removeButton.setOnClickListener {
            binding.subjectTextView.startAnimation(animation)
            viewModel.setSubject(null)
        }

        binding.dateTimeRadioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            for (v: View in radioGroup.children) {
                if (v is TwoLineRadioButton && !v.isChecked) {
                    v.titleTextColor = ContextCompat.getColor(
                        v.context,
                        R.color.color_secondary_text
                    )
                    v.subtitle = null
                }
            }
        }

        binding.inNextMeetingRadio.setOnClickListener {
            viewModel.setNextMeetingForDueDate()

            with(binding.inNextMeetingRadio) {
                titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                subtitle = viewModel.getEvent()?.formatSchedule(context)
            }
        }

        binding.pickDateTimeRadio.setOnClickListener {
            hideKeyboardFromCurrentFocus(requireView())

            SchedulePickerSheet
                .show(viewModel.schedules, childFragmentManager)
        }

        binding.customDateTimeRadio.setOnClickListener {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                dateTimePicker(
                    requireFutureDateTime = true,
                    currentDateTime = viewModel.getSchedule()?.toCalendar(),
                    show24HoursView = is24HourFormat(requireContext())
                ) { _, datetime ->
                    viewModel.setSchedule(
                        ZonedDateTime.ofInstant(
                            datetime.toInstant(),
                            ZoneId.systemDefault()
                        )
                    )
                }
                positiveButton(R.string.button_done) {

                    with(binding.customDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(
                            context,
                            R.color.color_primary_text
                        )
                        subtitle = viewModel.getEvent()?.formatSchedule(context)
                    }
                }
                negativeButton { binding.customDateTimeRadio.isChecked = false }
            }
        }

        binding.actionButton.setOnClickListener {
            viewModel.setName(binding.eventNameTextInput.text.toString())
            viewModel.setLocation(binding.locationTextInput.text.toString())

            // Conditions to check if the fields are null or blank
            // then if resulted true, show a feedback then direct
            // user focus to the field and stop code execution.
            if (viewModel.getName()?.isEmpty() == true) {
                createSnackbar(R.string.feedback_event_empty_name, binding.root)
                binding.eventNameTextInput.requestFocus()
                return@setOnClickListener
            }

            if (viewModel.getLocation()?.isEmpty() == true) {
                createSnackbar(R.string.feedback_event_empty_location, binding.root)
                binding.locationTextInput.requestFocus()
                return@setOnClickListener
            }

            if (viewModel.getSchedule() == null) {
                createSnackbar(R.string.feedback_event_empty_schedule, binding.root)
                binding.scheduleTextView.performClick()
                return@setOnClickListener
            }

            viewModel.setImportant(binding.prioritySwitch.isChecked)

            if (requestKey == REQUEST_KEY_INSERT)
                viewModel.insert()
            else viewModel.update()

            if (controller?.graph?.id == R.id.navigation_container_event)
                requireActivity().finish()
            else controller?.navigateUp()
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            SubjectPickerFragment.REQUEST_KEY_PICK -> {
                result.getParcelable<SubjectPackage>(SubjectPickerFragment.EXTRA_SELECTED_SUBJECT)?.let {
                    viewModel.setSubject(it.subject)
                    viewModel.schedules = it.schedules
                }
            }
            SchedulePickerSheet.REQUEST_KEY -> {
                result.getParcelable<Schedule>(SchedulePickerSheet.EXTRA_SCHEDULE)?.also {
                    viewModel.setClassScheduleAsDueDate(it)

                    with(binding.pickDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(
                            context,
                            R.color.color_primary_text
                        )
                        subtitle = viewModel.getEvent()?.formatSchedule(context)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        /**
         * Check if the soft keyboard is visible
         * then hide it before the user leaves
         * the fragment
         */
        hideKeyboardFromCurrentFocus(requireView())
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(receiver)
    }

    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BaseService.ACTION_SERVICE_BROADCAST) {
                when (intent.getStringExtra(BaseService.EXTRA_BROADCAST_STATUS)) {
                    DataExporterService.BROADCAST_EXPORT_ONGOING -> {
                        createSnackbar(
                            R.string.feedback_export_ongoing, binding.root,
                            Snackbar.LENGTH_INDEFINITE
                        )
                    }
                    DataExporterService.BROADCAST_EXPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_export_completed, binding.root)

                        intent.getStringExtra(BaseService.EXTRA_BROADCAST_DATA)?.also {
                            val uri = Fokus.obtainUriForFile(requireContext(), File(it))

                            startActivity(
                                ShareCompat.IntentBuilder.from(requireActivity())
                                    .addStream(uri)
                                    .setType(Streamable.MIME_TYPE_ZIP)
                                    .setChooserTitle(R.string.dialog_send_to)
                                    .intent
                            )
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

                        intent.getParcelableExtra<EventPackage>(BaseService.EXTRA_BROADCAST_DATA)
                            ?.also {
                                viewModel.setEvent(it.event)
                            }
                    }
                    DataImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, binding.root)
                    }
                }
            }
        }
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_export -> {
                val fileName = getSharingName()
                if (fileName == null) {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.feedback_unable_to_share_title)
                        message(R.string.feedback_unable_to_share_message)
                        positiveButton(R.string.button_done) { dismiss() }
                    }
                    return false
                }

                /**
                 * Make the user specify where to save
                 * the exported file
                 */
                exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, fileName)
                    type = Streamable.MIME_TYPE_ZIP
                })
            }
            R.id.action_share -> {
                val fileName = getSharingName()
                if (fileName == null) {
                    MaterialDialog(requireContext()).show {
                        lifecycleOwner(viewLifecycleOwner)
                        title(R.string.feedback_unable_to_share_title)
                        message(R.string.feedback_unable_to_share_message)
                        positiveButton(R.string.button_done) { dismiss() }
                    }
                    return false
                }

                /**
                 * We need first to export the data as a raw file
                 * then pass it onto the system sharing component
                 */
                context?.startService(Intent(context, DataExporterService::class.java).apply {
                    action = DataExporterService.ACTION_EXPORT_EVENT
                    putExtra(
                        DataExporterService.EXTRA_EXPORT_SOURCE,
                        viewModel.getEvent()
                    )
                })
            }
            R.id.action_import -> {
                val chooser = Intent.createChooser(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = Streamable.MIME_TYPE_ZIP
                }, getString(R.string.dialog_select_file_import))

                importLauncher.launch(chooser)
            }
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            viewModel.setEvent(getParcelable(EXTRA_EVENT))
            viewModel.setSubject(getParcelable(EXTRA_SUBJECT))
        }
    }

    private fun getSharingName(): String? {
        return when (requestKey) {
            REQUEST_KEY_INSERT -> {
                if (viewModel.getName()?.isEmpty() == true || viewModel.getLocation()
                        ?.isEmpty() == true ||
                    viewModel.getSchedule() == null
                ) {
                    MaterialDialog(requireContext()).show {
                        title(R.string.feedback_unable_to_share_title)
                        message(R.string.feedback_unable_to_share_message)
                        positiveButton(R.string.button_dismiss) { dismiss() }
                    }
                    return null
                }

                viewModel.getName() ?: Streamable.ARCHIVE_NAME_GENERIC
            }
            REQUEST_KEY_UPDATE -> {
                viewModel.getName() ?: Streamable.ARCHIVE_NAME_GENERIC
            }
            else -> null
        }
    }

    companion object {
        const val REQUEST_KEY_INSERT = "request:insert"
        const val REQUEST_KEY_UPDATE = "request:update"

        const val EXTRA_EVENT = "extra:event"
        const val EXTRA_SUBJECT = "extra:subject"
    }
}