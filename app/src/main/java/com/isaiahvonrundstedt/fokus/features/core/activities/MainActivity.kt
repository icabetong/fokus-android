package com.isaiahvonrundstedt.fokus.features.core.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.event.EventEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.components.sheet.NavigationBottomSheet
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.subject.SubjectEditor
import com.isaiahvonrundstedt.fokus.features.task.TaskEditor
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_appbar.*

class MainActivity: BaseActivity() {

    private var controller: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_tasks)

        if (intent?.action == actionTask)
            startActivityForResult(Intent(this, TaskEditor::class.java),
                TaskEditor.insertRequestCode)
        else if (intent?.action == actionEvent)
            startActivityForResult(Intent(this, EventEditor::class.java),
                EventEditor.insertRequestCode)
        else if (intent?.action == actionSubject)
            startActivityForResult(Intent(this, SubjectEditor::class.java),
                SubjectEditor.insertRequestCode)

        toolbar?.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu_black)
        toolbar?.setNavigationOnClickListener {
            NavigationBottomSheet()
                .invoke(supportFragmentManager)
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

    companion object {
        const val actionTask = "action:new:task"
        const val actionEvent = "action:new:event"
        const val actionSubject = "action:new:subject"
    }

}