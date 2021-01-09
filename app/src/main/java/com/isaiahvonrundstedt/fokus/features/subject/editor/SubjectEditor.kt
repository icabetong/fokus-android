package com.isaiahvonrundstedt.fokus.features.subject.editor

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ShareCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.ShareOptionsBottomSheet
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toArrayList
import com.isaiahvonrundstedt.fokus.components.interfaces.Streamable
import com.isaiahvonrundstedt.fokus.components.service.DataExporterService
import com.isaiahvonrundstedt.fokus.components.service.DataImporterService
import com.isaiahvonrundstedt.fokus.databinding.ActivityEditorSubjectBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleAdapter
import com.isaiahvonrundstedt.fokus.features.schedule.ScheduleEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBasicAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseService
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectPackage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class SubjectEditor : BaseEditor(), BaseBasicAdapter.ActionListener<Schedule> {
    private lateinit var binding: ActivityEditorSubjectBinding

    private var requestCode = 0

    private val scheduleAdapter = ScheduleAdapter(this)
    private val viewModel: SubjectEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBarLayout.toolbar)

        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        // Check if the parent activity have passed some extras
        requestCode = if (intent.hasExtra(EXTRA_SUBJECT)) REQUEST_CODE_UPDATE
        else REQUEST_CODE_INSERT

        if (requestCode == REQUEST_CODE_UPDATE) {
            viewModel.subject = intent.getParcelableExtra(EXTRA_SUBJECT)
            viewModel.schedules = intent.getParcelableArrayListExtra(EXTRA_SCHEDULE) ?: arrayListOf()

            binding.root.transitionName = TRANSITION_ELEMENT_ROOT + viewModel.subject?.subjectID

            window.sharedElementEnterTransition = buildContainerTransform(binding.root)
            window.sharedElementReturnTransition = buildContainerTransform(binding.root,
                transitionDuration = TRANSITION_SHORT_DURATION)
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
            adapter = scheduleAdapter
        }

        var currentScrollPosition = 0
        binding.contentView.viewTreeObserver.addOnScrollChangedListener {
            if (binding.contentView.scrollY > currentScrollPosition)
                binding.actionButton.hide()
            else binding.actionButton.show()
            currentScrollPosition = binding.contentView.scrollY
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.subjectObservable.observe(this) {
            if (requestCode == REQUEST_CODE_UPDATE && it != null) {
                with(it) {
                    binding.codeTextInput.setText(code)
                    binding.descriptionTextInput.setText(description)
                    binding.tagView.setCompoundDrawableAtStart(binding.tagView.getCompoundDrawableAtStart()
                        ?.let { drawable -> tintDrawable(drawable) })
                    binding.tagView.setText(tag.getNameResource())
                }

                binding.tagView.setTextColorFromResource(R.color.color_primary_text)
            }
        }

        viewModel.schedulesObservable.observe(this) {
            scheduleAdapter.submitList(it)
        }

        binding.codeTextInput.addTextChangedListener {
            viewModel.setSubjectCode(it.toString())
        }
        binding.descriptionTextInput.addTextChangedListener {
            viewModel.setDescription(it.toString())
        }

        binding.addActionLayout.addItemButton.setOnClickListener {
            ScheduleEditor(supportFragmentManager).show {
                arguments = bundleOf(
                    ScheduleEditor.EXTRA_SUBJECT_ID to viewModel.subject?.subjectID
                )
                waitForResult { schedule ->
                    viewModel.addSchedule(schedule)
                    this.dismiss()
                }
            }
        }

        binding.tagView.setOnClickListener {
            MaterialDialog(this).show {
                lifecycleOwner(this@SubjectEditor)
                title(R.string.dialog_pick_color_tag)
                colorChooser(Subject.Tag.getColors(), waitForPositiveButton = false) { _, color ->
                    viewModel.setTag(Subject.Tag.convertColorToTag(color) ?: Subject.Tag.SKY)

                    with(it as TextView) {
                        text = getString(viewModel.getTag()!!.getNameResource())
                        setTextColorFromResource(R.color.color_primary_text)
                        setCompoundDrawableAtStart(
                            viewModel.subject?.tintDrawable(getCompoundDrawableAtStart()))
                    }
                    this.dismiss()
                }
            }
        }

        binding.actionButton.setOnClickListener {
            if (!viewModel.hasSubjectCode()) {
                createSnackbar(R.string.feedback_subject_empty_name, binding.root)
                binding.codeTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!viewModel.hasDescription()) {
                createSnackbar(R.string.feedback_subject_empty_description, binding.root)
                binding.descriptionTextInput.requestFocus()
                return@setOnClickListener
            }

            if (!viewModel.hasSchedules()) {
                createSnackbar(R.string.feedback_subject_no_schedule, binding.root).show()
                return@setOnClickListener
            }

            // Pass the intent to the parent activity
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(EXTRA_SUBJECT, viewModel.subject)
                putExtra(EXTRA_SCHEDULE, viewModel.schedules)
            })

            if (requestCode == REQUEST_CODE_UPDATE)
                supportFinishAfterTransition()
            else finish()
        }
    }

    override fun onActionPerformed(t: Schedule, position: Int, action: BaseBasicAdapter.ActionListener.Action) {
        when (action) {
            BaseBasicAdapter.ActionListener.Action.SELECT -> {
                ScheduleEditor(supportFragmentManager).show {
                    arguments = bundleOf(
                        ScheduleEditor.EXTRA_SUBJECT_ID to viewModel.subject?.subjectID,
                        ScheduleEditor.EXTRA_SCHEDULE to t)
                    waitForResult {
                        viewModel.updateSchedule(it)
                        this.dismiss()
                    }
                }
            }
            BaseBasicAdapter.ActionListener.Action.DELETE -> {
                viewModel.removeSchedule(t)
                createSnackbar(R.string.feedback_schedule_removed, binding.root).run {
                    setAction(R.string.button_undo) { viewModel.addSchedule(t) }
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
                        createSnackbar(R.string.feedback_export_ongoing, binding.root,
                            Snackbar.LENGTH_INDEFINITE)
                    }
                    DataExporterService.BROADCAST_EXPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_export_completed, binding.root)

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
                        createSnackbar(R.string.feedback_export_failed, binding.root)
                    }
                    DataImporterService.BROADCAST_IMPORT_ONGOING -> {
                        createSnackbar(R.string.feedback_import_ongoing, binding.root)
                    }
                    DataImporterService.BROADCAST_IMPORT_COMPLETED -> {
                        createSnackbar(R.string.feedback_import_completed, binding.root)

                        val data: SubjectPackage? = intent.getParcelableExtra(BaseService.EXTRA_BROADCAST_DATA)
                        viewModel.subject = data?.subject
                        viewModel.schedules = data?.schedules?.toArrayList() ?: arrayListOf()
                    }
                    DataImporterService.BROADCAST_IMPORT_FAILED -> {
                        createSnackbar(R.string.feedback_import_failed, binding.root)
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
                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE, viewModel.subject)
                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS, viewModel.schedules)
                })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share_options -> {

                var fileName = viewModel.getSubjectCode() ?: Streamable.ARCHIVE_NAME_GENERIC
                when (requestCode) {
                    REQUEST_CODE_INSERT -> {
                        if (binding.codeTextInput.text.isNullOrEmpty() ||
                                binding.descriptionTextInput.text.isNullOrEmpty()
                            || scheduleAdapter.itemCount < 1) {
                            MaterialDialog(this).show {
                                title(R.string.feedback_unable_to_share_title)
                                message(R.string.feedback_unable_to_share_message)
                                positiveButton(R.string.button_dismiss) { dismiss() }
                            }
                            return false
                        }
                        fileName = binding.codeTextInput.text.toString()
                    }
                    REQUEST_CODE_UPDATE -> {
                        fileName = viewModel.getSubjectCode() ?: Streamable.ARCHIVE_NAME_GENERIC
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
                                    putExtra(DataExporterService.EXTRA_EXPORT_SOURCE,
                                        viewModel.subject)
                                    putExtra(DataExporterService.EXTRA_EXPORT_DEPENDENTS,
                                        viewModel.schedules)
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
            putParcelable(EXTRA_SUBJECT, viewModel.subject)
            putParcelableArrayList(EXTRA_SCHEDULE, viewModel.schedules.toArrayList())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        with(savedInstanceState) {
            viewModel.subject = getParcelable(EXTRA_SUBJECT)
            viewModel.schedules = getParcelableArrayList(EXTRA_SCHEDULE) ?: arrayListOf()
        }
    }

    companion object {
        const val REQUEST_CODE_INSERT = 27
        const val REQUEST_CODE_UPDATE = 13

        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_SCHEDULE = "extra:schedule"

        private const val REQUEST_CODE_EXPORT = 32
        private const val REQUEST_CODE_IMPORT = 95
    }
}