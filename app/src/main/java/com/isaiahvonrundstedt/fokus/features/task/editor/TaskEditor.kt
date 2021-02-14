package com.isaiahvonrundstedt.fokus.features.task.editor

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.ShareOptionsSheet
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toCalendar
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toZonedDateTime
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.DataExporterService
import com.isaiahvonrundstedt.fokus.components.service.DataImporterService
import com.isaiahvonrundstedt.fokus.components.service.FileImporterService
import com.isaiahvonrundstedt.fokus.components.utils.PermissionManager
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.components.views.RadioButtonCompat
import com.isaiahvonrundstedt.fokus.components.views.TwoLineRadioButton
import com.isaiahvonrundstedt.fokus.databinding.EditorTaskBinding
import com.isaiahvonrundstedt.fokus.databinding.LayoutDialogInputAttachmentBinding
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.attachments.AttachmentAdapter
import com.isaiahvonrundstedt.fokus.features.attachments.AttachmentOptionSheet
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.picker.SchedulePickerSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBasicAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import com.isaiahvonrundstedt.fokus.features.subject.picker.SubjectPickerActivity
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.ZonedDateTime
import javax.inject.Inject

@AndroidEntryPoint
class TaskEditor : BaseEditor(), BaseBasicAdapter.ActionListener<Attachment>, FragmentResultListener {
    private var _binding: EditorTaskBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_INSERT

    private val attachmentAdapter = AttachmentAdapter(this)
    private val viewModel: TaskEditorViewModel by viewModels()
    private val binding get() = _binding!!

