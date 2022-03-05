package com.isaiahvonrundstedt.fokus.features.schedule.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.databinding.LayoutSheetScheduleBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet

class ScheduleViewerSheet(private val items: List<Schedule>, manager: FragmentManager) :
    BaseBottomSheet(manager) {

    private var _binding: LayoutSheetScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LayoutSheetScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = ScheduleViewerAdapter(items)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}