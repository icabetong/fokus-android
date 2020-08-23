package com.isaiahvonrundstedt.fokus.features.task

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
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.ShareOptionsBottomSheet
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.DataExporterService
import com.isaiahvonrundstedt.fokus.components.service.DataImporterService
import com.isaiahvonrundstedt.fokus.components.service.FileImporterService
import com.isaiahvonrundstedt.fokus.components.utils.PermissionManager
import com.isaiahvonrundstedt.fokus.database.converter.DateTimeConverter
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.attachments.AttachmentAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.selector.SubjectSelectorSheet
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_task.*
import kotlinx.android.synthetic.main.layout_item_add.*
import org.joda.time.DateTime
import org.joda.time.LocalDateTime.fromCalendarFields
import java.io.File
import java.util.*

class TaskEditor : BaseEditor(), BaseAdapter.ActionListener {

    private var requestCode = 0
    private var task = Task()
    private var subject: Subject? = null
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
            task = intent.getParcelableExtra(EXTRA_TASK)!!
            subject = intent.getParcelableExtra(EXTRA_SUBJECT)
            adapter.setItems(intent.getParcelableListExtra(EXTRA_ATTACHMENTS) ?: emptyList())

            taskNameTextInput.transitionName = TRANSITION_ID_NAME + task.taskID
        } else if (intent.hasExtra(TaskEditorSheet.EXTRA_TASK_TITLE)) {

            task.name = intent.getStringExtra(TaskEditorSheet.EXTRA_TASK_TITLE)

            taskNameTextInput.setText(task.name)
            taskNameTextInput.requestFocus()
            taskNameTextInput.transitionName = TRANSITION_ID_NAME

            if (intent.hasExtra(TaskEditorSheet.EXTRA_TASK_DUE)) {
                task.dueDate = DateTimeConverter.toDateTime(
                    intent.getStringExtra(TaskEditorSheet.EXTRA_TASK_DUE))
                dueDateTextView.text = task.formatDueDate(this)
                dueDateTextView.transitionName = TRANSITION_ID_DUE
                dueDateTextView.setTextColorFromResource(R.color.color_primary_text)
            }
        }

        statusSwitch.changeTextColorWhenChecked()
        prioritySwitch.changeTextColorWhenChecked()

        // The passed extras from the parent activity
        // will be shown in their respective fields.
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

        taskNameTextInput.addTextChangedListener { hasFieldChange = true }
        notesTextInput.addTextChangedListener { hasFieldChange = true }

        addItemButton.setOnClickListener {
            // Check if we have read storage permissions then request the permission
            // if we have the permission, open up file picker
            if (PermissionManager(this).readStorageGranted) {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .setType("*/*"), REQUEST_CODE_ATTACHMENT)
            } else
                PermissionManager.requestReadStoragePermission(this)
        }

        dueDateTextView.setOnClickListener { v ->
            MaterialDialog(this).show {
                lifecycleOwner(this@TaskEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = task.dueDate?.toCalendar(Locale.getDefault())) { _, datetime ->
                    task.dueDate = fromCalendarFields(datetime).toDateTime()
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) {
                        v.text = task.formatDueDate(this@TaskEditor)
                        v.setTextColorFromResource(R.color.color_primary_text)
                    }
                    hasFieldChange = true
                }
            }
        }

        subjectTextView.setOnClickListener {
            SubjectSelectorSheet(supportFragmentManager).show {
                waitForResult { result ->
                    this@TaskEditor.removeButton.isVisible = true
                    task.subject = result.subjectID
                    subject = result

                    with(this@TaskEditor.subjectTextView) {
                        text = result.code
                        setTextColorFromResource(R.color.color_primary_text)
                        ContextCompat.getDrawable(this.context, R.drawable.shape_color_holder)?.let {
                            this.setCompoundDrawableAtStart(it)
                        }
                    }
                    ContextCompat.getDrawable(this@TaskEditor, R.drawable.shape_color_holder)?.let {
                        this@TaskEditor.subjectTextView
                            .setCompoundDrawableAtStart(result.tintDrawable(it))
                    }
                    hasFieldChange = true
                }
            }
        }

        removeButton.setOnClickListener {
            hasFieldChange = true
            subjectTextView.startAnimation(animation)

            it.visibility = View.INVISIBLE
            this.task.subject = null
            with(subjectTextView) {
                removeCompoundDrawableAtStart()
                setText(R.string.field_not_set)
                setTextColorFromResource(R.color.color_secondary_text)
            }
        }

        actionButton.setOnClickListener {

            // These if checks if the user have entered the
            // values on the fields, if we don't have the value required,
            // show a snackbar feedback then direct the user's
            // attention to the field. Then return to stop the execution
            // of the code.
            if (taskNameTextInput.text.isNullOrEmpty()) {
                createSnackbar(R.string.feedback_task_empty_name, rootLayout)
                taskNameTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!task.hasDueDate()) {
                createSnackbar(R.string.feedback_task_empty_due_date, rootLayout)
                dueDateTextView.performClick()
                return@setOnClickListener
            }

            task.name = taskNameTextInput.text.toString()
            task.notes = notesTextInput.text.toString()
            task.isImportant = prioritySwitch.isChecked
            task.isFinished = statusSwitch.isChecked

            // Send the data back to the parent activity
            val data = Intent()
            data.putExtra(EXTRA_TASK, task)
            data.putExtra(EXTRA_ATTACHMENTS, adapter.itemList)
            setResult(RESULT_OK, data)
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
                            createAttachment(it)
                        }
                        adapter.insert(attachment)
                    }
                    FileImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, rootLayout)
                    }
                    DataExporterService.BROADCAST_EXPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_export_ongoing, rootLayout)
                    }
                    DataExporterService.BROADCAST_EXPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_export_completed, rootLayout)

                        if (intent.hasExtra(BaseService.EXTRA_BROADCAST_DATA)) {
                            android.util.Log.e("DEBUG", "has extra")
                            val path = intent.getStringExtra(BaseService.EXTRA_BROADCAST_DATA)

                            val uri = FileProvider.getUriForFile(this@TaskEditor,
                                CoreApplication.PROVIDER_AUTHORITY, File(path!!))

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

                        intent.getParcelableExtra<TaskPackage>(BaseService.EXTRA_BROADCAST_DATA)?.also {
                            this@TaskEditor.task = it.task
                            adapter.setItems(it.attachments)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == PermissionManager.STORAGE_READ_REQUEST_CODE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*"), REQUEST_CODE_ATTACHMENT)
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun createAttachment(targetPath: String): Attachment {
        hasFieldChange = true
        return Attachment().apply {
            task = this@TaskEditor.task.taskID
            target = targetPath
            dateAttached = DateTime.now()
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
                        FileImporterService.DIRECTORY_ATTACHMENTS)
                }

                startService(service)
            }
            REQUEST_CODE_EXPORT -> {
                startService(Intent(this, DataExporterService::class.java).apply {
                    this.data = data?.data
                    action = DataExporterService.ACTION_EXPORT_TASK
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, task)
                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, adapter.itemList)
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

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Attachment) {
            val attachment = t.target?.let { File(it) }

            when (action) {
                BaseAdapter.ActionListener.Action.DELETE -> {
                    MaterialDialog(this).show {
                        title(text = String.format(getString(R.string.dialog_confirm_deletion_title),
                            attachment?.name))
                        message(R.string.dialog_confirm_deletion_summary)
                        positiveButton(R.string.button_delete) {
                            adapter.remove(t)
                            attachment?.delete()
                            hasFieldChange = true
                        }
                        negativeButton(R.string.button_cancel)
                    }
                }
                BaseAdapter.ActionListener.Action.SELECT -> {
                    if (attachment != null) {
                        onParseIntent(FileProvider.getUriForFile(this,
                            CoreApplication.PROVIDER_AUTHORITY, attachment))
                    }
                }
            }
        }
    }

    // This function invokes the corresponding application that
    // will open the uri of the attachment if the user clicks
    // on the attachment item
    private fun onParseIntent(uri: Uri?) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, contentResolver?.getType(uri!!))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (intent.resolveActivity(packageManager!!) != null)
            startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share_options -> {
                if (requestCode == REQUEST_CODE_INSERT && !taskNameTextInput.text.isNullOrEmpty()
                    && task.hasDueDate()) {
                    MaterialDialog(this@TaskEditor).show {
                        title(R.string.feedback_unable_to_share_title)
                        message(R.string.feedback_unable_to_share_message)
                        positiveButton(R.string.button_done) { dismiss() }
                    }
                    return false
                }

                var fileName: String = Streamable.ARCHIVE_NAME_GENERIC
                when (requestCode) {
                    REQUEST_CODE_INSERT -> fileName = taskNameTextInput.text.toString()
                    REQUEST_CODE_UPDATE -> fileName = task.name ?: Streamable.ARCHIVE_NAME_GENERIC
                }

                ShareOptionsBottomSheet(supportFragmentManager).show {
                    waitForResult { id ->
                        when (id) {
                            R.id.action_export -> {
                                startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                    putExtra(Intent.EXTRA_TITLE, fileName)
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = Streamable.MIME_TYPE_ZIP
                                }, REQUEST_CODE_EXPORT)
                            }
                            R.id.action_share -> {
                                startService(Intent(context, DataExporterService::class.java).apply {
                                    action = DataExporterService.ACTION_EXPORT_TASK
                                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, task)
                                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, adapter.itemList)
                                })
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
            putParcelable(EXTRA_TASK, task)
            putParcelable(EXTRA_SUBJECT, subject)
            putParcelableArrayList(EXTRA_ATTACHMENTS, adapter.itemList.toArrayList())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        with(savedInstanceState) {
            getParcelable<Task>(EXTRA_TASK)?.let {
                this@TaskEditor.task = it
            }
            getParcelable<Subject>(EXTRA_SUBJECT)?.let {
                this@TaskEditor.subject = it
            }
            getParcelableArrayList<Attachment>(EXTRA_ATTACHMENTS)?.let {
                this@TaskEditor.adapter.setItems(it)
            }
        }
    }

    override fun onBackPressed() {
        if (hasFieldChange) {
            MaterialDialog(this).show {
                title(R.string.dialog_discard_changes)
                positiveButton(R.string.button_discard) {
                    adapter.itemList.forEach { attachment ->
                        attachment.target?.also { File(it).delete() }
                    }
                    super.onBackPressed()
                }
                negativeButton(R.string.button_cancel)
            }
        } else super.onBackPressed()
    }

    override fun onValueChanged() {
        with(task) {
            taskNameTextInput.setText(name)
            notesTextInput.setText(notes)
            prioritySwitch.isChecked = isImportant
            statusSwitch.isChecked = isFinished
            dueDateTextView.text = formatDueDate(this@TaskEditor)
            dueDateTextView.setTextColorFromResource(R.color.color_primary_text)
        }

        subject?.let {
            with(subjectTextView) {
                text = it.code
                setTextColorFromResource(R.color.color_primary_text)
                setCompoundDrawableAtStart(ContextCompat.getDrawable(this@TaskEditor,
                    R.drawable.shape_color_holder)?.let { drawable -> it.tintDrawable(drawable) })
            }
            removeButton.isVisible = true
        }

        window.decorView.rootView.clearFocus()
    }

    companion object {
        const val REQUEST_CODE_INSERT = 32
        const val REQUEST_CODE_UPDATE = 19

        const val EXTRA_TASK = "extra:task"
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_ATTACHMENTS = "extra:attachments"

        const val TRANSITION_ID_NAME = "transition:task:name:"
        const val TRANSITION_ID_DUE = "transition:task:due:"

        private const val REQUEST_CODE_ATTACHMENT = 20
        private const val REQUEST_CODE_EXPORT = 49
        private const val REQUEST_CODE_IMPORT = 68
    }

}