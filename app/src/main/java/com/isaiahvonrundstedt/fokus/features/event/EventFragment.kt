package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.print
import com.isaiahvonrundstedt.fokus.features.event.editor.EventEditor
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.layout_appbar.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class EventFragment : BaseFragment(), BaseAdapter.ActionListener {

    private val adapter = EventAdapter(this)
    private val toolbarDateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val viewModel: EventViewModel by lazy {
        ViewModelProvider(this).get(EventViewModel::class.java)
    }

    private var daysOfWeek: Array<DayOfWeek> = emptyArray()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityToolbar?.title = viewModel.currentMonth.format(toolbarDateFormatter)

        recyclerView.addItemDecoration(ItemDecoration(requireContext()))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        daysOfWeek = daysOfWeekFromLocale()
        calendarView.apply {
            setup(viewModel.startMonth, viewModel.endMonth,
                daysOfWeek.first())
            scrollToMonth(viewModel.currentMonth)
        }

        if (savedInstanceState == null)
            calendarView.post { setCurrentDate(viewModel.today) }

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(requireContext(), adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.events.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.eventsEmpty.observe(viewLifecycleOwner) { emptyView.isVisible = it }
    }

    override fun onStart() {
        super.onStart()

        class DayViewContainer(view: View): ViewContainer(view) {
            lateinit var day: CalendarDay

            val calendarDayView: TextView = view.findViewById(R.id.calendarDayView)
            val calendarDotView: View = view.findViewById(R.id.calendarDotView)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH)
                        setCurrentDate(day.date)
                }
            }
        }

        class MonthViewContainer(view: View): ViewContainer(view) {
            val headerLayout: LinearLayout = view.findViewById(R.id.headerLayout)
        }

        calendarView.dayBinder = object: DayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                bindToCalendar(day, container.calendarDayView, container.calendarDotView)
            }
        }

        calendarView.monthHeaderBinder = object: MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View): MonthViewContainer = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                val headerLayout = container.headerLayout
                if (container.headerLayout.tag == null) {
                    headerLayout.tag = month.yearMonth
                    headerLayout.children.map { it as TextView }.forEachIndexed { index, textView ->
                        textView.text = daysOfWeek[index].name.first().toString()
                    }
                }
            }
        }

        calendarView.monthScrollListener = {
            setCurrentDate(it.yearMonth.atDay(1))
            activityToolbar?.title = it.yearMonth.format(toolbarDateFormatter)

            if (it.yearMonth.minusMonths(2) == viewModel.startMonth) {
                viewModel.startMonth = viewModel.startMonth.minusMonths(2)
                calendarView.updateMonthRangeAsync(startMonth = viewModel.startMonth)
            } else if (it.yearMonth.plusMonths(2) == viewModel.endMonth) {
                viewModel.endMonth = viewModel.endMonth.plusMonths(2)
                calendarView.updateMonthRangeAsync(endMonth = viewModel.endMonth)
            }
        }

        viewModel.dates.observe(viewLifecycleOwner) { dates ->
            calendarView.dayBinder = object: DayBinder<DayViewContainer> {
                override fun create(view: View): DayViewContainer {
                    return DayViewContainer(view)
                }

                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    container.day = day
                    bindToCalendar(day, container.calendarDayView, container.calendarDotView, dates)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            startActivityForResult(Intent(context, EventEditor::class.java),
                EventEditor.REQUEST_CODE_INSERT)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is EventPackage) {
            when (action) {
                // Show up the editorUI and pass the extra
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, EventEditor::class.java).apply {
                        putExtra(EventEditor.EXTRA_EVENT, t.event)
                        putExtra(EventEditor.EXTRA_SUBJECT, t.subject)
                    }
                    startActivityWithTransition(views, intent, EventEditor.REQUEST_CODE_UPDATE)
                }
                // Item has been swiped, notify database for deletion
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.event)

                    createSnackbar(R.string.feedback_event_removed, recyclerView).run {
                        setAction(R.string.button_undo) { viewModel.insert(t.event) }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK)
            return

        // Check the request code first if the data was from TaskEditor
        // so that it doesn't crash when casting the Parcelable object
        if (requestCode == EventEditor.REQUEST_CODE_INSERT ||
            requestCode == EventEditor.REQUEST_CODE_UPDATE) {
            val event: Event? = data?.getParcelableExtra(EventEditor.EXTRA_EVENT)

            event?.also {
                when (requestCode) {
                    EventEditor.REQUEST_CODE_INSERT ->
                        viewModel.insert(it)
                    EventEditor.REQUEST_CODE_UPDATE ->
                        viewModel.update(it)
                }
            }
        }
    }

    private fun bindToCalendar(day: CalendarDay, textView: TextView, view: View,
                               dates: List<LocalDate> = emptyList()) {

        textView.text = day.date.dayOfMonth.toString()
        if (day.owner == DayOwner.THIS_MONTH) {
            when (day.date) {
                viewModel.today -> {
                    textView.setTextColorFromResource(R.color.color_on_primary)
                    textView.setBackgroundResource(R.drawable.shape_calendar_current_day)
                    view.isVisible = false
                }
                viewModel.selectedDate -> {
                    textView.setTextColorFromResource(R.color.color_primary)
                    textView.setBackgroundResource(R.drawable.shape_calendar_selected_day)
                    view.isVisible = false
                }
                else -> {
                    textView.setTextColorFromResource(R.color.color_primary_text)
                    textView.background = null
                    view.isVisible = dates.contains(day.date)
                }
            }
        } else {
            textView.setTextColorFromResource(R.color.color_secondary_text)
            view.isVisible = false
        }
    }

    private fun setCurrentDate(date: LocalDate) {
        if (viewModel.selectedDate != date) {
            val oldDate = viewModel.selectedDate

            viewModel.selectedDate = date
            calendarView.notifyDateChanged(oldDate)
            calendarView.notifyDateChanged(date)
        }
        currentDateTextView.text = date.print("d MMM yyyy")
    }

    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }
}
