package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat.setTransitionName
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.extensions.toArrayList
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleAdapter
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_subject.*
import kotlinx.android.synthetic.main.layout_item_add.*

class SubjectEditor : BaseEditor(), BaseAdapter.ActionListener {

    private var requestCode = 0
    private var subject = Subject()
    private var colors: IntArray? = null

    private val adapter = ScheduleAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_subject)
        setPersistentActionBar(toolbar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Check if the parent activity have passed some extras
        requestCode = if (intent.hasExtra(EXTRA_SUBJECT)) REQUEST_CODE_UPDATE
        else REQUEST_CODE_INSERT

        if (requestCode == REQUEST_CODE_UPDATE) {
            subject = intent.getParcelableExtra(EXTRA_SUBJECT)!!
            adapter.setItems(intent.getParcelableListExtra(EXTRA_SCHEDULE) ?: emptyList())

            setTransitionName(codeTextInput, SubjectAdapter.TRANSITION_CODE_ID + subject.subjectID)
            setTransitionName(descriptionTextInput, SubjectAdapter.TRANSITION_DESCRIPTION_ID + subject.subjectID)
        }

        // Get actual values for the items
        colors = Subject.Tag.getColors()

        // The extras passed by the parent activity will
        // be shown to the fields.
        if (requestCode == REQUEST_CODE_UPDATE) {
            with(subject) {
                codeTextInput.setText(code)
                descriptionTextInput.setText(description)
                tagView.setCompoundDrawableAtStart(tagView.getCompoundDrawableAtStart()
                    ?.let { drawable -> tintDrawable(drawable) })
                tagView.setText(tag.getNameResource())
            }

            tagView.setTextColorFromResource(R.color.color_primary_text)

            window.decorView.rootView.clearFocus()
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action) {
        if (t is Schedule) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    ScheduleEditor(supportFragmentManager).show {
                        arguments = bundleOf(
                            Pair(ScheduleEditor.EXTRA_SUBJECT_ID, subject.subjectID),
                            Pair(ScheduleEditor.EXTRA_SCHEDULE, t)
                        )
                        result { adapter.update(it) }
                    }
                }
                BaseAdapter.ActionListener.Action.DELETE -> {
                    adapter.remove(t)
                    createSnackbar(rootLayout, R.string.feedback_schedule_removed).run {
                        setAction(R.string.button_undo) { adapter.insert(t) }
                        show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        addItemButton.setOnClickListener {
            ScheduleEditor(supportFragmentManager).show {
                arguments = bundleOf(
                    Pair(ScheduleEditor.EXTRA_SUBJECT_ID, subject.subjectID)
                )
                result { adapter.insert(it) }
            }
        }

        tagView.setOnClickListener {
            MaterialDialog(it.context, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                lifecycleOwner(this@SubjectEditor)
                title(R.string.dialog_select_color_tag)
                colorChooser(colors!!, waitForPositiveButton = false) { _, color ->
                    subject.tag = Subject.Tag.convertColorToTag(
                        color)!!

                    with(it as TextView) {
                        text = getString(subject.tag.getNameResource())
                        setTextColorFromResource(R.color.color_primary_text)
                        setCompoundDrawableAtStart(subject.tintDrawable(getCompoundDrawableAtStart()))
                    }
                    this.dismiss()
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if (requestCode == REQUEST_CODE_UPDATE) {
            menuInflater.inflate(R.menu.menu_editor_update, menu)
            true
        } else super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {

                if (codeTextInput.text.isNullOrEmpty()) {
                    createSnackbar(rootLayout, R.string.feedback_subject_empty_name).show()
                    codeTextInput.requestFocus()
                    return false
                }

                if (descriptionTextInput.text.isNullOrEmpty()) {
                    createSnackbar(rootLayout, R.string.feedback_subject_empty_description).show()
                    descriptionTextInput.requestFocus()
                    return false
                }

                if (adapter.itemCount == 0) {
                    createSnackbar(rootLayout, R.string.feedback_subject_no_schedule).show()
                    return false
                }

                subject.code = codeTextInput.text.toString()
                subject.description = descriptionTextInput.text.toString()

                // Pass the intent to the parent activity
                val data = Intent()
                data.putExtra(EXTRA_SUBJECT, subject)
                data.putExtra(EXTRA_SCHEDULE, adapter.itemList)
                setResult(Activity.RESULT_OK, data)
                supportFinishAfterTransition()
            }
            R.id.action_delete -> {
                MaterialDialog(this).show {
                    title(text = String.format(getString(R.string.dialog_confirm_deletion_title),
                        subject.code))
                    message(R.string.dialog_confirm_deletion_summary)
                    positiveButton(R.string.button_delete) {
                        // Pass the intent to the parent activity
                        val data = Intent()
                        data.putExtra(EXTRA_SUBJECT, subject)
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
            putParcelable(EXTRA_SUBJECT, subject)
            putParcelableArrayList(EXTRA_SCHEDULE, adapter.itemList.toArrayList())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        with(savedInstanceState) {
            getParcelable<Subject>(EXTRA_SUBJECT)?.let {
                this@SubjectEditor.subject = it
            }
            getParcelableArrayList<Schedule>(EXTRA_SCHEDULE)?.let {
                this@SubjectEditor.adapter.setItems(it)
            }
        }
    }

    companion object {
        const val REQUEST_CODE_INSERT = 27
        const val REQUEST_CODE_UPDATE = 13

        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_SCHEDULE = "extra:schedule"
    }
}