    private lateinit var subjectLauncher: ActivityResultLauncher<Intent>
    private lateinit var attachmentLauncher: ActivityResultLauncher<Intent>
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private lateinit var importLauncher: ActivityResultLauncher<Intent>

    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subjectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getParcelableExtra<SubjectPackage>(SubjectPickerActivity.EXTRA_SELECTED_SUBJECT)
                    ?.also {
                        viewModel.setSubject(it.subject)
                        viewModel.schedules = ArrayList(it.schedules)
                    }
            }
        }

        attachmentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val service = Intent(context, FileImporterService::class.java).apply {
                    action = FileImporterService.ACTION_START
                    data = it.data?.data
                    putExtra(FileImporterService.EXTRA_DIRECTORY,
                        Streamable.DIRECTORY_ATTACHMENTS)
                }
                context?.startService(service)
            }
        }

        exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                context?.startService(Intent(context, DataExporterService::class.java).apply {
                    this.data = it.data?.data
                    action = DataExporterService.ACTION_EXPORT_TASK
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.getTask())
                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, viewModel.getAttachments())
                })
            }
        }

        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            context?.startService(Intent(context, DataImporterService::class.java).apply {
                this.data = it.data?.data
                action = DataImporterService.ACTION_IMPORT_TASK
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = EditorTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.transitionName = TRANSITION_ELEMENT_ROOT
        controller = Navigation.findNavController(view)

        binding.appBarLayout.toolbar.inflateMenu(R.menu.menu_editor)
        binding.appBarLayout.toolbar.setNavigationOnClickListener { controller?.navigateUp() }
        binding.appBarLayout.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_share_options -> {
                    ShareOptionsSheet(childFragmentManager)
                        .show()
                }
                R.id.action_import -> {
                    val chooser = Intent.createChooser(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        type = Streamable.MIME_TYPE_ZIP
                    }, getString(R.string.dialog_select_file_import))

                    importLauncher.launch(chooser)
                }
            }
            true
        }

        arguments?.getBundle(EXTRA_TASK)?.also {
            requestKey = REQUEST_KEY_UPDATE

            Task.fromBundle(it)?.also { task ->
                viewModel.setTask(task)
                binding.root.transitionName = TRANSITION_ELEMENT_ROOT + task.taskID
            }
        }
        arguments?.getParcelableArrayList<Attachment>(EXTRA_ATTACHMENTS)?.also {
            viewModel.setAttachments(it)
        }
        arguments?.getBundle(EXTRA_SUBJECT)?.also {
            viewModel.setSubject(Subject.fromBundle(it))
        }

        sharedElementEnterTransition = getTransition()
        sharedElementReturnTransition = getTransition()

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = attachmentAdapter
        }

        var currentScrollPosition = 0
        binding.contentView.viewTreeObserver.addOnScrollChangedListener {
            if (binding.contentView.scrollY > currentScrollPosition)
                binding.actionButton.hide()
            else binding.actionButton.show()
            currentScrollPosition = binding.contentView.scrollY
        }

        registerForFragmentResult(
            arrayOf(ShareOptionsSheet.REQUEST_KEY,
                SchedulePickerSheet.REQUEST_KEY,
                AttachmentOptionSheet.REQUEST_KEY), this)
    }

    private val dialogView: View by lazy {
        LayoutDialogInputAttachmentBinding.inflate(layoutInflater).root
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

        viewModel.task.observe(this) {
            if (requestKey == REQUEST_KEY_UPDATE && it != null) {
                with(it) {
                    binding.taskNameTextInput.setText(name)
                    binding.notesTextInput.setText(notes)
                    binding.prioritySwitch.isChecked = isImportant
                    binding.statusSwitch.isChecked = isFinished
                    binding.removeDueDateButton.isVisible = dueDate != null

                    if (it.hasDueDate()) {
                        binding.dueDateTextView.text = formatDueDate(requireContext())
                        binding.dueDateTextView.setTextColorFromResource(R.color.color_primary_text)
                    } else {
                        binding.dueDateTextView.setText(R.string.field_not_set)
                        binding.dueDateTextView.setTextColorFromResource(R.color.color_secondary_text)
                    }
                }
            }
        }

        viewModel.attachments.observe(this) {
            attachmentAdapter.submitList(it)
        }

        viewModel.subject.observe(this) {
            binding.removeButton.isVisible = it != null
            binding.dateTimeRadioGroup.isVisible = it != null
            binding.dueDateTextView.isVisible = it == null
            binding.removeDueDateButton.isVisible = it == null && viewModel.getDueDate() == null

            if (it != null) {
                with(binding.subjectTextView) {
                    text = it.code
                    setTextColorFromResource(R.color.color_primary_text)
                    setCompoundDrawableAtStart(ContextCompat.getDrawable(context,
                        R.drawable.shape_color_holder)?.let { shape -> it.tintDrawable(shape) })
                }

                if (viewModel.getDueDate() == null) {
                    with(binding.customDateTimeRadio) {
                        isChecked = true
                        titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                        subtitle = viewModel.getTask()?.formatDueDate(context)
                    }
                }
            } else {
                with(binding.subjectTextView) {
                    setText(R.string.field_not_set)
                    setTextColorFromResource(R.color.color_secondary_text)
                    removeCompoundDrawableAtStart()
                }

                if (viewModel.getDueDate() == null) {
                    with(binding.dueDateTextView) {
                        text = viewModel.getTask()?.formatDueDate(context)
                        setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.addActionLayout.addItemButton.setOnClickListener {
            AttachmentOptionSheet
                .show(childFragmentManager)
        }

        binding.dueDateTextView.setOnClickListener {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getDueDate()?.toCalendar()) { _, datetime ->
                    viewModel.setDueDate(datetime.toZonedDateTime())
                    binding.removeDueDateButton.isVisible = true
                }
                positiveButton(R.string.button_done) {
                    with(binding.dueDateTextView) {
                        text = viewModel.getTask()?.formatDueDate(context)
                        setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.removeDueDateButton.setOnClickListener {
            viewModel.setDueDate(null)

            binding.removeDueDateButton.isVisible = false
            binding.dueDateTextView.setText(R.string.field_not_set)
            binding.dueDateTextView.setTextColor(ContextCompat.getColor(it.context,
                R.color.color_secondary_text))
        }

        binding.subjectTextView.setOnClickListener {
            subjectLauncher.launch(Intent(context, SubjectPickerActivity::class.java))
        }

        binding.removeButton.setOnClickListener {
            binding.dueDateTextView.setText(R.string.field_not_set)
            binding.dueDateTextView.setTextColor(ContextCompat.getColor(it.context,
                R.color.color_secondary_text))
            binding.subjectTextView.startAnimation(animation)

            it.isVisible = false
            viewModel.setSubject(null)
        }

        // When a radio button has been checked, set the other
        // radio buttons text color to colorSecondaryText
        binding.dateTimeRadioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            for (v: View in radioGroup.children) {
                if (v is TwoLineRadioButton && !v.isChecked) {
                    v.titleTextColor = ContextCompat.getColor(v.context,
                        R.color.color_secondary_text)
                    v.subtitle = null
                } else if (v is RadioButtonCompat && !v.isChecked) {
                    v.setTextColor(ContextCompat.getColor(v.context,
                        R.color.color_secondary_text))
                }
            }
        }

        binding.noDueRadioButton.setOnClickListener {
            viewModel.setDueDate(null)
            binding.dueDateTextView.setText(R.string.field_not_set)
            (it as RadioButtonCompat).setTextColor(ContextCompat.getColor(it.context,
                R.color.color_primary_text))
        }

        binding.inNextMeetingRadio.setOnClickListener {
            viewModel.setNextMeetingForDueDate()
            with(binding.inNextMeetingRadio) {
                titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                subtitle = viewModel.getTask()?.formatDueDate(context)
            }
        }

        binding.pickDateTimeRadio.setOnClickListener {
            SchedulePickerSheet
                .show(viewModel.schedules, childFragmentManager)
        }

        binding.customDateTimeRadio.setOnClickListener {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getDueDate()?.toCalendar()) { _, datetime ->
                    viewModel.setDueDate(datetime.toZonedDateTime())
                }
                positiveButton(R.string.button_done) {
                    with(binding.customDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getTask()?.formatDueDate(context)
                    }
                }
                negativeButton { binding.customDateTimeRadio.isChecked = false }
            }
        }

        binding.actionButton.setOnClickListener {

            // These if checks if the user have entered the
            // values on the fields, if we don't have the value required,
            // show a snackbar feedback then direct the user's
            // attention to the field. Then return to stop the execution
            // of the code.
            if (viewModel.getName()?.isEmpty() == true) {
                createSnackbar(R.string.feedback_task_empty_name, binding.root)
                binding.taskNameTextInput.requestFocus()
                return@setOnClickListener
            }

            viewModel.setImportant(binding.prioritySwitch.isChecked)
            viewModel.setFinished(binding.statusSwitch.isChecked)

            if (requestKey == REQUEST_KEY_INSERT)
                viewModel.insert()
            else viewModel.update()
            controller?.navigateUp()
        }
    }

    private fun triggerSystemFilePickerForAttachment() {
        attachmentLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("*/*"))
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(receiver)

        // Cancel all current import processes
        context?.startService(Intent(context, FileImporterService::class.java).apply {
            action = FileImporterService.ACTION_CANCEL
        })
    }

    private var receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BaseService.ACTION_SERVICE_BROADCAST) {
                when (intent.getStringExtra(BaseService.EXTRA_BROADCAST_STATUS)) {
                    FileImporterService.BROADCAST_IMPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_import_ongoing, binding.root,
                            Snackbar.LENGTH_INDEFINITE)
                    }
                    FileImporterService.BROADCAST_IMPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_import_completed, binding.root)

                        val attachment = intent.getStringExtra(BaseService.EXTRA_BROADCAST_DATA)?.let {
                            createAttachment(it, Attachment.TYPE_IMPORTED_FILE)
                        }
                        if (attachment != null) {
                            val file = attachment.target?.let { File(it) }
                            attachment.name = file?.name
                            viewModel.addAttachment(attachment)

                            binding.appBarLayout.toolbar.menu?.findItem(R.id.action_share_options)?.isVisible = !viewModel.hasFileAttachment()
                        }
                    }
                    FileImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, binding.root)
                    }
                    DataExporterService.BROADCAST_EXPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_export_ongoing, binding.root)
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

                        intent.getParcelableExtra<TaskPackage>(BaseService.EXTRA_BROADCAST_DATA)?.also {
                            viewModel.setTask(it.task)
                            viewModel.setAttachments(ArrayList(it.attachments))
                        }
                    }
                    DataImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, binding.root)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == PermissionManager.STORAGE_READ_REQUEST_CODE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            triggerSystemFilePickerForAttachment()
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun createAttachment(targetPath: String, attachmentType: Int): Attachment {
        return Attachment().apply {
            task = viewModel.getID()!!
            target = targetPath
            dateAttached = ZonedDateTime.now()
            type = attachmentType
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            ShareOptionsSheet.REQUEST_KEY -> {
                result.getInt(ShareOptionsSheet.EXTRA_SHARE_OPTION).also {
                    triggerSystemShareSheet(it)
                }
            }
            SchedulePickerSheet.REQUEST_KEY -> {
                result.getParcelable<Schedule>(SchedulePickerSheet.EXTRA_SCHEDULE)?.also {
                    viewModel.setClassScheduleAsDueDate(it)

                    with(binding.pickDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getTask()?.formatDueDate(context)
                    }
                }
            }
            AttachmentOptionSheet.REQUEST_KEY -> {
                result.getInt(AttachmentOptionSheet.EXTRA_OPTION).also {
                    when (it) {
                        R.id.action_import_file -> {
                            // Check if we have read storage permissions then request the permission
                            // if we have the permission, open up file picker
                            if (permissionManager.readStorageGranted) {
                                if (!preferenceManager.noConfirmImport)
                                    MaterialDialog(requireContext()).show {
                                        lifecycleOwner(viewLifecycleOwner)
                                        title(R.string.dialog_import_attachment_title)
                                        message(R.string.dialog_import_attachment_summary)
                                        checkBoxPrompt(R.string.dialog_import_attachment_confirm) { isChecked ->
                                            preferenceManager.noConfirmImport = isChecked
                                        }
                                        positiveButton(R.string.button_continue) {
                                            triggerSystemFilePickerForAttachment()
                                        }
                                        negativeButton(R.string.button_cancel)
                                    }
                                else triggerSystemFilePickerForAttachment()
                            } else
                                PermissionManager.requestReadStoragePermission(requireActivity())
                        }
                        R.id.action_website_url -> {
                            var attachment: Attachment?

                            MaterialDialog(requireContext()).show {
                                title(res = R.string.dialog_enter_website_url)
                                customView(view = dialogView)
                                positiveButton(R.string.button_done) {
                                    val binding = LayoutDialogInputAttachmentBinding.bind(it.view)

                                    attachment = createAttachment(binding.editText.text.toString(),
                                        Attachment.TYPE_WEBSITE_LINK)
                                    attachment?.name = binding.editText.text.toString()

                                    attachment?.let { item -> viewModel.addAttachment(item) }
                                }
                                onShow {
                                    val webLink = viewModel.fetchRecentItemFromClipboard()

                                    if (webLink.startsWith("https://") ||
                                        webLink.startsWith("http://") ||
                                        webLink.startsWith("www")) {

                                        val binding = LayoutDialogInputAttachmentBinding.bind(it.view)
                                        binding.editText.setText(webLink)
                                    }
                                }
                                negativeButton(R.string.button_cancel)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActionPerformed(t: Attachment, position: Int,action: BaseBasicAdapter.ActionListener.Action) {
        when (action) {
            BaseBasicAdapter.ActionListener.Action.SELECT -> {
                if (t.target != null)
                    onParseForIntent(t.target!!, t.type)
            }
            BaseBasicAdapter.ActionListener.Action.DELETE -> {
                val uri = Uri.parse(t.target)
                val name: String = if (t.type == Attachment.TYPE_CONTENT_URI)
                    t.name ?: uri.getFileName(requireContext())
                    else t.target!!

                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(text = String.format(getString(R.string.dialog_confirm_deletion_title),
                        name))
                    message(R.string.dialog_confirm_deletion_summary)
                    positiveButton(R.string.button_delete) {

                        viewModel.removeAttachment(t)
                        when (t.type) {
                            Attachment.TYPE_CONTENT_URI ->
                                context.contentResolver.delete(uri, null, null)
                            Attachment.TYPE_IMPORTED_FILE ->
                                t.target?.let { File(it) }?.delete()
                        }
                    }
                    negativeButton(R.string.button_cancel)
                }
            }
        }
    }

    // This function invokes the corresponding application that
    // will open the uri of the attachment if the user clicks
    // on the attachment item
    private fun onParseForIntent(target: String, type: Int) {
        when(type) {
            Attachment.TYPE_CONTENT_URI -> {
                val targetUri: Uri = Uri.parse(target)

                val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(targetUri, requireContext().contentResolver?.getType(targetUri))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                if (intent.resolveActivity(context?.packageManager!!) != null)
                    startActivity(intent)
            }
            Attachment.TYPE_IMPORTED_FILE -> {
                val targetUri: Uri = CoreApplication.obtainUriForFile(requireContext(), File(target))

                val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(targetUri, requireContext().contentResolver?.getType(targetUri))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                if (intent.resolveActivity(context?.packageManager!!) != null)
                    startActivity(intent)
            }
            Attachment.TYPE_WEBSITE_LINK -> {
                var targetPath: String = target
                if (!target.startsWith("http://") && !target.startsWith("https://"))
                    targetPath = "http://$targetPath"

                val targetUri: Uri = Uri.parse(targetPath)

                if (PreferenceManager(requireContext()).useExternalBrowser) {
                    val intent = Intent(Intent.ACTION_VIEW, targetUri)

                    if (intent.resolveActivity(context?.packageManager!!) != null)
                        startActivity(intent)
                } else
                    CustomTabsIntent.Builder().build()
                        .launchUrl(requireContext(), targetUri)
            }
        }
    }

    private fun getSharingName(): String? {
        return when (requestKey) {
            REQUEST_KEY_INSERT -> {
                if (viewModel.getName()?.isEmpty() == true
                    || viewModel.getDueDate() == null) {
                    return null
                }

                binding.taskNameTextInput.text.toString()
            }
            REQUEST_KEY_UPDATE -> {
                viewModel.getName() ?: Streamable.ARCHIVE_NAME_GENERIC
            }
            else -> null
        }
    }

    private fun triggerSystemShareSheet(option: Int) {
        val fileName = getSharingName()
        if (fileName == null) {
            MaterialDialog(requireContext()).show {
                title(R.string.feedback_unable_to_share_title)
                message(R.string.feedback_unable_to_share_message)
                positiveButton(R.string.button_done) { dismiss() }
            }
            return
        }

        when(option) {
            R.id.action_export -> {
                val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, fileName)
                    type = Streamable.MIME_TYPE_ZIP
                }

                exportLauncher.launch(exportIntent)
            }
            R.id.action_share -> {
                val serviceIntent = Intent(context, DataExporterService::class.java).apply {
                    action = DataExporterService.ACTION_EXPORT_TASK
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE,
                        viewModel.getTask())
                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS,
                        viewModel.getAttachments())
                }

                context?.startService(serviceIntent)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        with(outState) {
            putParcelable(EXTRA_TASK, viewModel.getTask())
            putParcelable(EXTRA_SUBJECT, viewModel.getSubject())
            putParcelableArrayList(EXTRA_ATTACHMENTS, ArrayList(viewModel.getAttachments()))
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            viewModel.setTask(getParcelable(EXTRA_TASK))
            viewModel.setAttachments(getParcelableArrayList(EXTRA_ATTACHMENTS) ?: arrayListOf())
            viewModel.setSubject(getParcelable(EXTRA_SUBJECT))
        }
    }

    companion object {
        const val REQUEST_KEY_INSERT = "request:insert"
        const val REQUEST_KEY_UPDATE = "request:update"

        const val EXTRA_TASK = "extra:task"
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_ATTACHMENTS = "extra:attachments"
    }

}