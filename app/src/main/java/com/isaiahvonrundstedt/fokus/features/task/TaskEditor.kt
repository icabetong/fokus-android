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
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PermissionManager
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.components.service.AttachmentImportService
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
import java.util.*

class TaskEditor : BaseEditor(), BaseAdapter.ActionListener {

    private var requestCode = 0
    private var task = Task()
    private var subject: Subject? = null

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
            }
        }

        statusSwitch.changeTextColorWhenChecked()
        prioritySwitch.changeTextColorWhenChecked()

        // The passed extras from the parent activity
        // will be shown in their respective fields.
        if (requestCode == REQUEST_CODE_UPDATE) {
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
                clearButton.isVisible = true
            }

            window.decorView.rootView.clearFocus()
        }
    }

    override fun onStart() {
        super.onStart()

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
                }
            }
        }

        subjectTextView.setOnClickListener {
            SubjectSelectorSheet(supportFragmentManager).show {
                result { result ->
                    this@TaskEditor.clearButton.isVisible = true
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
                }
            }
        }

        clearButton.setOnClickListener {
            subjectTextView.startAnimation(animation)

            it.isVisible = false
            this.task.subject = null
            with(subjectTextView) {
                removeCompoundDrawableAtStart()
                setText(R.string.field_not_set)
                setTextColorFromResource(R.color.color_secondary_text)
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)
        startService(Intent(this, AttachmentImportService::class.java).apply {
            action = AttachmentImportService.ACTION_CANCEL
        })
        super.onDestroy()
    }

    var snackbar: Snackbar? = null
    private var receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BaseService.ACTION_SERVICE_BROADCAST) {
                when (intent.getStringExtra(BaseService.EXTRA_BROADCAST_STATUS)) {
                    AttachmentImportService.BROADCAST_IMPORT_ONGOING -> {
                        snackbar = createSnackbar(R.string.feedback_import_ongoing, rootLayout,
                            Snackbar.LENGTH_INDEFINITE)
                        snackbar?.show()
                    }
                    AttachmentImportService.BROADCAST_IMPORT_COMPLETED -> {
                        if (snackbar?.isShown == true)
                            snackbar?.dismiss()

                        snackbar = createSnackbar(R.string.feedback_import_completed, rootLayout)
                        snackbar?.show()
                        adapter.insert(createAttachment(
                            intent.getParcelableExtra(BaseService.EXTRA_BROADCAST_DATA)))
                    }
                    AttachmentImportService.BROADCAST_IMPORT_FAILED -> {
                        if (snackbar?.isShown == true)
                            snackbar?.dismiss()

                        snackbar = createSnackbar(R.string.feedback_import_failed, rootLayout)
                        snackbar?.show()
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

    private fun createAttachment(attachmentUri: Uri?): Attachment {
        return Attachment().apply {
            task = this@TaskEditor.task.taskID
            uri = attachmentUri
            dateAttached = DateTime.now()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            REQUEST_CODE_ATTACHMENT -> {
                if (PreferenceManager(this).noImport) {
                    contentResolver?.takePersistableUriPermission(data?.data!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    adapter.insert(createAttachment(data?.data))
                } else
                    startService(Intent(this, AttachmentImportService::class.java).apply {
                            setData(data?.data)
                            action = AttachmentImportService.ACTION_START
                        })
            }
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Attachment) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    onParseIntent(t.uri)
                }
                BaseAdapter.ActionListener.Action.DELETE -> {
                    MaterialDialog(this).show {
                        title(text = String.format(getString(R.string.dialog_confirm_deletion_title),
                            t.uri?.getFileName(this@TaskEditor)))
                        message(R.string.dialog_confirm_deletion_summary)
                        positiveButton(R.string.button_delete) {
                            adapter.remove(t)


                            if (!PreferenceManager(this@TaskEditor).noImport){
                                t.uri?.let { data ->
                                    DocumentFile.fromSingleUri(this@TaskEditor, data)?.delete()
                                }
                            }
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
    private fun onParseIntent(uri: Uri?) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, contentResolver?.getType(uri!!))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (intent.resolveActivity(packageManager!!) != null)
            startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {

                // These if checks if the user have entered the
                // values on the fields, if we don't have the value required,
                // show a snackbar feedback then direct the user's
                // attention to the field. Then return to stop the execution
                // of the code.
                if (taskNameTextInput.text.isNullOrEmpty()) {
                    createSnackbar(R.string.feedback_task_empty_name, rootLayout)
                    taskNameTextInput.requestFocus()
                    return false
                }

                if (!task.hasDueDate()) {
                    createSnackbar(R.string.feedback_task_empty_due_date, rootLayout)
                    dueDateTextView.performClick()
                    return false
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
                supportFinishAfterTransition()
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

    companion object {
        const val REQUEST_CODE_INSERT = 32
        const val REQUEST_CODE_UPDATE = 19

        const val EXTRA_TASK = "extra:task"
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_ATTACHMENTS = "extra:attachments"

        const val TRANSITION_ID_NAME = "transition:task:name:"
        const val TRANSITION_ID_DUE = "transition:task:due:"

        private const val REQUEST_CODE_ATTACHMENT = 20
    }

}