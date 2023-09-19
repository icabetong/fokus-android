package com.isaiahvonrundstedt.fokus.features.core.activities

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.getParcelableListExtra
import com.isaiahvonrundstedt.fokus.components.utils.NotificationChannelManager
import com.isaiahvonrundstedt.fokus.databinding.ActivityMainBinding
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.editor.EventEditorFragment
import com.isaiahvonrundstedt.fokus.features.notifications.task.TaskReminderWorker
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.editor.SubjectEditorFragment
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.editor.TaskEditorFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private var controller: NavController? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controller = findNavController(R.id.navigationHostFragment)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
                if (enabled) createSnackbar(R.string.notifications_are_now_enabled_for_this_app)
                else createSnackbar(R.string.notifications_are_not_enabled_for_this_app)
            }
        intent?.also { intent ->
            when (intent.action) {
                ACTION_WIDGET_TASK -> {
                    val task: Task? = intent.getParcelableExtra(EXTRA_TASK)
                    val subject: Subject? = intent.getParcelableExtra(EXTRA_SUBJECT)
                    val attachments: List<Attachment>? =
                        intent.getParcelableListExtra(EXTRA_ATTACHMENTS)

                    val args = bundleOf(
                        TaskEditorFragment.EXTRA_TASK to task?.let { Task.toBundle(it) },
                        TaskEditorFragment.EXTRA_SUBJECT to subject?.let { Subject.toBundle(it) },
                        TaskEditorFragment.EXTRA_ATTACHMENTS to attachments
                    )

                    controller?.navigate(R.id.navigation_editor_task, args)
                }
                ACTION_WIDGET_EVENT -> {
                    val event: Event? = intent.getParcelableExtra(EXTRA_EVENT)
                    val subject: Subject? = intent.getParcelableExtra(EXTRA_SUBJECT)

                    val args = bundleOf(
                        EventEditorFragment.EXTRA_EVENT to event?.let { Event.toBundle(it) },
                        EventEditorFragment.EXTRA_SUBJECT to subject?.let { Subject.toBundle(it) }
                    )

                    controller?.navigate(R.id.navigation_editor_event, args)
                }
                ACTION_WIDGET_SUBJECT -> {
                    val subject: Subject? = intent.getParcelableExtra(EXTRA_SUBJECT)
                    val schedules: List<Schedule>? = intent.getParcelableListExtra(EXTRA_SCHEDULES)

                    val args = bundleOf(
                        SubjectEditorFragment.EXTRA_SUBJECT to subject?.let { Subject.toBundle(it) },
                        SubjectEditorFragment.EXTRA_SCHEDULE to schedules
                    )
                    controller?.navigate(R.id.navigation_editor_subject, args)
                }
                ACTION_SHORTCUT_TASK -> {
                    controller?.navigate(R.id.navigation_editor_task)
                }
                ACTION_SHORTCUT_EVENT -> {
                    controller?.navigate(R.id.navigation_editor_event)
                }
                ACTION_SHORTCUT_SUBJECT -> {
                    controller?.navigate(R.id.navigation_editor_subject)
                }
                ACTION_NAVIGATION_TASK -> {
                    //controller?.navigate(R.id.navigation_tasks)
                }
                ACTION_NAVIGATION_EVENT -> {
                    //controller?.navigate(R.id.navigation_events)
                }
                ACTION_NAVIGATION_SUBJECT -> {
                    //controller?.navigate(R.id.navigation_subjects)
                }
            }
        }

        TaskReminderWorker.reschedule(this.applicationContext)
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU && !notificationManager.areNotificationsEnabled()) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                with(NotificationChannelManager(this)) {
                    register(
                        NotificationChannelManager.CHANNEL_ID_GENERIC,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    register(
                        NotificationChannelManager.CHANNEL_ID_TASK,
                        groupID = NotificationChannelManager.CHANNEL_GROUP_ID_REMINDERS
                    )
                    register(
                        NotificationChannelManager.CHANNEL_ID_EVENT,
                        groupID = NotificationChannelManager.CHANNEL_GROUP_ID_REMINDERS
                    )
                    register(
                        NotificationChannelManager.CHANNEL_ID_CLASS,
                        groupID = NotificationChannelManager.CHANNEL_GROUP_ID_REMINDERS
                    )
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = controller?.navigateUp() ?: false

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