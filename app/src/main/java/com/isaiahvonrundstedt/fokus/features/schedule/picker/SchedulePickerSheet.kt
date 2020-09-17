package com.isaiahvonrundstedt.fokus.features.schedule.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import kotlinx.android.synthetic.main.layout_sheet_schedule.*

class SchedulePickerSheet(private val items: List<Schedule>, manager: FragmentManager)
    : BaseBottomSheet<Schedule>(manager), BaseListAdapter.ActionListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SchedulePickerAdapter(this)
        adapter.setItems(items)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is Schedule) {
            when (action) {
                BaseListAdapter.ActionListener.Action.SELECT -> receiver?.onReceive(t)
                BaseListAdapter.ActionListener.Action.DELETE -> {}
            }
        }
    }
}