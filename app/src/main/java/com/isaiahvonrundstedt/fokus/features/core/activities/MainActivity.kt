package com.isaiahvonrundstedt.fokus.features.core.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.about.AboutActivity
import com.isaiahvonrundstedt.fokus.features.notifications.NotificationActivity
import com.isaiahvonrundstedt.fokus.features.settings.SettingsActivity
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.adapters.NavigationAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.layout_sheet_navigation.*

class MainActivity: BaseActivity() {

    private var controller: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_tasks)

        toolbar?.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu)
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

    private class NavigationBottomSheet: BaseBottomSheet(), NavigationAdapter.NavigationListener {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.layout_sheet_navigation, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            menuTitleView.text = String.format(getString(R.string.hello),
                PreferenceManager(context).name)

            val adapter = NavigationAdapter(activity, this)
            adapter.setItems(R.menu.menu_main)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
        }

        override fun onNavigate(id: Int) {
            when (id) {
                R.id.action_notifications -> startActivity(
                    Intent(context, NotificationActivity::class.java))
                R.id.action_settings -> startActivity(Intent(context, SettingsActivity::class.java))
                R.id.action_about -> startActivity(Intent(context, AboutActivity::class.java))
            }
            this.dismiss()
        }
    }

}