package com.isaiahvonrundstedt.fokus.features.core.activities

import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.NavigationBottomSheet
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.components.utils.NotificationChannelManager
import com.isaiahvonrundstedt.fokus.databinding.ActivityMainBinding
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.editor.EventEditor
import com.isaiahvonrundstedt.fokus.features.notifications.task.TaskReminderWorker
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditor
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.editor.TaskEditor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_calendar_week_days.view.*

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private var controller: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controller = Navigation.findNavController(this, R.id.navigationHostFragment)

        intent?.also { intent ->
            when (intent.action) {
                ACTION_WIDGET_TASK -> {
                    val task: Task? = intent.getParcelableExtra(EXTRA_TASK)
                    val subject: Subject? = intent.getParcelableExtra(EXTRA_SUBJECT)
                    val attachments: List<Attachment>? = intent.getParcelableListExtra(EXTRA_ATTACHMENTS)

                    val args = bundleOf(
                        TaskEditor.EXTRA_TASK to task?.let { Task.toBundle(it) },
                        TaskEditor.EXTRA_SUBJECT to subject?.let { Subject.toBundle(it) },
                        TaskEditor.EXTRA_ATTACHMENTS to attachments
                    )

                    controller?.navigate(R.id.action_to_navigation_editor_task, args)
                }
                ACTION_WIDGET_EVENT -> {
                    val event: Event? = intent.getParcelableExtra(EXTRA_EVENT)
                    val subject: Subject? = intent.getParcelableExtra(EXTRA_SUBJECT)

                    val args = bundleOf(
                        EventEditor.EXTRA_EVENT to event?.let { Event.toBundle(it) },
                        EventEditor.EXTRA_SUBJECT to subject?.let { Subject.toBundle(it) }
                    )

                    controller?.navigate(R.id.action_to_navigation_editor_event, args)
                }
                ACTION_WIDGET_SUBJECT -> {
                    val subject: Subject? = intent.getParcelableExtra(EXTRA_SUBJECT)
                    val schedules: List<Schedule>? = intent.getParcelableListExtra(EXTRA_SCHEDULES)

                    val args = bundleOf(
                        SubjectEditor.EXTRA_SUBJECT to subject?.let { Subject.toBundle(it) },
                        SubjectEditor.EXTRA_SCHEDULE to schedules
                    )

                    controller?.navigate(R.id.action_to_navigation_editor_subject, args)
                }
                ACTION_SHORTCUT_TASK -> {
                    controller?.navigate(R.id.action_to_navigation_editor_task)
                }
                ACTION_SHORTCUT_EVENT -> {
                    controller?.navigate(R.id.action_to_navigation_editor_event)
                }
                ACTION_SHORTCUT_SUBJECT -> {
                    controller?.navigate(R.id.action_to_navigation_editor_subject)
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

        TaskReminderWorker.reschedule(this.applicationContext)

        with(supportFragmentManager) {
            setFragmentResultListener(NavigationBottomSheet.REQUEST_KEY, this@MainActivity) { _, args ->
                args.getInt(NavigationBottomSheet.EXTRA_DESTINATION).also {
                    controller?.navigate(it)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(NotificationChannelManager(this)) {
                register(NotificationChannelManager.CHANNEL_ID_GENERIC,
                    NotificationManager.IMPORTANCE_DEFAULT)
                register(NotificationChannelManager.CHANNEL_ID_TASK,
                    groupID = NotificationChannelManager.CHANNEL_GROUP_ID_REMINDERS)
                register(NotificationChannelManager.CHANNEL_ID_EVENT,
                    groupID = NotificationChannelManager.CHANNEL_GROUP_ID_REMINDERS)
                register(NotificationChannelManager.CHANNEL_ID_CLASS,
                    groupID = NotificationChannelManager.CHANNEL_GROUP_ID_REMINDERS)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = controller?.navigateUp() == true
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                TaskEditor.REQUEST_CODE_INSERT, TaskEditor.REQUEST_CODE_UPDATE -> {
//                    val task: Task? =
//                        data?.getParcelableExtra(TaskEditor.EXTRA_TASK)
//                    val attachments: List<Attachment>? =
//                        data?.getParcelableListExtra(TaskEditor.EXTRA_ATTACHMENTS)
//
//                    task?.also {
//                        if (requestCode == TaskEditor.REQUEST_CODE_INSERT)
//                            tasksViewModel.insert(it, attachments ?: emptyList())
//                        else if (requestCode == TaskEditor.REQUEST_CODE_UPDATE)
//                            tasksViewModel.update(it, attachments ?: emptyList())
//                    }
//                }
//                EventEditor.REQUEST_CODE_INSERT, EventEditor.REQUEST_CODE_UPDATE -> {
//                    val event: Event? = data?.getParcelableExtra(EventEditor.EXTRA_EVENT)
//
//                    event?.also {
//                        if (requestCode == EventEditor.REQUEST_CODE_INSERT)
//                            eventsViewModel.insert(it)
//                        else eventsViewModel.update(it)
//                    }
//                }
//                SubjectEditor.REQUEST_CODE_INSERT, SubjectEditor.REQUEST_CODE_UPDATE -> {
//                    val subject: Subject?
//                            = data?.getParcelableExtra(SubjectEditor.EXTRA_SUBJECT)
//                    val schedules: List<Schedule>?
//                            = data?.getParcelableListExtra(SubjectEditor.EXTRA_SCHEDULE)
//
//                    subject?.also {
//                        if (requestCode == SubjectEditor.REQUEST_CODE_INSERT)
//                            subjectsViewModel.insert(it, schedules ?: emptyList())
//                        else if (requestCode == SubjectEditor.REQUEST_CODE_UPDATE)
//                            subjectsViewModel.update(it, schedules ?: emptyList())
//                    }
//                }
//            }
//        }
//    }
//
//    private val tasksViewModel by lazy {
//        ViewModelProvider(this).get(TaskViewModel::class.java)
//    }
//
//    private val eventsViewModel by lazy {
//        ViewModelProvider(this).get(EventViewModel::class.java)
//    }
//
//    private val subjectsViewModel by lazy {
//        ViewModelProvider(this).get(SubjectViewModel::class.java)
//    }

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