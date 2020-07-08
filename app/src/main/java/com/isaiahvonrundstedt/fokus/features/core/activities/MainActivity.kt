package com.isaiahvonrundstedt.fokus.features.core.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.NavigationBottomSheet
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.event.Event
import com.isaiahvonrundstedt.fokus.features.event.EventEditor
import com.isaiahvonrundstedt.fokus.features.event.EventViewModel
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskEditor
import com.isaiahvonrundstedt.fokus.features.task.TaskViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_appbar.*

class MainActivity: BaseActivity() {

    private var controller: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_tasks)

        intent?.getStringExtra(StartupActivity.EXTRA_SHORTCUT_ACTION)?.let {
            when (it) {
                StartupActivity.SHORTCUT_ACTION_TASK ->
                    startActivityForResult(Intent(this, TaskEditor::class.java),
                        TaskEditor.REQUEST_CODE_INSERT)
                StartupActivity.SHORTCUT_ACTION_EVENT ->
                    startActivityForResult(Intent(this, EventEditor::class.java),
                        EventEditor.REQUEST_CODE_INSERT)
            }
        }

        toolbar?.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_outline_menu_24)
        toolbar?.setNavigationOnClickListener {
            NavigationBottomSheet().invoke(supportFragmentManager)
        }

        val navigationHost = supportFragmentManager.findFragmentById(R.id.navigationHostFragment)
        controller = navigationHost?.findNavController()
        NavigationUI.setupWithNavController(navigationView, controller!!)
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
            R.id.navigation_tasks ->  setToolbarTitle(R.string.activity_tasks)
            R.id.navigation_events ->  setToolbarTitle(R.string.activity_events)
            R.id.navigation_subjects -> setToolbarTitle(R.string.activity_subjects)
        }
    }

    override fun onSupportNavigateUp(): Boolean = true

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == BaseEditor.RESULT_OK) {
            when (requestCode) {
                TaskEditor.REQUEST_CODE_INSERT -> {
                    val task: Task? = data?.getParcelableExtra(TaskEditor.EXTRA_TASK)
                    val attachments: List<Attachment>? =
                        data?.getParcelableArrayListExtra(TaskEditor.EXTRA_ATTACHMENTS)

                    task?.also {
                        tasksViewModel.insert(it, attachments ?: emptyList())
                    }
                }
                EventEditor.REQUEST_CODE_INSERT -> {
                    val event: Event? = data?.getParcelableExtra(EventEditor.EXTRA_EVENT)

                    event?.also {
                        eventsViewModel.insert(it)
                    }
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

}