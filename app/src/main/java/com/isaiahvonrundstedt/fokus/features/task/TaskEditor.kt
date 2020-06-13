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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.customListAdapter
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.extensions.getUsingID
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.features.shared.PermissionManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.adapters.SubjectListAdapter
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectEditor
import com.isaiahvonrundstedt.fokus.features.subject.SubjectViewModel
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_task.*
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class TaskEditor: BaseEditor(), SubjectListAdapter.ItemSelected {

    private var requestCode = 0
    private var task = Task()

    private var subject: Subject? = null
    private var subjectDialog: MaterialDialog? = null

    private val attachmentRequestCode = 32
    private val attachmentList = ArrayList<Attachment>()
    private val adapter = SubjectListAdapter(this)
    private val viewModel: SubjectViewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_task)
        setPersistentActionBar(toolbar)

        // Check if the parent activity has extras sent then
        // determine if the editor will be in insert or
        // update mode
        requestCode = if (intent.hasExtra(extraTask) && intent.hasExtra(extraSubject)
                && intent.hasExtra(extraAttachments)) updateRequestCode else insertRequestCode

        if (requestCode == updateRequestCode) {
            task = intent.getParcelableExtra(extraTask)!!
            subject = intent.getParcelableExtra(extraSubject)
            attachmentList.clear()
            attachmentList.addAll(intent.getParcelableArrayListExtra(extraAttachments) ?: emptyList())

            val id = task.taskID
            setTransitionName(nameEditText, TaskAdapter.transitionNameID + id)
            setTransitionName(dueDateTextView, TaskAdapter.transitionDateID + id)
        }

        viewModel.fetch()?.observe(this, Observer { items ->
            adapter.submitList(items)
        })

        prioritySwitch.changeTextColorWhenChecked()

        // The passed extras from the parent activity
        // will be shown in their respective fields.
        if (requestCode == updateRequestCode) {
            with(task) {
                nameEditText.setText(name)
                notesEditText.setText(notes)
                dueDateTextView.text = formatDueDate(this@TaskEditor)
                dueDateTextView.setTextColorFromResource(R.color.colorPrimaryText)
                prioritySwitch.isChecked = isImportant
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

            attachmentList.forEach { attachment ->
                attachmentChipGroup.addView(createChip(attachment), 0)
            }

            window.decorView.rootView.clearFocus()
        }
    }

    override fun onStart() {
        super.onStart()

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
            subjectDialog = MaterialDialog(this, BottomSheet()).show {
                lifecycleOwner(this@TaskEditor)
                title(R.string.dialog_select_subject)
                customListAdapter(adapter)
                positiveButton(R.string.button_new) {
                    startActivityForResult(Intent(this@TaskEditor,
                        SubjectEditor::class.java), SubjectEditor.insertRequestCode)
                }
            }
        }

        attachmentChip.setOnClickListener {
            // Check if we have read storage permissions then request the permission
            // if we have the permission, open up file picker
            if (PermissionManager(this).storageReadGranted) {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .setType("*/*"), attachmentRequestCode)
            } else
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PermissionManager.storageRequestCode)
        }

        clearButton.setOnClickListener {
            it.isVisible = false
            this.task.subjectID = null
            with(subjectTextView) {
                removeCompoundDrawableAtStart()
                setText(R.string.field_not_set)
                setTextColorFromResource(R.color.colorSecondaryText)
            }
        }

        actionButton.setOnClickListener {

            // This three ifs checks if the user have entered the
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

            // Send the data back to the parent activity
            val data = Intent()
            data.putExtra(extraTask, task)
            data.putParcelableArrayListExtra(extraAttachments, attachmentList.toArrayList())
            setResult(RESULT_OK, data)
            supportFinishAfterTransition()
        }
    }

    // Item selection callback for the subject dialog
    override fun onItemSelected(subject: Subject) {
        clearButton.isVisible = true
        task.subjectID = subject.id
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
        subjectDialog?.dismiss()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == PermissionManager.storageRequestCode
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
                taskID = task.taskID
                uri = data?.data
                dateAttached = DateTime.now()
            }

            attachmentList.add(attachment)
            attachmentChipGroup.addView(createChip(attachment), 0)
        } else if (requestCode == SubjectEditor.insertRequestCode && resultCode == Activity.RESULT_OK) {
            val subject: Subject? = data?.getParcelableExtra(SubjectEditor.extraSubject)

            subject?.let {
                viewModel.insert(it)
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createChip(attachment: Attachment): Chip {
        return Chip(this).apply {
            text = attachment.uri!!.getFileName(this@TaskEditor)
            tag = attachment.id
            isCloseIconVisible = true
            setOnClickListener(chipClickListener)
            setOnCloseIconClickListener(chipRemoveListener)
        }
    }

    private val chipClickListener = View.OnClickListener {
        val attachment = attachmentList.getUsingID(it.tag.toString())
        if (attachment != null) onParseIntent(attachment.uri)
    }

    private val chipRemoveListener = View.OnClickListener {
        val index = attachmentChipGroup.indexOfChild(it)
        attachmentChipGroup.removeViewAt(index)

        val attachment = attachmentList.getUsingID(it.tag.toString())
        if (attachment != null) attachmentList.remove(attachment)
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
        return if (requestCode == updateRequestCode)
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
                        data.putExtra(extraTask, task)
                        data.putParcelableArrayListExtra(extraAttachments, attachmentList.toArrayList())
                        setResult(RESULT_DELETE, data)
                        supportFinishAfterTransition()
                    }
                    negativeButton(R.string.button_cancel)
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    companion object {
        const val insertRequestCode = 32
        const val updateRequestCode = 19

        const val extraTask = "extraTask"
        const val extraSubject = "extraSubject"
        const val extraAttachments = "extraAttachments"
    }

}