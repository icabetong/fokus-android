package com.isaiahvonrundstedt.fokus.features.schedule.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import kotlinx.android.synthetic.main.layout_sheet_schedule.*

class ScheduleViewerSheet(private val items: List<Schedule>, manager: FragmentManager)
    : BaseBottomSheet<Unit>(manager) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = ScheduleViewerAdapter(items)
        }
    }
}