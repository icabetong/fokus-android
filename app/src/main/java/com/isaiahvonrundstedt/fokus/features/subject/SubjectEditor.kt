package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleAdapter
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_subject.*
import kotlinx.android.synthetic.main.layout_item_add.*

class SubjectEditor: BaseEditor(), BaseBottomSheet.DismissListener, BaseAdapter.ActionListener {

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
        requestCode = if (intent.hasExtra(EXTRA_SUBJECT)) REQUEST_CODE_UPDATE else REQUEST_CODE_INSERT

        if (requestCode == REQUEST_CODE_UPDATE) {
            subject = intent.getParcelableExtra(EXTRA_SUBJECT)!!
            adapter.setItems(intent.getListExtra(EXTRA_SCHEDULE)!!)

            setTransitionName(codeEditText, SubjectAdapter.TRANSITION_CODE_ID + subject.subjectID)
            setTransitionName(descriptionEditText, SubjectAdapter.TRANSITION_DESCRIPTION_ID + subject.subjectID)
        }

        // Get actual values for the items
        colors = Subject.Tag.getColors()

        // The extras passed by the parent activity will
        // be shown to the fields.
        if (requestCode == REQUEST_CODE_UPDATE) {
            with(subject) {
                codeEditText.setText(code)
                descriptionEditText.setText(description)
                tagView.setCompoundDrawableAtStart(tagView.getCompoundDrawableAtStart()
                    ?.let { drawable -> tintDrawable(drawable) })
                tagView.setText(tag.getNameResource())

            }

            tagView.setTextColorFromResource(R.color.colorPrimaryText)

            window.decorView.rootView.clearFocus()
        }
    }

    override fun <T> onDismiss(t: T, requestCode: Int) {
        if (t is Schedule){
            when (requestCode) {
                ScheduleEditor.REQUEST_CODE_INSERT -> adapter.insert(t)
                ScheduleEditor.REQUEST_CODE_UPDATE -> adapter.update(t)
            }
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is Schedule) {
            when (action) {
                BaseAdapter.ActionListener.Action.DELETE -> {
                    adapter.remove(t)
                }
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val editor = ScheduleEditor(this)
                    editor.arguments = bundleOf(
                        Pair(EXTRA_SUBJECT, subject.subjectID),
                        Pair(EXTRA_SCHEDULE, t)
                    )
                    editor.invoke(supportFragmentManager)
                }
                BaseAdapter.ActionListener.Action.MODIFY -> {}
            }
        }
    }

    override fun onStart() {
        super.onStart()

        addItemButton.setOnClickListener {
            val editor = ScheduleEditor(this)
            editor.arguments = bundleOf(Pair(EXTRA_SUBJECT, subject.subjectID))
            editor.invoke(supportFragmentManager)
        }

        tagView.setOnClickListener {
            MaterialDialog(it.context, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                lifecycleOwner(this@SubjectEditor)
                title(R.string.dialog_select_color_tag)
                colorChooser(colors!!) { _, color ->
                    subject.tag = Subject.Tag.convertColorToTag(color)!!

                    with(it as TextView) {
                        text = getString(subject.tag.getNameResource())
                        setTextColorFromResource(R.color.colorPrimaryText)
                        setCompoundDrawableAtStart(subject.tintDrawable(getCompoundDrawableAtStart()))
                    }
                }
            }
        }

        actionButton.setOnClickListener {

            if (codeEditText.text.isNullOrEmpty()) {
                createSnackbar(rootLayout, R.string.feedback_subject_empty_name).show()
                codeEditText.requestFocus()
                return@setOnClickListener
            }

            if (descriptionEditText.text.isNullOrEmpty()) {
                createSnackbar(rootLayout, R.string.feedback_subject_empty_description).show()
                descriptionEditText.requestFocus()
                return@setOnClickListener
            }

            if (adapter.itemCount == 0) {
                createSnackbar(rootLayout, R.string.feedback_subject_no_schedule).show()
                return@setOnClickListener
            }

            subject.code = codeEditText.text.toString()
            subject.description = descriptionEditText.text.toString()

            // Pass the intent to the parent activity
            val data = Intent()
            data.putExtra(EXTRA_SUBJECT, subject)
            data.putExtra(EXTRA_SCHEDULE, adapter.itemList)
            setResult(Activity.RESULT_OK, data)
            supportFinishAfterTransition()
        }
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

    companion object {
        const val REQUEST_CODE_INSERT = 27
        const val REQUEST_CODE_UPDATE = 13
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_SCHEDULE = "extra:schedule"
    }
}