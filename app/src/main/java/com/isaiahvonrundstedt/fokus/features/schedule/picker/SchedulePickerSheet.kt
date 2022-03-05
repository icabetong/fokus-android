package com.isaiahvonrundstedt.fokus.features.schedule.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.databinding.LayoutSheetScheduleBinding
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet

class SchedulePickerSheet(private val items: List<Schedule>, manager: FragmentManager) :
    BaseBottomSheet(manager), BaseAdapter.ActionListener {

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
            adapter = SchedulePickerAdapter(this@SchedulePickerSheet).apply {
                setItems(items)
            }
        }
    }

    override fun <T> onActionPerformed(
        t: T, action: BaseAdapter.ActionListener.Action,
        container: View?
    ) {
        if (t is Schedule) {
            when (action) {
                BaseAdapter.ActionListener.Action.SELECT -> {
                    setFragmentResult(
                        REQUEST_KEY, bundleOf(
                            EXTRA_SCHEDULE to t
                        )
                    )
                    this.dismiss()
                }
                BaseAdapter.ActionListener.Action.DELETE -> {
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "request:pick"
        const val EXTRA_SCHEDULE = "extra:schedule"

        fun show(items: List<Schedule>, manager: FragmentManager) {
            SchedulePickerSheet(items, manager).show()
        }
    }
}