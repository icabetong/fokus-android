package com.isaiahvonrundstedt.fokus.features.event

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
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
import com.isaiahvonrundstedt.fokus.features.event.editor.EventEditor
import com.isaiahvonrundstedt.fokus.features.event.previous.PreviousEventsActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseListAdapter
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.layout_empty_events.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class EventFragment : BaseFragment(), BaseListAdapter.ActionListener {

    private val adapter = EventAdapter(this)
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val toolbarFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    private val viewModel: EventViewModel by lazy {
        ViewModelProvider(this).get(EventViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.addItemDecoration(ItemDecoration(requireContext()))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        calendarView.apply {
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10),
                daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        if (savedInstanceState == null)
        calendarView.post { selectDate(viewModel.today) }

        val itemTouchHelper = ItemTouchHelper(ItemSwipeCallback(requireContext(), adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel.events.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.eventsEmpty.observe(viewLifecycleOwner) { emptyView.isVisible = it }

        class DayViewContainer(view: View): ViewContainer(view) {
            lateinit var day: CalendarDay

            val calendarDayView: TextView = view.findViewById(R.id.calendarDay)
            val calendarDotView: View = view.findViewById(R.id.calendarDotView)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH)
                        selectDate(day.date)
                }
            }
        }

        class MonthViewContainer(view: View): ViewContainer(view) {
            val daysOfWeekLayout: LinearLayout = view.findViewById(R.id.legendLayout)
        }

        calendarView.dayBinder = object: DayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day

                val textView = container.calendarDayView
                val dotView = container.calendarDotView

                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    when (day.date) {
                        viewModel.today -> {
                            textView.setTextColorFromResource(R.color.color_surface)
                            textView.setBackgroundResource(R.drawable.shape_calendar_today)
                            dotView.isVisible = false
                        }
                        viewModel.selectedDate -> {
                            textView.setTextColorFromResource(R.color.color_primary)
                            textView.setBackgroundResource(R.drawable.shape_calendar_selected_day)
                            dotView.isVisible = false
                        }
                        else -> {
                            textView.setTextColorFromResource(R.color.color_primary_text)
                            textView.background = null
                            dotView.isVisible = viewModel.hasDate(day.date)
                        }
                    }
                } else {
                    textView.setTextColorFromResource(R.color.color_secondary_text)
                    dotView.isVisible = false
                }
            }
        }

        calendarView.monthHeaderBinder = object: MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View): MonthViewContainer = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                val layout = container.daysOfWeekLayout
                if (layout.tag == null) {
                    layout.tag = month.yearMonth
                    layout.children.map { it as TextView }.forEachIndexed { index, textView ->
                        textView.text = daysOfWeek[index].name.first().toString()
                        //textView.setTextColorFromResource()
                    }
                }
            }
        }

        calendarView.monthScrollListener = {
            selectDate(it.yearMonth.atDay(1))
        }
    }

    override fun onResume() {
        super.onResume()

        actionButton.setOnClickListener {
            startActivityForResult(Intent(context, EventEditor::class.java),
                EventEditor.REQUEST_CODE_INSERT)
        }
    }

    override fun <T> onActionPerformed(t: T, action: BaseListAdapter.ActionListener.Action,
                                       views: Map<String, View>) {
        if (t is EventPackage) {
            when (action) {
                // Show up the editorUI and pass the extra
                BaseListAdapter.ActionListener.Action.SELECT -> {
                    val intent = Intent(context, EventEditor::class.java).apply {
                        putExtra(EventEditor.EXTRA_EVENT, t.event)
                        putExtra(EventEditor.EXTRA_SUBJECT, t.subject)
                    }
                    startActivityWithTransition(views, intent, EventEditor.REQUEST_CODE_UPDATE)
                }
                // Item has been swiped, notify database for deletion
                BaseListAdapter.ActionListener.Action.DELETE -> {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_event, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_previous -> {
                startActivity(Intent(context, PreviousEventsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun selectDate(date: LocalDate) {
        if (viewModel.selectedDate != date) {
            val oldDate = viewModel.selectedDate

            viewModel.selectedDate = date
            calendarView.notifyDateChanged(oldDate)
            calendarView.notifyDateChanged(date)
        }
        currentDateTextView.text = selectionFormatter.format(date)
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
