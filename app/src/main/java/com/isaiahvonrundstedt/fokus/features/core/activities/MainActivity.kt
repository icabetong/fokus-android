package com.isaiahvonrundstedt.fokus.features.core.activities

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.NavigationBottomSheet
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.components.extensions.android.putExtra
import com.isaiahvonrundstedt.fokus.features.about.AboutActivity
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.core.work.task.TaskReminderWorker
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventEditor
import com.isaiahvonrundstedt.fokus.features.event.EventViewModel
import com.isaiahvonrundstedt.fokus.features.log.LogActivity
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.settings.SettingsActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectEditor
import com.isaiahvonrundstedt.fokus.features.subject.SubjectViewModel
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskEditor
import com.isaiahvonrundstedt.fokus.features.task.TaskViewModel
import github.com.st235.lib_expandablebottombar.navigation.ExpandableBottomBarNavigationUI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_appbar.*

class MainActivity : BaseActivity() {

    private var controller: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setPersistentActionBar(toolbar)
        //setToolbarTitle(R.string.activity_tasks)
        setToolbarTitle(R.string.activity_tasks)

        TaskReminderWorker.reschedule(this.applicationContext)

        val navigationHost = supportFragmentManager.findFragmentById(R.id.navigationHostFragment)
        controller = navigationHost?.findNavController()
        ExpandableBottomBarNavigationUI.setupWithNavController(navigationView, controller!!)

        intent?.also {
            when (it.action) {
                ACTION_WIDGET_TASK -> {
                    val task: Task? = it.getParcelableExtra(EXTRA_TASK)
                    val subject: Subject? = it.getParcelableExtra(EXTRA_SUBJECT)
                    val attachments: List<Attachment>? = it.getParcelableListExtra(EXTRA_ATTACHMENTS)

                    startActivityForResult(Intent(this, TaskEditor::class.java).apply {
                        putExtra(TaskEditor.EXTRA_TASK, task)
                        putExtra(TaskEditor.EXTRA_SUBJECT, subject)
                        putExtra(TaskEditor.EXTRA_ATTACHMENTS, attachments ?: emptyList())
                    }, TaskEditor.REQUEST_CODE_INSERT)
                }
                ACTION_WIDGET_EVENT -> {
                    val event: Event? = it.getParcelableExtra(EXTRA_EVENT)
                    val subject: Subject? = it.getParcelableExtra(EXTRA_SUBJECT)

                    startActivityForResult(Intent(this, EventEditor::class.java).apply {
                        putExtra(EventEditor.EXTRA_EVENT, event)
                        putExtra(EventEditor.EXTRA_SUBJECT, subject)
                    }, EventEditor.REQUEST_CODE_UPDATE)
                }
                ACTION_WIDGET_SUBJECT -> {
                    val subject: Subject? = it.getParcelableExtra(EXTRA_SUBJECT)
                    val scheduleList: List<Schedule>? = it.getParcelableListExtra(EXTRA_SCHEDULES)

                    startActivityForResult(Intent(this, SubjectEditor::class.java).apply {
                        putExtra(EXTRA_SUBJECT, subject)
                        putExtra(EXTRA_SCHEDULES, scheduleList ?: emptyList())
                    }, SubjectEditor.REQUEST_CODE_UPDATE)
                }
                ACTION_SHORTCUT_TASK -> {
                    startActivityForResult(Intent(this, TaskEditor::class.java),
                        TaskEditor.REQUEST_CODE_INSERT)
                }
                ACTION_SHORTCUT_EVENT -> {
                    startActivityForResult(Intent(this, EventEditor::class.java),
                        EventEditor.REQUEST_CODE_INSERT)
                }
                ACTION_SHORTCUT_SUBJECT -> {
                    startActivityForResult(Intent(this, SubjectEditor::class.java),
                        SubjectEditor.REQUEST_CODE_INSERT)
                }
                ACTION_NAVIGATION_TASK -> {
                    controller?.navigate(R.id.action_to_navigation_tasks)
                }
                ACTION_NAVIGATION_EVENT -> {
                    controller?.navigate(R.id.action_to_navigation_events)
                }
                ACTION_NAVIGATION_SUBJECT -> {
                    controller?.navigate(R.id.action_to_navigation_subjects)
                }
            }
        }

