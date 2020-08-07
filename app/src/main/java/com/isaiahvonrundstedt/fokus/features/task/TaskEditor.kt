package com.isaiahvonrundstedt.fokus.features.task

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setTransitionName
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PermissionManager
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.components.service.BackupRestoreService
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.attachments.AttachmentAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
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

            setTransitionName(nameTextInput, TaskAdapter.TRANSITION_NAME_ID + task.taskID)
        }

        statusSwitch.changeTextColorWhenChecked()
        prioritySwitch.changeTextColorWhenChecked()

        // The passed extras from the parent activity
        // will be shown in their respective fields.
        if (requestCode == REQUEST_CODE_UPDATE) {
            with(task) {
                nameTextInput.setText(name)
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

        actionButton.setOnClickListener {

            // These if checks if the user have entered the
            // values on the fields, if we don't have the value required,
            // show a snackbar feedback then direct the user's
            // attention to the field. Then return to stop the execution
            // of the code.
            if (nameTextInput.text.isNullOrEmpty()) {
                createSnackbar(rootLayout, R.string.feedback_task_empty_name).show()
                nameTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!task.hasDueDate()) {
                createSnackbar(rootLayout, R.string.feedback_task_empty_due_date).show()
                dueDateTextView.performClick()
                return@setOnClickListener
            }

            task.name = nameTextInput.text.toString()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            REQUEST_CODE_ATTACHMENT -> {
                contentResolver?.takePersistableUriPermission(data?.data!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val attachment = Attachment().apply {
                    task = this@TaskEditor.task.taskID
                    uri = data?.data
                    dateAttached = DateTime.now()
                }

                adapter.insert(attachment)
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
                    adapter.remove(t)
                    createSnackbar(rootLayout, R.string.feedback_attachment_removed).run {
                        setAction(R.string.button_undo) { adapter.insert(t) }
                        show()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if (requestCode == REQUEST_CODE_UPDATE)
            super.onCreateOptionsMenu(menu)
        else false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                MaterialDialog(this).show {
                    title(text = String.format(getString(R.string.dialog_confirm_deletion_title),
                        task.name))
                    message(R.string.dialog_confirm_deletion_summary)
                    positiveButton(R.string.button_delete) {
                        // Send the data back to the parent activity
                        val data = Intent()
                        data.putExtra(EXTRA_TASK, task)
                        data.putExtra(EXTRA_ATTACHMENTS, adapter.itemList)
                        setResult(RESULT_DELETE, data)
                        finish()
                    }
                    negativeButton(R.string.button_cancel)
                }
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

        private const val REQUEST_CODE_ATTACHMENT = 20
    }

}