package com.isaiahvonrundstedt.fokus.features.subject.editor

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat.setTransitionName
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.ShareOptionsBottomSheet
import com.isaiahvonrundstedt.fokus.components.extensions.android.*
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toArrayList
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.DataExporterService
import com.isaiahvonrundstedt.fokus.components.service.DataImporterService
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleAdapter
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_subject.*
import kotlinx.android.synthetic.main.layout_item_add.*
import java.io.File

class SubjectEditor : BaseEditor(), BaseListAdapter.ActionListener {

    private val viewModel by lazy {
        ViewModelProvider(this).get(SubjectEditorViewModel::class.java)
    }

    private var requestCode = 0
    private var hasFieldChange = false

    private val adapter = ScheduleAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_subject)
        setPersistentActionBar(toolbar)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(BaseService.ACTION_SERVICE_BROADCAST))

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Check if the parent activity have passed some extras
        requestCode = if (intent.hasExtra(EXTRA_SUBJECT)) REQUEST_CODE_UPDATE
        else REQUEST_CODE_INSERT

        if (requestCode == REQUEST_CODE_UPDATE) {
            viewModel.setSubject(intent.getParcelableExtra(EXTRA_SUBJECT))
            viewModel.setSchedules(intent.getParcelableListExtra(EXTRA_SCHEDULE))

            setTransitionName(codeTextInput,
                TRANSITION_ID_CODE + viewModel.getSubject()?.subjectID)
            setTransitionName(descriptionTextInput,
                TRANSITION_ID_DESCRIPTION + viewModel.getSubject()?.subjectID)
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

        viewModel.subject.observe(this) {
            if (requestCode == REQUEST_CODE_UPDATE && it != null) {
                with(it) {
                    codeTextInput.setText(code)
                    descriptionTextInput.setText(description)
                    tagView.setCompoundDrawableAtStart(tagView.getCompoundDrawableAtStart()
                        ?.let { drawable -> tintDrawable(drawable) })
                    tagView.setText(tag.getNameResource())
                }

                tagView.setTextColorFromResource(R.color.color_primary_text)
            }
        }

        viewModel.schedules.observe(this) {
            adapter.submitList(it)
        }

        codeTextInput.addTextChangedListener {
            viewModel.getSubject()?.code = it.toString()
            hasFieldChange = true
        }
        descriptionTextInput.addTextChangedListener {
            viewModel.getSubject()?.description = it.toString()
            hasFieldChange = true
        }

        addItemButton.setOnClickListener {
            ScheduleEditor(supportFragmentManager).show {
                arguments = bundleOf(
                    Pair(ScheduleEditor.EXTRA_SUBJECT_ID, viewModel.getSubject()?.subjectID)
                )
                waitForResult { viewModel.addSchedule(it) }
            }
        }

        tagView.setOnClickListener {
            MaterialDialog(this).show {
                lifecycleOwner(this@SubjectEditor)
                title(R.string.dialog_pick_color_tag)
                colorChooser(Subject.Tag.getColors(), waitForPositiveButton = false) { _, color ->
                    viewModel.getSubject()?.tag = Subject.Tag.convertColorToTag(
                        color)!!

                    hasFieldChange = true
                    with(it as TextView) {
                        text = getString(viewModel.getSubject()?.tag!!.getNameResource())
                        setTextColorFromResource(R.color.color_primary_text)
                        setCompoundDrawableAtStart(
                            viewModel.getSubject()?.tintDrawable(getCompoundDrawableAtStart()))
                    }
                    this.dismiss()
                }
            }
        }

        actionButton.setOnClickListener {
            if (viewModel.hasSubjectCode) {
                createSnackbar(R.string.feedback_subject_empty_name, rootLayout)
                codeTextInput.requestFocus()
                return@setOnClickListener
            }

            if (viewModel.hasDescription) {
                createSnackbar(R.string.feedback_subject_empty_description, rootLayout)
                descriptionTextInput.requestFocus()
                return@setOnClickListener
            }

            if (viewModel.hasSchedules) {
                createSnackbar(R.string.feedback_subject_no_schedule, rootLayout).show()
                return@setOnClickListener
            }

            // Pass the intent to the parent activity
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(EXTRA_SUBJECT, viewModel.getSubject())
                putExtra(EXTRA_SCHEDULE, viewModel.getSchedules())
            })

            if (requestCode == REQUEST_CODE_UPDATE)
                supportFinishAfterTransition()
            else finish()
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is Schedule) {
            when (action) {
                BaseListAdapter.ActionListener.Action.SELECT -> {
                    ScheduleEditor(supportFragmentManager).show {
                        arguments = bundleOf(
                            Pair(ScheduleEditor.EXTRA_SUBJECT_ID, viewModel.getSubject()?.subjectID),
                            Pair(ScheduleEditor.EXTRA_SCHEDULE, t)
                        )
                        waitForResult {
                            viewModel.updateSchedule(it)
                            hasFieldChange = true
                        }
                    }
                }
                BaseListAdapter.ActionListener.Action.DELETE -> {
                    viewModel.removeSchedule(t)
                    hasFieldChange = true
                    createSnackbar(R.string.feedback_schedule_removed, rootLayout).run {
                        setAction(R.string.button_undo) { viewModel.addSchedule(t) }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)
    }

    private var receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BaseService.ACTION_SERVICE_BROADCAST) {
                when (intent.getStringExtra(BaseService.EXTRA_BROADCAST_STATUS)) {
                    DataExporterService.BROADCAST_EXPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_export_ongoing, rootLayout,
                            Snackbar.LENGTH_INDEFINITE)
                    }
                    DataExporterService.BROADCAST_EXPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_export_completed, rootLayout)

                        intent.getStringExtra(BaseService.EXTRA_BROADCAST_DATA)?.also {
                            val uri = CoreApplication.obtainUriForFile(this@SubjectEditor, File(it))

                            startActivity(ShareCompat.IntentBuilder.from(this@SubjectEditor)
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

                        val data: SubjectPackage? = intent.getParcelableExtra(BaseService.EXTRA_BROADCAST_DATA)
                        viewModel.setSubject(data?.subject)
                        viewModel.setSchedules(data?.schedules)
                    }
                    DataImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, rootLayout)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            REQUEST_CODE_IMPORT ->
                startService(Intent(this, DataImporterService::class.java).apply {
                    this.data = data?.data
                    action = DataImporterService.ACTION_IMPORT_SUBJECT
                })
            REQUEST_CODE_EXPORT ->
                startService(Intent(this, DataExporterService::class.java).apply {
                    this.data = data?.data
                    action = DataExporterService.ACTION_EXPORT_SUBJECT
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.getSubject())
                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, viewModel.getSchedules())
                })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share_options -> {

                var fileName = viewModel.getSubject()?.code ?: Streamable.ARCHIVE_NAME_GENERIC
                when (requestCode) {
                    REQUEST_CODE_INSERT -> {
                        if (codeTextInput.text.isNullOrEmpty() || descriptionTextInput.text.isNullOrEmpty()
                            || adapter.itemCount < 1) {
                            MaterialDialog(this).show {
                                title(R.string.feedback_unable_to_share_title)
                                message(R.string.feedback_unable_to_share_message)
                                positiveButton(R.string.button_dismiss) { dismiss() }
                            }
                            return false
                        }
                        fileName = codeTextInput.text.toString()
                    }
                    REQUEST_CODE_UPDATE -> {
                        fileName = viewModel.getSubject()?.code ?: Streamable.ARCHIVE_NAME_GENERIC
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

                                this@SubjectEditor.startActivityForResult(exportIntent,
                                    REQUEST_CODE_EXPORT)
                            }
                            R.id.action_share -> {
                                val serviceIntent = Intent(this@SubjectEditor, DataExporterService::class.java).apply {
                                    action = DataExporterService.ACTION_EXPORT_SUBJECT
                                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.getSubject())
                                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, viewModel.getSchedules())
                                }

                                this@SubjectEditor.startService(serviceIntent)
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
            putParcelable(EXTRA_SUBJECT, viewModel.getSubject())
            putParcelableArrayList(EXTRA_SCHEDULE, viewModel.getSchedules().toArrayList())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        with(savedInstanceState) {
            viewModel.setSubject(getParcelable(EXTRA_SUBJECT))
            viewModel.setSchedules(getParcelableArrayList(EXTRA_SCHEDULE))
        }
    }

    override fun onBackPressed() {
        if (hasFieldChange) {
            MaterialDialog(this).show {
                title(R.string.dialog_discard_changes)
                positiveButton(R.string.button_discard) { super.onBackPressed() }
                negativeButton(R.string.button_cancel)
            }
        } else super.onBackPressed()
    }

    companion object {
        const val REQUEST_CODE_INSERT = 27
        const val REQUEST_CODE_UPDATE = 13

        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_SCHEDULE = "extra:schedule"

        const val TRANSITION_ID_CODE = "transition:subject:code:"
        const val TRANSITION_ID_DESCRIPTION = "transition:subject:description:"

        private const val REQUEST_CODE_EXPORT = 32
        private const val REQUEST_CODE_IMPORT = 95
    }
}