package com.isaiahvonrundstedt.fokus.features.task

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setTransitionName
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.extensions.getUsingID
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.components.PermissionManager
import com.isaiahvonrundstedt.fokus.features.attachments.AttachmentAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectEditor
import com.isaiahvonrundstedt.fokus.features.subject.selector.SubjectSelectorActivity
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_subject.*
import kotlinx.android.synthetic.main.layout_editor_task.*
import kotlinx.android.synthetic.main.layout_editor_task.actionButton
import kotlinx.android.synthetic.main.layout_editor_task.recyclerView
import kotlinx.android.synthetic.main.layout_editor_task.rootLayout
import kotlinx.android.synthetic.main.layout_item_add.*
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class TaskEditor: BaseEditor(), BaseAdapter.ActionListener {

    private var requestCode = 0
    private var task = Task()
    private var subject: Subject? = null

    private val attachmentRequestCode = 32
    private val attachmentList = ArrayList<Attachment>()
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

            setTransitionName(nameEditText, TaskAdapter.TRANSITION_NAME_ID + task.taskID)
        }

        statusSwitch.changeTextColorWhenChecked()
        prioritySwitch.changeTextColorWhenChecked()

        // The passed extras from the parent activity
        // will be shown in their respective fields.
        if (requestCode == REQUEST_CODE_UPDATE) {
            with(task) {
                nameEditText.setText(name)
                notesEditText.setText(notes)
                dueDateTextView.text = formatDueDate(this@TaskEditor)
                dueDateTextView.setTextColorFromResource(R.color.colorPrimaryText)
                prioritySwitch.isChecked = isImportant
                statusSwitch.isChecked = isFinished
            }

            subject?.let {
                with(subjectTextView) {
                    text = it.code
                    setTextColorFromResource(R.color.colorPrimaryText)
                    setCompoundDrawableAtStart(ContextCompat.getDrawable(this@TaskEditor,
                        R.drawable.shape_color_holder)?.let { drawable -> it.tintDrawable(drawable)} )
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
            if (PermissionManager(this).storageReadGranted) {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .setType("*/*"), attachmentRequestCode)
            } else
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PermissionManager.REQUEST_CODE_STORAGE)
        }

        dueDateTextView.setOnClickListener { v ->
            MaterialDialog(this).show {
                lifecycleOwner(this@TaskEditor)
                dateTimePicker(requireFutureDateTime = true,
                    currentDateTime = task.dueDate?.toDateTime()?.toCalendar(Locale.getDefault())) { _, datetime ->
                    task.dueDate = LocalDateTime.fromCalendarFields(datetime).toDateTime()
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) {
                        v.text = task.formatDueDate(this@TaskEditor)
                        v.setTextColorFromResource(R.color.colorPrimaryText)
                    }
                }
            }
        }

        subjectTextView.setOnClickListener {
            startActivityForResult(Intent(this, SubjectSelectorActivity::class.java),
                SubjectSelectorActivity.REQUEST_CODE)
            overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_nothing)
        }

        clearButton.setOnClickListener {
            subjectTextView.startAnimation(animation)

            it.isVisible = false
            this.task.subject = null
            with(subjectTextView) {
                removeCompoundDrawableAtStart()
                setText(R.string.field_not_set)
                setTextColorFromResource(R.color.colorSecondaryText)
            }
        }

        actionButton.setOnClickListener {

            // These if checks if the user have entered the
            // values on the fields, if we don't have the value required,
            // show a snackbar feedback then direct the user's
            // attention to the field. Then return to stop the execution
            // of the code.
            if (nameEditText.text.isNullOrEmpty()) {
                createSnackbar(rootLayout, R.string.feedback_task_empty_name).show()
                nameEditText.requestFocus()
                return@setOnClickListener
            }

            if (task.dueDate == null) {
                createSnackbar(rootLayout, R.string.feedback_task_empty_due_date).show()
                dueDateTextView.performClick()
                return@setOnClickListener
            }

            task.name = nameEditText.text.toString()
            task.notes = notesEditText.text.toString()
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
        if (requestCode == PermissionManager.REQUEST_CODE_STORAGE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("*/*"), attachmentRequestCode)
        } else
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Check if the request codes are the same and the result code is successful
        // then create an Attachment object and a corresponding ChipView
        // attachments have to be inserted temporarily on the ArrayList
        if (requestCode == attachmentRequestCode && resultCode == Activity.RESULT_OK) {
            contentResolver?.takePersistableUriPermission(data?.data!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val attachment = Attachment().apply {
                task = this@TaskEditor.task.taskID
                uri = data?.data
                dateAttached = DateTime.now()
            }

            adapter.insert(attachment)
        } else if (requestCode == SubjectSelectorActivity.REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {

            data?.getParcelableExtra<Subject>(SubjectSelectorActivity.EXTRA_SUBJECT)?.let { subject ->
                clearButton.isVisible = true
                task.subject = subject.subjectID
                this.subject = subject

                with(subjectTextView) {
                    text = subject.code
                    setTextColorFromResource(R.color.colorPrimaryText)
                    ContextCompat.getDrawable(this.context, R.drawable.shape_color_holder)?.let {
                        this.setCompoundDrawableAtStart(it)
                    }
                }
                ContextCompat.getDrawable(this, R.drawable.shape_color_holder)?.let {
                    subjectTextView.setCompoundDrawableAtStart(subject.tintDrawable(it))
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data)
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
    // on the ChipView
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
                    title(R.string.dialog_confirm_deletion_title)
                    message(R.string.dialog_confirm_deletion_summary)
                    positiveButton(R.string.button_delete) {
                        // Send the data back to the parent activity
                        val data = Intent()
                        data.putExtra(EXTRA_TASK, task)
                        data.putExtra(EXTRA_ATTACHMENTS, attachmentList)
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

    companion object {
        const val REQUEST_CODE_INSERT = 32
        const val REQUEST_CODE_UPDATE = 19

        const val EXTRA_TASK = "extra:task"
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_ATTACHMENTS = "extra:attachments"
    }

}