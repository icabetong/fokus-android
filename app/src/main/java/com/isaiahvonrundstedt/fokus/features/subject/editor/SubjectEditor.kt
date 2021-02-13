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
import androidx.core.app.ShareCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.ShareOptionsBottomSheet
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.DataExporterService
import com.isaiahvonrundstedt.fokus.components.service.DataImporterService
import com.isaiahvonrundstedt.fokus.databinding.EditorSubjectBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleAdapter
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class SubjectEditor : BaseEditor(), BaseAdapter.ActionListener {
    private var _binding: EditorSubjectBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_INSERT

    private val scheduleAdapter = ScheduleAdapter(this)
    private val viewModel: SubjectEditorViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = EditorSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.transitionName = TRANSITION_ELEMENT_ROOT
        controller = Navigation.findNavController(view)

        binding.appBarLayout.toolbar.setNavigationOnClickListener { controller?.navigateUp() }

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

        sharedElementEnterTransition = getTransition()
        sharedElementReturnTransition = getTransition()

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = scheduleAdapter
        }

        var currentScrollPosition = 0
        binding.contentView.viewTreeObserver.addOnScrollChangedListener {
            if (binding.contentView.scrollY > currentScrollPosition)
                binding.actionButton.hide()
            else binding.actionButton.show()
            currentScrollPosition = binding.contentView.scrollY
        }

        childFragmentManager.setFragmentResultListener(ShareOptionsBottomSheet.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            var fileName = viewModel.getCode() ?: Streamable.ARCHIVE_NAME_GENERIC
            when (requestKey) {
                REQUEST_KEY_INSERT -> {
                    if (binding.codeTextInput.text.isNullOrEmpty() ||
                        binding.descriptionTextInput.text.isNullOrEmpty()
                        || scheduleAdapter.itemCount < 1) {
                        MaterialDialog(requireContext()).show {
                            title(R.string.feedback_unable_to_share_title)
                            message(R.string.feedback_unable_to_share_message)
                            positiveButton(R.string.button_dismiss) { dismiss() }
                        }
                        return@setFragmentResultListener
                    }
                    fileName = binding.codeTextInput.text.toString()
                }
                REQUEST_KEY_UPDATE -> {
                    fileName = viewModel.getCode() ?: Streamable.ARCHIVE_NAME_GENERIC
                }
            }
            bundle.getInt(ShareOptionsBottomSheet.EXTRA_SHARE_OPTION).also {
                when (it) {
                    R.id.action_export -> {
                        val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(Intent.EXTRA_TITLE, fileName)
                            type = Streamable.MIME_TYPE_ZIP
                        }

                        this@SubjectEditor.startActivityForResult(exportIntent,
                            REQUEST_CODE_EXPORT)
                    }
                    R.id.action_share -> {
                        val serviceIntent = Intent(context, DataExporterService::class.java).apply {
                            action = DataExporterService.ACTION_EXPORT_SUBJECT
                            putExtra(DataExporterService.EXTRA_EXPORT_SOURCE,
                                viewModel.getSubject())
                            putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS,
                                viewModel.getSchedules())
                        }

                        context?.startService(serviceIntent)
                    }
                }
            }
        }
        childFragmentManager.setFragmentResultListener(ScheduleEditor.REQUEST_KEY_INSERT, viewLifecycleOwner) { _, args ->
            args.getParcelable<Schedule>(ScheduleEditor.EXTRA_SCHEDULE)?.also {
                viewModel.addSchedule(it)
            }
        }
        childFragmentManager.setFragmentResultListener(ScheduleEditor.REQUEST_KEY_UPDATE, viewLifecycleOwner) { _, args ->
            args.getParcelable<Schedule>(ScheduleEditor.EXTRA_SCHEDULE)?.also {
                viewModel.updateSchedule(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

        viewModel.subject.observe(this) {
            if (requestKey == REQUEST_KEY_UPDATE && it != null) {
                with(it) {
                    binding.codeTextInput.setText(code)
                    binding.descriptionTextInput.setText(description)
                    binding.tagView.setCompoundDrawableAtStart(binding.tagView.getCompoundDrawableAtStart()
                        ?.let { drawable -> tintDrawable(drawable) })
                    binding.tagView.setText(tag.getNameResource())
                }

                binding.tagView.setTextColorFromResource(R.color.color_primary_text)
            }
        }

        viewModel.schedules.observe(this) {
            scheduleAdapter.submitList(ArrayList(it))
        }

        binding.addActionLayout.addItemButton.setOnClickListener {
            ScheduleEditor(childFragmentManager).show {
                arguments = bundleOf(
                    ScheduleEditor.EXTRA_SUBJECT_ID to viewModel.getID()
                )
            }
        }

        binding.tagView.setOnClickListener {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(this@SubjectEditor)
                title(R.string.dialog_pick_color_tag)
                colorChooser(Subject.Tag.getColors(), waitForPositiveButton = false) { _, color ->
                    viewModel.setTag(Subject.Tag.convertColorToTag(color) ?: Subject.Tag.SKY)

                    with(it as TextView) {
                        text = getString(viewModel.getTag()!!.getNameResource())
                        setTextColorFromResource(R.color.color_primary_text)
                        setCompoundDrawableAtStart(
                            viewModel.getSubject()?.tintDrawable(getCompoundDrawableAtStart()))
                    }
                    this.dismiss()
                }
            }
        }

        binding.actionButton.setOnClickListener {
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

            viewModel.setCode(binding.codeTextInput.text.toString())
            viewModel.setDescription(binding.descriptionTextInput.text.toString())

            if (requestKey == REQUEST_KEY_INSERT)
                viewModel.insert()
            else viewModel.update()

            controller?.navigateUp()
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       container: View?) {
        if (t is Schedule) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    ScheduleEditor(childFragmentManager).show {
                        arguments = bundleOf(
                            ScheduleEditor.EXTRA_SUBJECT_ID to viewModel.getID(),
                            ScheduleEditor.EXTRA_SCHEDULE to t)
                    }
                }
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.removeSchedule(t)
                    createSnackbar(R.string.feedback_schedule_removed, binding.root).run {
                        setAction(R.string.button_undo) { viewModel.addSchedule(t) }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext())
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
                            val uri = CoreApplication.obtainUriForFile(requireContext(), File(it))

                            startActivity(ShareCompat.IntentBuilder.from(requireActivity())
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

                        intent.getParcelableExtra<SubjectPackage>(BaseService.EXTRA_BROADCAST_DATA)?.also {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            REQUEST_CODE_IMPORT ->
                context?.startService(Intent(context, DataImporterService::class.java).apply {
                    this.data = data?.data
                    action = DataImporterService.ACTION_IMPORT_SUBJECT
                })
            REQUEST_CODE_EXPORT ->
                context?.startService(Intent(context, DataExporterService::class.java).apply {
                    this.data = data?.data
                    action = DataExporterService.ACTION_EXPORT_SUBJECT
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.getSubject())
                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, viewModel.getSchedules())
                })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share_options -> {
                ShareOptionsBottomSheet(childFragmentManager)
                    .show()
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

//    override fun onSaveInstanceState(outState: Bundle) {
//        with(outState) {
//            putParcelable(EXTRA_SUBJECT, viewModel.getSubject())
//            putParcelableArrayList(EXTRA_SCHEDULE, viewModel.getSchedules())
//        }
//        super.onSaveInstanceState(outState)
//    }
//
//    override fun onViewStateRestored(savedInstanceState: Bundle?) {
//        super.onViewStateRestored(savedInstanceState)
//        savedInstanceState?.run {
//            viewModel.setSubject(getParcelable(EXTRA_SUBJECT))
//            viewModel.setSchedules(getParcelableArrayList(EXTRA_SCHEDULE) ?: arrayListOf())
//        }
//    }

    companion object {
        const val REQUEST_KEY_INSERT = "request:insert"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val REQUEST_CODE_INSERT = 27
        const val REQUEST_CODE_UPDATE = 13

        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_SCHEDULE = "extra:schedule"

        private const val REQUEST_CODE_EXPORT = 32
        private const val REQUEST_CODE_IMPORT = 95
    }
}