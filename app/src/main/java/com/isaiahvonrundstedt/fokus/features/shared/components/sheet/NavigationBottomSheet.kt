package com.isaiahvonrundstedt.fokus.features.shared.components.sheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.about.AboutActivity
import com.isaiahvonrundstedt.fokus.features.notifications.NotificationActivity
import com.isaiahvonrundstedt.fokus.features.settings.SettingsActivity
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.components.adapters.NavigationAdapter
import kotlinx.android.synthetic.main.layout_sheet_navigation.*

class NavigationBottomSheet: BaseBottomSheet(), NavigationAdapter.NavigationListener {

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
            R.id.action_notifications -> startActivity(Intent(context, NotificationActivity::class.java))
            R.id.action_settings -> startActivity(Intent(context, SettingsActivity::class.java))
            R.id.action_about -> startActivity(Intent(context, AboutActivity::class.java))
        }
        this.dismiss()
    }
}