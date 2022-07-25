package com.isaiahvonrundstedt.fokus.features.subject.editor

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ShareCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.isaiahvonrundstedt.fokus.Fokus
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.DataExporterService
import com.isaiahvonrundstedt.fokus.components.service.DataImporterService
import com.isaiahvonrundstedt.fokus.databinding.FragmentEditorSubjectBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditorFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import dagger.hilt.android.AndroidEntryPoint
import me.saket.cascade.overrideOverflowMenu
import java.io.File

@AndroidEntryPoint
class SubjectEditorFragment : BaseEditorFragment(),  FragmentResultListener {
    private var _binding: FragmentEditorSubjectBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_INSERT

    private val viewModel: SubjectEditorViewModel by viewModels()
    private val binding get() = _binding!!

    private lateinit var importLauncher: ActivityResultLauncher<Intent>
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()

        importLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    context?.startService(Intent(context, DataImporterService::class.java).apply {
                        this.data = it.data?.data
                        action = DataImporterService.ACTION_IMPORT_SUBJECT
                    })
                }
            }

        exportLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    context?.startService(Intent(context, DataExporterService::class.java).apply {
                        this.data = it.data?.data
                        action = DataExporterService.ACTION_EXPORT_SUBJECT
                        putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.getSubject())
                        putExtra(
                            DataExporterService.EXTRA_EXPORT_DEPENDENTS,
                            viewModel.getSchedules()
                        )
                    })
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorSubjectBinding.inflate(inflater, container, false)
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
                if (controller?.graph?.id == R.id.navigation_container_subject)
                    requireActivity().finish()
                else controller?.navigateUp()
            }

            overrideOverflowMenu(::customPopupProvider)
            setOnMenuItemClickListener(::onMenuItemClicked)
        }

        arguments?.getBundle(EXTRA_SUBJECT)?.also {
            requestKey = REQUEST_KEY_UPDATE

            Subject.fromBundle(it)?.also { subject ->
                viewModel.setSubject(subject)
                binding.root.transitionName = TRANSITION_ELEMENT_ROOT + subject.subjectID
            }
        }
        arguments?.getParcelableArrayList<Schedule>(EXTRA_SCHEDULE)?.also {
            viewModel.setSchedules(it)
        }

        registerForFragmentResult(
            arrayOf(
                ScheduleEditor.REQUEST_KEY_INSERT,
                ScheduleEditor.REQUEST_KEY_UPDATE
            ), this
        )
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

        if (requestKey == REQUEST_KEY_UPDATE) {
            binding.codeTextInput.setText(viewModel.getCode())
            binding.descriptionTextInput.setText(viewModel.getDescription())
            binding.instructorTextInput.setText(viewModel.getInstructor())
        }

        viewModel.subject.observe(this) {
            if (requestKey == REQUEST_KEY_UPDATE && it != null) {
                with(it) {
                    binding.codeTextInput.setText(code)
                    binding.descriptionTextInput.setText(description)
                    binding.instructorTextInput.setText(instructor)
                    binding.tagView.setCompoundDrawableAtStart(binding.tagView.getCompoundDrawableAtStart()
                        ?.let { drawable -> tintDrawable(drawable) })
                    binding.tagView.setText(tag.getNameResource())
                }

                binding.tagView.setTextColorFromResource(R.color.color_primary_text)
            }
        }

        viewModel.schedules.observe(this) {
            binding.schedulesChipGroup.removeAllViews()
            it.forEach { schedule ->
                binding.schedulesChipGroup.addView(Chip(requireContext()).apply {
                    text = schedule.formatDaysOfWeek(requireContext(), true)
                    tag = schedule.scheduleID
                    isCloseIconVisible = true
                    setCloseIconResource(R.drawable.ic_outline_close_24)
                    setOnClickListener {
                        ScheduleEditor(childFragmentManager).show {
                            arguments = bundleOf(
                                ScheduleEditor.EXTRA_SUBJECT_ID to viewModel.getID(),
                                ScheduleEditor.EXTRA_SCHEDULE to schedule
                            )
                        }
                    }
                    setOnCloseIconClickListener {
                        viewModel.removeSchedule(schedule)
                        createSnackbar(R.string.feedback_schedule_removed, binding.root).run {
                            setAction(R.string.button_undo) { viewModel.addSchedule(schedule) }
                        }
                    }
                })
            }
        }

        viewModel.isCodeExists.observe(this) {
            binding.codeTextInputLayout.error =
                if (it) getString(R.string.feedback_subject_code_exists)
                else null
        }

        binding.codeTextInput.doAfterTextChanged {
            viewModel.checkCodeUniqueness(it.toString())
        }

        binding.codeTextInput.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is TextInputEditText) {
                viewModel.setCode(v.text.toString())
            }
        }

        binding.descriptionTextInput.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is TextInputEditText) {
                viewModel.setDescription(v.text.toString())
            }
        }

        binding.instructorTextInput.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is TextInputEditText) {
                viewModel.setInstructor(v.text.toString())
            }
        }

        binding.addActionChip.setOnClickListener {
            hideKeyboardFromCurrentFocus(requireView())

            ScheduleEditor(childFragmentManager).show {
                arguments = bundleOf(
                    ScheduleEditor.EXTRA_SUBJECT_ID to viewModel.getID()
                )
            }
        }

        binding.tagView.setOnClickListener {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(this@SubjectEditorFragment)
                title(R.string.dialog_pick_color_tag)
                colorChooser(Subject.Tag.getColors(), waitForPositiveButton = false) { _, color ->
                    viewModel.setTag(Subject.Tag.convertColorToTag(color) ?: Subject.Tag.SKY)

                    with(it as TextView) {
                        text = getString(viewModel.getTag()!!.getNameResource())
                        setTextColorFromResource(R.color.color_primary_text)
                        setCompoundDrawableAtStart(
                            viewModel.getSubject()?.tintDrawable(getCompoundDrawableAtStart())
                        )
                    }
                    this.dismiss()
                }
            }
        }

        binding.actionButton.setOnClickListener {
            viewModel.setCode(binding.codeTextInput.text.toString())
            viewModel.setDescription(binding.descriptionTextInput.text.toString())
            viewModel.setInstructor(binding.instructorTextInput.text.toString())

            if (viewModel.getCode()?.isEmpty() == true) {
                createSnackbar(R.string.feedback_subject_empty_name, binding.root)
                binding.codeTextInput.requestFocus()
                return@setOnClickListener
            }

            if (viewModel.getDescription()?.isEmpty() == true) {
                createSnackbar(R.string.feedback_subject_empty_description, binding.root)
                binding.descriptionTextInput.requestFocus()
                return@setOnClickListener
            }

            if (viewModel.getSchedules().size < 1) {
                createSnackbar(R.string.feedback_subject_no_schedule, binding.root).show()
                return@setOnClickListener
            }


            if (requestKey == REQUEST_KEY_INSERT)
                viewModel.insert()
            else viewModel.update()

            if (controller?.graph?.id == R.id.navigation_container_subject)
                requireActivity().finish()
            else controller?.navigateUp()
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

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            ScheduleEditor.REQUEST_KEY_INSERT -> {
                result.getParcelable<Schedule>(ScheduleEditor.EXTRA_SCHEDULE)?.also {
                    viewModel.addSchedule(it)
                }
            }
            ScheduleEditor.REQUEST_KEY_UPDATE -> {
                result.getParcelable<Schedule>(ScheduleEditor.EXTRA_SCHEDULE)?.also {
                    viewModel.updateSchedule(it)
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
                    action = DataExporterService.ACTION_EXPORT_SUBJECT
                    putExtra(
                        DataExporterService.EXTRA_EXPORT_SOURCE,
                        viewModel.getSubject()
                    )
                    putExtra(
                        DataExporterService.EXTRA_EXPORT_DEPENDENTS,
                        viewModel.getSchedules()
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

                        intent.getParcelableExtra<SubjectPackage>(BaseService.EXTRA_BROADCAST_DATA)
                            ?.also {
                                viewModel.setSubject(it.subject)
                                viewModel.setSchedules(ArrayList(it.schedules))
                            }
                    }
                    DataImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, binding.root)
                    }
                }
            }
        }
    }

    private fun getSharingName(): String? {
        return when (requestKey) {
            REQUEST_KEY_INSERT -> {
                if (binding.codeTextInput.text.isNullOrEmpty() ||
                    binding.descriptionTextInput.text.isNullOrEmpty()
                    || binding.schedulesChipGroup.children.none()
                ) {
                    MaterialDialog(requireContext()).show {
                        title(R.string.feedback_unable_to_share_title)
                        message(R.string.feedback_unable_to_share_message)
                        positiveButton(R.string.button_dismiss) { dismiss() }
                    }
                    return null
                }
                binding.codeTextInput.text.toString()
            }
            REQUEST_KEY_UPDATE -> {
                viewModel.getCode() ?: Streamable.ARCHIVE_NAME_GENERIC
            }
            else -> null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        with(outState) {
            putParcelable(EXTRA_SUBJECT, viewModel.getSubject())
            putParcelableArrayList(EXTRA_SCHEDULE, viewModel.getSchedules())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            viewModel.setSubject(getParcelable(EXTRA_SUBJECT))
            viewModel.setSchedules(getParcelableArrayList(EXTRA_SCHEDULE) ?: arrayListOf())
        }
    }

    companion object {
        const val REQUEST_KEY_INSERT = "request:insert"
        const val REQUEST_KEY_UPDATE = "request:update"

        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_SCHEDULE = "extra:schedule"
    }
}