package com.isaiahvonrundstedt.fokus.components.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.PreferenceManager
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.adapters.NavigationAdapter
import kotlinx.android.synthetic.main.layout_sheet_navigation.*
import org.joda.time.LocalTime

class NavigationBottomSheet(manager: FragmentManager): BaseBottomSheet<Int>(manager),
    NavigationAdapter.NavigationListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentTime = LocalTime.now()
        menuTitleView.text =
            when (currentTime.hourOfDay) {
                in 0..6 -> getString(R.string.greeting_default)
                in 7..12 -> getString(R.string.greeting_morning)
                in 13..18 -> getString(R.string.greeting_afternoon)
                in 19..23 -> getString(R.string.greeting_evening)
                else -> getString(R.string.greeting_default)
            }

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