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
import androidx.appcompat.widget.AppCompatTextView
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
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
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
import com.isaiahvonrundstedt.fokus.components.views.TwoLineRadioButton
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.attachments.AttachmentAdapter
import com.isaiahvonrundstedt.fokus.features.attachments.AttachmentOptionSheet
import com.isaiahvonrundstedt.fokus.features.schedule.picker.SchedulePickerSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.picker.SubjectPickerSheet
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_task.*
import kotlinx.android.synthetic.main.layout_item_add.*
import java.io.File
import java.time.ZonedDateTime

class TaskEditor : BaseEditor(), BaseAdapter.ActionListener {

    private val viewModel by lazy {
        ViewModelProvider(this).get(TaskEditorViewModel::class.java)
    }

    private var requestCode = 0
    private var hasFieldChange = false

    private val adapter = AttachmentAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_task)
        setPersistentActionBar(toolbar)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Check if the parent activity has extras sent then
        // determine if the editor will be in insert or
        // update mode
        requestCode = if (intent.hasExtra(EXTRA_TASK) && intent.hasExtra(EXTRA_SUBJECT)
            && intent.hasExtra(EXTRA_ATTACHMENTS)) REQUEST_CODE_UPDATE else REQUEST_CODE_INSERT

        if (requestCode == REQUEST_CODE_UPDATE) {
            viewModel.setTask(intent.getParcelableExtra(EXTRA_TASK))
            viewModel.setAttachments(intent.getParcelableListExtra(EXTRA_ATTACHMENTS))
            viewModel.setSubject(intent.getParcelableExtra(EXTRA_SUBJECT))

            taskNameTextInput.transitionName = TRANSITION_ID_NAME + viewModel.getTask()?.taskID
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

        viewModel.task.observe(this) {
            if (requestCode == REQUEST_CODE_UPDATE && it != null) {
                with(it) {
                    taskNameTextInput.setText(name)
                    notesTextInput.setText(notes)
                    prioritySwitch.isChecked = isImportant
                    statusSwitch.isChecked = isFinished
                    dueDateTextView.text = formatDueDate(this@TaskEditor)
                    dueDateTextView.setTextColorFromResource(R.color.color_primary_text)
                }
            }
        }

        viewModel.attachments.observe(this) {
            adapter.submitList(it)
        }

        viewModel.subject.observe(this) {
            removeButton.isVisible = it != null
            dueDateTextView.isVisible = it == null
            dateTimeRadioGroup.isVisible = it != null

            if (it != null) {
                with(subjectTextView) {
                    text = it.code
                    setTextColorFromResource(R.color.color_primary_text)
                    setCompoundDrawableAtStart(ContextCompat.getDrawable(this@TaskEditor,
                        R.drawable.shape_color_holder)?.let { shape -> it.tintDrawable(shape) })
                }

                if (viewModel.getTask()?.dueDate != null) {
                    with(customDateTimeRadio) {
                        isChecked = true
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getTask()?.formatDueDate(context) ?: ""
                    }
                }
            } else {
                with(subjectTextView) {
                    removeCompoundDrawableAtStart()
                    setText(R.string.field_not_set)
                    setTextColorFromResource(R.color.color_secondary_text)
                }

                if (viewModel.getTask()?.dueDate != null) {
                    with(dueDateTextView) {
                        text = viewModel.getTask()?.formatDueDate(context)
                        setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        taskNameTextInput.addTextChangedListener {
            viewModel.getTask()?.name = it.toString()
            hasFieldChange = true
        }
        notesTextInput.addTextChangedListener {
            viewModel.getTask()?.notes = it.toString()
            hasFieldChange = true
        }

        addItemButton.setOnClickListener { _ ->
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
                            var attachment: Attachment? = null

                            MaterialDialog(this@TaskEditor).show {
                                title(res = R.string.dialog_enter_website_url)
                                input { _, charSequence ->
                                    attachment = createAttachment(charSequence.toString(),
                                        Attachment.TYPE_WEBSITE_LINK)
                                    attachment?.name = charSequence.toString()
                                }
                                positiveButton(R.string.button_done) {
                                    attachment?.let { item -> viewModel.addAttachment(item) }
                                }
                                negativeButton(R.string.button_cancel)
                            }
                        }
                    }
                    this.dismiss()
                }
            }
        }

        dueDateTextView.setOnClickListener { v ->
            MaterialDialog(this).show {
                lifecycleOwner(this@TaskEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getTaskDueDate()?.toCalendar()) { _, datetime ->
                    viewModel.getTask()?.dueDate = datetime.toZonedDateTime()
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) {
                        v.text = viewModel.getTask()?.formatDueDate(this@TaskEditor)
                        v.setTextColorFromResource(R.color.color_primary_text)
                    }
                    hasFieldChange = true
                }
            }
        }

        subjectTextView.setOnClickListener {
            SubjectPickerSheet(supportFragmentManager).show {
                waitForResult { result ->
                    with(this@TaskEditor) {
                        removeButton.isVisible = true
                        viewModel.setSubject(result.subject)
                        viewModel.setSchedules(result.schedules)
                    }
                    hasFieldChange = true
                }
            }
        }

        removeButton.setOnClickListener {
            hasFieldChange = true
            subjectTextView.startAnimation(animation)

            it.visibility = View.INVISIBLE
            this.viewModel.getTask()?.subject = null
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
            viewModel.setNextMeetingForDueDate()
            hasFieldChange = true

            with(inNextMeetingRadio) {
                titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                subtitle = viewModel.getTask()?.formatDueDate(context) ?: ""
            }
        }

        pickDateTimeRadio.setOnClickListener {
            SchedulePickerSheet(viewModel.getSchedules(), supportFragmentManager).show {
                waitForResult { schedule ->
                    viewModel.setClassScheduleAsDueDate(schedule)
                    with(this@TaskEditor.pickDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context, R.color.color_primary_text)
                        subtitle = viewModel.getTask()?.formatDueDate(context) ?: ""
                    }

                    hasFieldChange = true
                    this.dismiss()
                }

            }
        }

        customDateTimeRadio.setOnClickListener {
            MaterialDialog(this).show {
                lifecycleOwner(this@TaskEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = viewModel.getTaskDueDate()?.toCalendar()) { _, datetime ->
                    viewModel.getTask()?.dueDate = datetime.toZonedDateTime()
                }
                positiveButton(R.string.button_done) {
                    hasFieldChange = true

                    with(this@TaskEditor.customDateTimeRadio) {
                        titleTextColor = ContextCompat.getColor(context,
                            R.color.color_primary_text)
                        subtitle = viewModel.getTask()?.formatDueDate(context) ?: ""
                    }
                }
                negativeButton { this@TaskEditor.customDateTimeRadio.isChecked = false }
            }
        }

        actionButton.setOnClickListener {

            // These if checks if the user have entered the
            // values on the fields, if we don't have the value required,
            // show a snackbar feedback then direct the user's
            // attention to the field. Then return to stop the execution
            // of the code.
            if (!viewModel.hasTaskName) {
                createSnackbar(R.string.feedback_task_empty_name, rootLayout)
                taskNameTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!viewModel.hasDueDate) {
                createSnackbar(R.string.feedback_task_empty_due_date, rootLayout)
                dueDateTextView.performClick()
                return@setOnClickListener
            }

            viewModel.getTask()?.isImportant = prioritySwitch.isChecked
            viewModel.getTask()?.isFinished = statusSwitch.isChecked

            // Send the data back to the parent activity
            setResult(RESULT_OK, Intent().apply {
                putExtra(EXTRA_TASK, viewModel.getTask())
                putExtra(EXTRA_ATTACHMENTS, viewModel.getAttachments())
            })

            if (requestCode == REQUEST_CODE_UPDATE)
                supportFinishAfterTransition()
            else finish()
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)
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
                        createSnackbar(R.string.feedback_import_ongoing, rootLayout,
                            Snackbar.LENGTH_INDEFINITE)
                    }
                    FileImporterService.BROADCAST_IMPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_import_completed, rootLayout)

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
                        createSnackbar(R.string.feedback_import_failed, rootLayout)
                    }
                    DataExporterService.BROADCAST_EXPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_export_ongoing, rootLayout)
                    }
                    DataExporterService.BROADCAST_EXPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_export_completed, rootLayout)

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
                        createSnackbar(R.string.feedback_export_failed, rootLayout)
                    }
                    DataImporterService.BROADCAST_IMPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_import_ongoing, rootLayout)
                    }
                    DataImporterService.BROADCAST_IMPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_import_completed, rootLayout)

                        val data: TaskPackage? = intent.getParcelableExtra(BaseService.EXTRA_BROADCAST_DATA)

                        viewModel.setTask(data?.task)
                        viewModel.setAttachments(data?.attachments)

                    }
                    DataImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, rootLayout)
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
        hasFieldChange = true
        return Attachment().apply {
            task = viewModel.getTask()?.taskID!!
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
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.getTask())
                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, viewModel.getAttachments())
                })
            }
            REQUEST_CODE_IMPORT -> {
                startService(Intent(this, DataImporterService::class.java).apply {
                    this.data = data?.data
                    action = DataImporterService.ACTION_IMPORT_TASK
                })
            }
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is Attachment) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    if (t.target != null)
                        onParseForIntent(t.target!!, t.type)
                }
                BaseAdapter.ActionListener.Action.DELETE -> {
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
                            hasFieldChange = true
                        }
                        negativeButton(R.string.button_cancel)
                    }
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
                        if (viewModel.hasTaskName || viewModel.hasDueDate) {
                            MaterialDialog(this@TaskEditor).show {
                                title(R.string.feedback_unable_to_share_title)
                                message(R.string.feedback_unable_to_share_message)
                                positiveButton(R.string.button_done) { dismiss() }
                            }

                            return false
                        }

                        fileName = taskNameTextInput.text.toString()
                    }
                    REQUEST_CODE_UPDATE -> {
                        fileName = viewModel.getTask()?.name ?: Streamable.ARCHIVE_NAME_GENERIC
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
                                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.getTask())
                                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, viewModel.getAttachments())
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
            putParcelable(EXTRA_TASK, viewModel.getTask())
            putParcelable(EXTRA_SUBJECT, viewModel.getSubject())
            putParcelableArrayList(EXTRA_ATTACHMENTS, viewModel.getAttachments().toArrayList())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        with(savedInstanceState) {
            viewModel.setTask(getParcelable(EXTRA_TASK))
            viewModel.setSubject(getParcelable(EXTRA_SUBJECT))
            viewModel.setAttachments(getParcelableArrayList(EXTRA_ATTACHMENTS))
        }
    }

    override fun onBackPressed() {
        if (hasFieldChange) {
            MaterialDialog(this).show {
                title(R.string.dialog_discard_changes)
                positiveButton(R.string.button_discard) {
                    viewModel.getAttachments().forEach { attachment ->
                        if (attachment.type == Attachment.TYPE_IMPORTED_FILE)
                            attachment.target?.also { File(it).delete() }
                    }
                    super.onBackPressed()
                }
                negativeButton(R.string.button_cancel)
            }
        } else super.onBackPressed()
    }

    companion object {
        const val REQUEST_CODE_INSERT = 32
        const val REQUEST_CODE_UPDATE = 19

        const val EXTRA_TASK = "extra:task"
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_ATTACHMENTS = "extra:attachments"

        const val TRANSITION_ID_NAME = "transition:task:name:"

        private const val REQUEST_CODE_ATTACHMENT = 20
        private const val REQUEST_CODE_EXPORT = 49
        private const val REQUEST_CODE_IMPORT = 68
    }

}