package com.isaiahvonrundstedt.fokus.features.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.MaterialColors
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.custom.ItemDecoration
import com.isaiahvonrundstedt.fokus.components.custom.ItemSwipeCallback
import com.isaiahvonrundstedt.fokus.components.extensions.android.createSnackbar
import com.isaiahvonrundstedt.fokus.components.extensions.android.isDark
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.databinding.FragmentEventBinding
import com.isaiahvonrundstedt.fokus.databinding.LayoutCalendarDayBinding
import com.isaiahvonrundstedt.fokus.features.event.editor.EventEditorFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseAdapter
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import me.saket.cascade.overrideOverflowMenu
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*


@AndroidEntryPoint
class EventFragment : BaseFragment(), BaseAdapter.ActionListener, BaseAdapter.ArchiveListener {

    private var daysOfWeek: Array<DayOfWeek> = emptyArray()
    private var _binding: FragmentEventBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val eventAdapter = EventAdapter(this, this)
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    private val viewModel: EventViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.actionButton.transitionName = TRANSITION_ELEMENT_ROOT
        setInsets(binding.root, binding.appBarLayout.toolbar, arrayOf(binding.containerLayout),
            binding.actionButton)

        with(binding.appBarLayout.toolbar) {
            title = viewModel.currentMonth.format(monthYearFormatter)
            menu?.clear()
            inflateMenu(R.menu.menu_events)
            overrideOverflowMenu(::customPopupProvider)
            setOnMenuItemClickListener(::onMenuItemClicked)
            setupNavigation(this)
        }

        with(binding.recyclerView) {
            addItemDecoration(ItemDecoration(context))
            layoutManager = LinearLayoutManager(context)
            adapter = eventAdapter

            ItemTouchHelper(ItemSwipeCallback(context, eventAdapter))
                .attachToRecyclerView(this)
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        daysOfWeek = daysOfWeekFromLocale()
        binding.calendarView.apply {
            setup(
                viewModel.startMonth, viewModel.endMonth,
                daysOfWeek.first()
            )
            scrollToMonth(viewModel.currentMonth)
        }

        if (savedInstanceState == null)
            binding.calendarView.post { setCurrentDate(viewModel.today) }
    }

    override fun onStart() {
        super.onStart()

        /**
         * Get the NavController here so
         * that it doesn't crash when
         * the host activity is recreated.
         */
        controller = Navigation.findNavController(requireActivity(), R.id.navigationHostFragment)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay

            val textView: TextView = LayoutCalendarDayBinding.bind(view).calendarDayView
            val dotView: View = LayoutCalendarDayBinding.bind(view).calendarDotView

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH)
                        setCurrentDate(day.date)
                }
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            eventAdapter.submitList(it)
        }
        viewModel.eventsEmpty.observe(viewLifecycleOwner) {
            binding.emptyView.isVisible = it
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val headerLayout: LinearLayout = view.findViewById(R.id.headerLayout)
        }

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                bindToCalendar(day, container.textView, container.dotView)
            }
        }

        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View): MonthViewContainer = MonthViewContainer(view)

                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    val headerLayout = container.headerLayout
                    if (container.headerLayout.tag == null) {
                        headerLayout.tag = month.yearMonth
                        headerLayout.children.map { it as TextView }
                            .forEachIndexed { index, textView ->
                                textView.text = daysOfWeek[index].name.first().toString()
                            }
                    }
                }
            }

        binding.calendarView.monthScrollListener = {
            setCurrentDate(it.yearMonth.atDay(1))
            binding.appBarLayout.toolbar.title = it.yearMonth.format(monthYearFormatter)

            // Check if the user is nearing the end of the month list.
            // Then continually add more months so that the user
            // can scroll infinitely.
            if (it.yearMonth.minusMonths(2) == viewModel.startMonth) {
                // The user is two months away from the starting month in the CalendarView
                // we'll need to add more months at the start
                viewModel.startMonth = viewModel.startMonth.minusMonths(2)
                binding.calendarView.updateMonthRange(startMonth = viewModel.startMonth)

            } else if (it.yearMonth.plusMonths(2) == viewModel.endMonth) {
                // The user is two months away from the ending month in the CalendarView
                // we'll need to add more months at the end
                viewModel.endMonth = viewModel.endMonth.plusMonths(2)
                binding.calendarView.updateMonthRange(endMonth = viewModel.endMonth)
            }
        }

        // Observe dates with events then rebind the
        // dayBinder to the Calendar.
        viewModel.dates.observe(viewLifecycleOwner) { dates ->
            binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
                override fun create(view: View): DayViewContainer {
                    return DayViewContainer(view)
                }

                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    container.day = day
                    bindToCalendar(day, container.textView, container.dotView, dates)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.calendarView.scrollToMonth(viewModel.currentMonth)
        binding.calendarView.scrollToDate(viewModel.today)
        setCurrentDate(viewModel.today)

        binding.actionButton.setOnClickListener {
            it.transitionName = TRANSITION_ELEMENT_ROOT

            controller?.navigate(
                R.id.navigation_editor_event, null, null,
                FragmentNavigatorExtras(it to TRANSITION_ELEMENT_ROOT)
            )
        }
    }

    override fun <T> onItemArchive(t: T) {
        if (t is EventPackage) {
            t.event.isEventArchived = true
            viewModel.update(t.event)
        }
    }

    override fun <T> onActionPerformed(
        t: T, action: BaseAdapter.ActionListener.Action,
        container: View?
    ) {
        if (t is EventPackage) {
            when (action) {
                // Show up the editorUI and pass the extra
                BaseAdapter.ActionListener.Action.SELECT -> {
                    val transitionName = TRANSITION_ELEMENT_ROOT + t.event.eventID

                    val args = bundleOf(
                        EventEditorFragment.EXTRA_EVENT to Event.toBundle(t.event),
                        EventEditorFragment.EXTRA_SUBJECT to t.subject?.let { Subject.toBundle(it) }
                    )

                    controller?.navigate(
                        R.id.navigation_editor_event, args, null,
                        FragmentNavigatorExtras(container!! to transitionName)
                    )
                }
                // Item has been swiped, notify database for deletion
                BaseAdapter.ActionListener.Action.DELETE -> {
                    viewModel.remove(t.event)

                    createSnackbar(R.string.feedback_event_removed, binding.recyclerView).run {
                        setAction(R.string.button_undo) { viewModel.insert(t.event) }
                    }
                }
            }
        }
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_archived -> {
                controller?.navigate(R.id.navigation_archived_event)
            }
        }
        return true
    }

    private fun bindToCalendar(
        day: CalendarDay, textView: TextView, view: View,
        dates: List<LocalDate> = emptyList()
    ) {

        textView.text = day.date.dayOfMonth.toString()
        if (day.owner == DayOwner.THIS_MONTH) {
            when (day.date) {
                viewModel.today -> {
                    val color = if (requireContext().isDark()) android.R.color.system_accent1_50
                        else android.R.color.system_accent1_500
                    textView.setTextColorFromResource(color)
                    textView.setBackgroundResource(R.drawable.shape_calendar_current_day)
                    view.isVisible = false
                }
                viewModel.selectedDate -> {
                    textView.setTextColorFromResource(R.color.theme_on_primary_container)
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
            binding.calendarView.notifyDateChanged(oldDate)
            binding.calendarView.notifyDateChanged(date)
        }
        binding.currentDateTextView.text = date.format(dateFormatter)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