        toolbar?.setNavigationIcon(R.drawable.ic_hero_menu_24)
        toolbar?.setNavigationOnClickListener {
            NavigationBottomSheet(supportFragmentManager).show {
                result { id ->
                    when (id) {
                        R.id.action_history ->
                            startActivity(Intent(context, LogActivity::class.java))
                        R.id.action_settings ->
                            startActivity(Intent(context, SettingsActivity::class.java))
                        R.id.action_about ->
                            startActivity(Intent(context, AboutActivity::class.java))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        controller?.addOnDestinationChangedListener(navigationListener)
    }

    override fun onPause() {
        super.onPause()
        controller?.removeOnDestinationChangedListener(navigationListener)
    }

    private val navigationListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.id) {
            R.id.navigation_tasks ->
                setToolbarTitle(R.string.activity_tasks)
            R.id.navigation_events ->
                setToolbarTitle(R.string.activity_events)
            R.id.navigation_subjects ->
                setToolbarTitle(R.string.activity_subjects)
        }
    }

    override fun onSupportNavigateUp(): Boolean = true

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == BaseEditor.RESULT_OK) {
            when (requestCode) {
                TaskEditor.REQUEST_CODE_INSERT, TaskEditor.REQUEST_CODE_UPDATE -> {
                    val task: Task? =
                        data?.getParcelableExtra(TaskEditor.EXTRA_TASK)
                    val attachments: List<Attachment>? =
                        data?.getParcelableListExtra(TaskEditor.EXTRA_ATTACHMENTS)

                    task?.also {
                        if (requestCode == TaskEditor.REQUEST_CODE_INSERT)
                            tasksViewModel.insert(it, attachments ?: emptyList())
                        else if (requestCode == TaskEditor.REQUEST_CODE_UPDATE)
                            tasksViewModel.update(it, attachments ?: emptyList())
                    }
                    controller?.navigate(R.id.action_to_navigation_tasks)
                }
                EventEditor.REQUEST_CODE_INSERT, EventEditor.REQUEST_CODE_UPDATE -> {
                    val event: Event? = data?.getParcelableExtra(EventEditor.EXTRA_EVENT)

                    event?.also {
                        if (requestCode == EventEditor.REQUEST_CODE_INSERT)
                            eventsViewModel.insert(it)
                        else eventsViewModel.update(it)
                    }
                    controller?.navigate(R.id.action_to_navigation_events)
                }
                SubjectEditor.REQUEST_CODE_INSERT, SubjectEditor.REQUEST_CODE_UPDATE -> {
                    val subject: Subject?
                            = data?.getParcelableExtra(SubjectEditor.EXTRA_SUBJECT)
                    val schedules: List<Schedule>?
                            = data?.getParcelableListExtra(SubjectEditor.EXTRA_SCHEDULE)

                    subject?.also {
                        if (requestCode == SubjectEditor.REQUEST_CODE_INSERT)
                            subjectsViewModel.insert(it, schedules ?: emptyList())
                        else if (requestCode == SubjectEditor.REQUEST_CODE_UPDATE)
                            subjectsViewModel.update(it, schedules ?: emptyList())
                    }
                    controller?.navigate(R.id.action_to_navigation_subjects)
                }
            }
        }
    }

    private val tasksViewModel by lazy {
        ViewModelProvider(this).get(TaskViewModel::class.java)
    }

    private val eventsViewModel by lazy {
        ViewModelProvider(this).get(EventViewModel::class.java)
    }

    private val subjectsViewModel by lazy {
        ViewModelProvider(this).get(SubjectViewModel::class.java)
    }

    companion object {
        const val ACTION_SHORTCUT_TASK = "action:shortcut:task"
        const val ACTION_SHORTCUT_EVENT = "action:shortcut:event"
        const val ACTION_SHORTCUT_SUBJECT = "action:shortcut:subject"

        const val ACTION_WIDGET_TASK = "action:widget:task"
        const val ACTION_WIDGET_EVENT = "action:widget:event"
        const val ACTION_WIDGET_SUBJECT = "action:widget:subject"

        const val ACTION_NAVIGATION_TASK = "action:navigation:task"
        const val ACTION_NAVIGATION_EVENT = "action:navigation:event"
        const val ACTION_NAVIGATION_SUBJECT = "action:navigation:subject"

        const val EXTRA_TASK = "extra:task"
        const val EXTRA_EVENT = "extra:event"
        const val EXTRA_SUBJECT = "extra:subject"
        const val EXTRA_ATTACHMENTS = "extra:attachments"
        const val EXTRA_SCHEDULES = "extra:schedules"
    }

}