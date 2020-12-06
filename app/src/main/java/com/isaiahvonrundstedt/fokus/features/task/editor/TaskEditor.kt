package com.isaiahvonrundstedt.fokus.features.task.editor

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.ShareOptionsBottomSheet
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toArrayList
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
import com.isaiahvonrundstedt.fokus.databinding.ActivityEditorTaskBinding
import com.isaiahvonrundstedt.fokus.databinding.LayoutDialogInputAttachmentBinding
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.attachments.AttachmentAdapter
import com.isaiahvonrundstedt.fokus.features.attachments.AttachmentOptionSheet
import com.isaiahvonrundstedt.fokus.features.schedule.picker.SchedulePickerSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBasicAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import com.isaiahvonrundstedt.fokus.features.subject.picker.SubjectPickerActivity
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.ZonedDateTime

@AndroidEntryPoint
class TaskEditor : BaseEditor(), BaseBasicAdapter.ActionListener<Attachment> {
    private lateinit var binding: ActivityEditorTaskBinding

    private var requestCode = 0

    private val attachmentAdapter = AttachmentAdapter(this)
    private val viewModel: TaskEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBarLayout.toolbar)

        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        // Check if the parent activity has extras sent then
        // determine if the editor will be in insert or
        // update mode
        requestCode = if (intent.hasExtra(EXTRA_TASK) && intent.hasExtra(EXTRA_SUBJECT)
            && intent.hasExtra(EXTRA_ATTACHMENTS)) REQUEST_CODE_UPDATE else REQUEST_CODE_INSERT

        if (requestCode == REQUEST_CODE_UPDATE) {
            viewModel.task = intent.getParcelableExtra(EXTRA_TASK)
            viewModel.attachments = intent.getParcelableArrayListExtra(EXTRA_ATTACHMENTS) ?: arrayListOf()
            viewModel.subject = intent.getParcelableExtra(EXTRA_SUBJECT)

            binding.root.transitionName = TRANSITION_ELEMENT_ROOT + viewModel.task?.taskID

            window.sharedElementEnterTransition = buildContainerTransform(binding.root)
            window.sharedElementReturnTransition = buildContainerTransform(binding.root,
                TRANSITION_SHORT_DURATION)
        } else {
            binding.root.transitionName = TRANSITION_ELEMENT_ROOT

            window.sharedElementEnterTransition = buildContainerTransform(binding.root,
                withMotion = true)
            window.sharedElementReturnTransition = buildContainerTransform(binding.root,
                TRANSITION_SHORT_DURATION, true)
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

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
    }

    private val dialogView: View by lazy {
        LayoutDialogInputAttachmentBinding.inflate(layoutInflater).root
    }

    override fun onStart() {
        super.onStart()

        viewModel.taskObservable.observe(this) {
            if (requestCode == REQUEST_CODE_UPDATE && it != null) {
                with(it) {
                    binding.taskNameTextInput.setText(name)
                    binding.notesTextInput.setText(notes)
                    binding.prioritySwitch.isChecked = isImportant
                    binding.statusSwitch.isChecked = isFinished

                    if (it.hasDueDate()) {
                        binding.dueDateTextView.text = formatDueDate(this@TaskEditor)
                        binding.dueDateTextView.setTextColorFromResource(R.color.color_primary_text)
                    } else {
                        binding.dueDateTextView.setText(R.string.field_not_set)
                        binding.dueDateTextView.setTextColorFromResource(R.color.color_secondary_text)
                    }
                }
            }
        }

        viewModel.attachmentObservable.observe(this) {
            attachmentAdapter.submitList(it)
        }

        viewModel.subjectObservable.observe(this) {
            binding.removeButton.isVisible = it != null
            binding.dateTimeRadioGroup.isVisible = it != null
            binding.dueDateTextView.isVisible = it == null
            binding.removeDueDateButton.isVisible = it == null && viewModel.hasDueDate()

            if (it != null) {
                with(binding.subjectTextView) {
                    text = it.code
                    setTextColorFromResource(R.color.color_primary_text)
                    setCompoundDrawableAtStart(ContextCompat.getDrawable(context,
                        R.drawable.shape_color_holder)?.let { shape -> it.tintDrawable(shape) })
                }

                if (viewModel.hasDueDate()) {
                    with(binding.customDateTimeRadio) {
                        isChecked = true
                        titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                        subtitle = viewModel.getFormattedDueDate()
                    }
                }
            } else {
                with(binding.subjectTextView) {
                    setText(R.string.field_not_set)
                    setTextColorFromResource(R.color.color_secondary_text)
                    removeCompoundDrawableAtStart()
                }

                if (viewModel.hasDueDate()) {
                    with(binding.dueDateTextView) {
                        text = viewModel.getFormattedDueDate()
                        setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.taskNameTextInput.addTextChangedListener {
            viewModel.setTaskName(it.toString())
        }
        binding.notesTextInput.addTextChangedListener {
            viewModel.setNotes(it.toString())
        }

        binding.addActionLayout.addItemButton.setOnClickListener { _ ->
            AttachmentOptionSheet(supportFragmentManager).show {
                waitForResult { id ->
                    when (id) {
                        R.id.action_import_file -> {
                            // Check if we have read storage permissions then request the permission
                            // if we have the permission, open up file picker
                            if (PermissionManager(this@TaskEditor).readStorageGranted) {
                                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                                    .setType("*/*"), REQUEST_CODE_ATTACHMENT)
                            } else
                                PermissionManager.requestReadStoragePermission(this@TaskEditor)
                        }
                        R.id.action_website_url -> {
                            var attachment: Attachment?

                            MaterialDialog(this@TaskEditor).show {
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

                                    if (webLink != null && (webLink.startsWith("https://") ||
                                            webLink.startsWith("http://") ||
                                                webLink.startsWith("www"))) {

                                        val binding = LayoutDialogInputAttachmentBinding.bind(it.view)
                                        binding.editText.setText(webLink)
                                    }
                                }
                                negativeButton(R.string.button_cancel)
                            }
                        }
                    }
                    this.dismiss()
                }
            }
        }

        binding.dueDateTextView.setOnClickListener {
            MaterialDialog(this).show {
                lifecycleOwner(this@TaskEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getDueDate()?.toCalendar()) { _, datetime ->
                    viewModel.setDueDate(datetime.toZonedDateTime())
                    binding.removeDueDateButton.isVisible = true
                }
                positiveButton(R.string.button_done) {
                    with(binding.dueDateTextView) {
                        text = viewModel.getFormattedDueDate()
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
            startActivityForResult(Intent(this, SubjectPickerActivity::class.java),
                SubjectPickerActivity.REQUEST_CODE_PICK)
        }

        binding.removeButton.setOnClickListener {
            binding.dueDateTextView.setText(R.string.field_not_set)
            binding.dueDateTextView.setTextColor(ContextCompat.getColor(it.context,
                R.color.color_secondary_text))
            binding.subjectTextView.startAnimation(animation)

            it.isVisible = false
            viewModel.subject = null
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
                subtitle = viewModel.getFormattedDueDate()
            }
        }

        binding.pickDateTimeRadio.setOnClickListener {
            SchedulePickerSheet(viewModel.schedules, supportFragmentManager).show {
                waitForResult { schedule ->
                    viewModel.setClassScheduleAsDueDate(schedule)
                    with(binding.pickDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                        subtitle = viewModel.getFormattedDueDate()
                    }
                    this.dismiss()
                }
            }
        }

        binding.customDateTimeRadio.setOnClickListener {
            MaterialDialog(this).show {
                lifecycleOwner(this@TaskEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getDueDate()?.toCalendar()) { _, datetime ->
                    viewModel.setDueDate(datetime.toZonedDateTime())
                }
                positiveButton(R.string.button_done) {
                    with(binding.customDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getFormattedDueDate()
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
            if (!viewModel.hasTaskName()) {
                createSnackbar(R.string.feedback_task_empty_name, binding.root)
                binding.taskNameTextInput.requestFocus()
                return@setOnClickListener
            }

            viewModel.setIsImportant(binding.prioritySwitch.isChecked)
            viewModel.setIsFinished(binding.statusSwitch.isChecked)

            // Send the data back to the parent activity
            setResult(RESULT_OK, Intent().apply {
                putExtra(EXTRA_TASK, viewModel.task)
                putExtra(EXTRA_ATTACHMENTS, viewModel.attachments)
            })

            if (requestCode == REQUEST_CODE_UPDATE)
                supportFinishAfterTransition()
            else finish()
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)

        // Cancel all current import processes
        startService(Intent(this, FileImporterService::class.java).apply {
            action = FileImporterService.ACTION_CANCEL
        })
        super.onDestroy()
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
                            val uri = CoreApplication.obtainUriForFile(this@TaskEditor, File(it))

                            startActivity(ShareCompat.IntentBuilder.from(this@TaskEditor)
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

                        val data: TaskPackage? = intent.getParcelableExtra(BaseService.EXTRA_BROADCAST_DATA)

                        viewModel.task = data?.task
                        viewModel.attachments = data?.attachments?.toArrayList() ?: arrayListOf()

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
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*"), REQUEST_CODE_ATTACHMENT)
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun createAttachment(targetPath: String, attachmentType: Int): Attachment {
        return Attachment().apply {
            task = viewModel.task?.taskID!!
            target = targetPath
            dateAttached = ZonedDateTime.now()
            type = attachmentType
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            REQUEST_CODE_ATTACHMENT -> {
                val service = Intent(this, FileImporterService::class.java).apply {
                    action = FileImporterService.ACTION_START
                    setData(data?.data)
                    putExtra(FileImporterService.EXTRA_DIRECTORY,
                        Streamable.DIRECTORY_ATTACHMENTS)
                }

                startService(service)
            }
            REQUEST_CODE_EXPORT -> {
                startService(Intent(this, DataExporterService::class.java).apply {
                    this.data = data?.data
                    action = DataExporterService.ACTION_EXPORT_TASK
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.task)
                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, viewModel.attachments)
                })
            }
            REQUEST_CODE_IMPORT -> {
                startService(Intent(this, DataImporterService::class.java).apply {
                    this.data = data?.data
                    action = DataImporterService.ACTION_IMPORT_TASK
                })
            }
            SubjectPickerActivity.REQUEST_CODE_PICK ->
                data?.getParcelableExtra<SubjectPackage>(SubjectPickerActivity.EXTRA_SELECTED_SUBJECT)
                    ?.also {
                        viewModel.subject = it.subject
                        viewModel.schedules = it.schedules
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
                    t.name ?: uri.getFileName(this)
                    else t.target!!

                MaterialDialog(this).show {
                    title(text = String.format(getString(R.string.dialog_confirm_deletion_title),
                        name))
                    message(R.string.dialog_confirm_deletion_summary)
                    positiveButton(R.string.button_delete) {

                        viewModel.removeAttachment(t)
                        when (t.type) {
                            Attachment.TYPE_CONTENT_URI ->
                                contentResolver.delete(uri, null, null)
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
                    .setDataAndType(targetUri, contentResolver?.getType(targetUri))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                if (intent.resolveActivity(packageManager!!) != null)
                    startActivity(intent)
            }
            Attachment.TYPE_IMPORTED_FILE -> {
                val targetUri: Uri = CoreApplication.obtainUriForFile(this, File(target))

                val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(targetUri, contentResolver?.getType(targetUri))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                if (intent.resolveActivity(packageManager!!) != null)
                    startActivity(intent)
            }
            Attachment.TYPE_WEBSITE_LINK -> {
                var targetPath: String = target
                if (!target.startsWith("http://") && !target.startsWith("https://"))
                    targetPath = "http://$targetPath"

                val targetUri: Uri = Uri.parse(targetPath)

                if (PreferenceManager(this).useExternalBrowser) {
                    val intent = Intent(Intent.ACTION_VIEW, targetUri)

                    if (intent.resolveActivity(packageManager!!) != null)
                        startActivity(intent)
                } else
                    CustomTabsIntent.Builder().build()
                        .launchUrl(this, targetUri)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share_options -> {

                var fileName: String = Streamable.ARCHIVE_NAME_GENERIC
                when (requestCode) {
                    REQUEST_CODE_INSERT -> {
                        if (viewModel.hasTaskName() || viewModel.hasDueDate()) {
                            MaterialDialog(this@TaskEditor).show {
                                title(R.string.feedback_unable_to_share_title)
                                message(R.string.feedback_unable_to_share_message)
                                positiveButton(R.string.button_done) { dismiss() }
                            }

                            return false
                        }

                        fileName = binding.taskNameTextInput.text.toString()
                    }
                    REQUEST_CODE_UPDATE -> {
                        fileName = viewModel.getTaskName() ?: Streamable.ARCHIVE_NAME_GENERIC
                    }
                }

                ShareOptionsBottomSheet(supportFragmentManager).show {
                    waitForResult { id ->
                        when (id) {
                            R.id.action_export -> {
                                val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    putExtra(Intent.EXTRA_TITLE, fileName)
                                    type = Streamable.MIME_TYPE_ZIP
                                }

                                this@TaskEditor.startActivityForResult(exportIntent, REQUEST_CODE_EXPORT)
                            }
                            R.id.action_share -> {
                                val serviceIntent = Intent(this@TaskEditor, DataExporterService::class.java).apply {
                                    action = DataExporterService.ACTION_EXPORT_TASK
                                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE,
                                        viewModel.task)
                                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS,
                                        viewModel.attachments)
                                }

                                this@TaskEditor.startService(serviceIntent)
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
            putParcelable(EXTRA_TASK, viewModel.task)
            putParcelable(EXTRA_SUBJECT, viewModel.subject)
            putParcelableArrayList(EXTRA_ATTACHMENTS, viewModel.attachments)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        with(savedInstanceState) {
            viewModel.task = getParcelable(EXTRA_TASK)
            viewModel.attachments = getParcelableArrayList(EXTRA_ATTACHMENTS) ?: arrayListOf()
            viewModel.subject = getParcelable(EXTRA_SUBJECT)
        }
    }

    companion object {
        const val REQUEST_CODE_INSERT = 32
        const val REQUEST_CODE_UPDATE = 19

        const val EXTRA_TASK = "extra:task"
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_ATTACHMENTS = "extra:attachments"

        private const val REQUEST_CODE_ATTACHMENT = 20
        private const val REQUEST_CODE_EXPORT = 49
        private const val REQUEST_CODE_IMPORT = 68
    }

}