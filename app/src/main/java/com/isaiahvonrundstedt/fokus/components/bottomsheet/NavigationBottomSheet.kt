package com.isaiahvonrundstedt.fokus.components.bottomsheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.about.AboutActivity
import com.isaiahvonrundstedt.fokus.features.history.HistoryActivity
import com.isaiahvonrundstedt.fokus.features.settings.SettingsActivity
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.adapters.NavigationAdapter
import kotlinx.android.synthetic.main.layout_sheet_navigation.*

class NavigationBottomSheet(manager: FragmentManager): BaseBottomSheet<Int>(manager)
    , NavigationAdapter.NavigationListener {

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
        callback?.invoke(id)
        this.dismiss()
    }
}