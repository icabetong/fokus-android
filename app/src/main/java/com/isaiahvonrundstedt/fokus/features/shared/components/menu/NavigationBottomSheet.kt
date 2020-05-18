package com.isaiahvonrundstedt.fokus.features.shared.components.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.archived.ArchivedActivity
import com.isaiahvonrundstedt.fokus.features.notifications.NotificationActivity
import com.isaiahvonrundstedt.fokus.features.settings.SettingsActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.subject.SubjectActivity
import kotlinx.android.synthetic.main.layout_sheet_navigation.*

class NavigationBottomSheet: BaseBottomSheet(), MenuAdapter.MenuItemSelectionListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuTitleView.text = String.format(getString(R.string.hello),
            PreferenceManager(context).name)

        val adapter = MenuAdapter(activity, this)
        adapter.setItems(R.menu.menu_navigation)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    override fun onItemSelected(id: Int) {
        when (id) {
            R.id.action_archived -> startActivity(Intent(context, ArchivedActivity::class.java))
            R.id.action_subjects -> startActivity(Intent(context, SubjectActivity::class.java))
            R.id.action_notifications -> startActivity(Intent(context, NotificationActivity::class.java))
            R.id.action_settings -> startActivity(Intent(context, SettingsActivity::class.java))
        }
        this.dismiss()
    }

}