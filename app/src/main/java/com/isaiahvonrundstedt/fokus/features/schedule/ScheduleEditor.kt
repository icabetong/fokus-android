package com.isaiahvonrundstedt.fokus.features.schedule

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.createToast
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toCalendar
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toLocalTime
import com.isaiahvonrundstedt.fokus.components.extensions.jdk.toZonedDateTimeToday
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.databinding.LayoutSheetScheduleEditorBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.*

class ScheduleEditor(manager: FragmentManager) : BaseBottomSheet(manager) {

    private var id: String? = null
    private var startTime: LocalTime? = null
    private var endTime: LocalTime? = null
    private var daysOfWeek: Int = 0
    private var weeksOfMonth: Int = 0
    private var subjectID: String? = null

    private var requestKey: String = REQUEST_KEY_INSERT
    private var _binding: LayoutSheetScheduleEditorBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LayoutSheetScheduleEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!PreferenceManager(requireContext()).allowWeekNumbers) {
            binding.weekOfMonthGroup.isVisible = false
            binding.weekNumbersHeader.isVisible = false
        }

        arguments?.also {
            subjectID = it.getString(EXTRA_SUBJECT_ID)

            it.getParcelable<Schedule>(EXTRA_SCHEDULE)?.also { schedule ->
                id = schedule.scheduleID
                startTime = schedule.startTime
                endTime = schedule.endTime
                daysOfWeek = schedule.daysOfWeek
                weeksOfMonth = schedule.weeksOfMonth
                subjectID = schedule.subject
                requestKey = REQUEST_KEY_UPDATE

                binding.startTimeTextView.text = Schedule.formatTime(view.context, startTime)
                binding.endTimeTextView.text = Schedule.formatTime(view.context, endTime)

                binding.startTimeTextView.setTextColorFromResource(R.color.color_primary_text)
                binding.endTimeTextView.setTextColorFromResource(R.color.color_primary_text)

                binding.weekOneChip.isChecked = schedule.hasWeek(Schedule.BIT_VALUE_WEEK_ONE)
                binding.weekTwoChip.isChecked = schedule.hasWeek(Schedule.BIT_VALUE_WEEK_TWO)
                binding.weekThreeChip.isChecked = schedule.hasWeek(Schedule.BIT_VALUE_WEEK_THREE)
                binding.weekFourChip.isChecked = schedule.hasWeek(Schedule.BIT_VALUE_WEEK_FOUR)

                Schedule.parseDaysOfWeek(daysOfWeek).forEach { day ->
                    when (day) {
                        DayOfWeek.SUNDAY.value -> binding.sundayChip.isChecked = true
                        DayOfWeek.MONDAY.value -> binding.mondayChip.isChecked = true
                        DayOfWeek.TUESDAY.value -> binding.tuesdayChip.isChecked = true
                        DayOfWeek.WEDNESDAY.value -> binding.wednesdayChip.isChecked = true
                        DayOfWeek.THURSDAY.value -> binding.thursdayChip.isChecked = true
                        DayOfWeek.FRIDAY.value -> binding.fridayChip.isChecked = true
                        DayOfWeek.SATURDAY.value -> binding.saturdayChip.isChecked = true
                    }
                }
            }
        }

        binding.startTimeTextView.setOnClickListener {
            MaterialDialog(it.context).show {
                lifecycleOwner(this@ScheduleEditor)
                title(R.string.dialog_pick_start_time)
                timePicker(
                    show24HoursView = is24HourFormat(requireContext()),
                    currentTime = startTime?.toZonedDateTimeToday()?.toCalendar()
                ) { _, time ->
                    startTime = time.toLocalTime()
                    if (endTime == null)
                        endTime = startTime

                    if (startTime!!.isAfter(endTime)
                        || startTime!!.compareTo(endTime) == 0
                    ) {

                        endTime = startTime
                            ?.plusHours(1)
                            ?.plusMinutes(30)
                        binding.endTimeTextView.text = Schedule.formatTime(it.context, endTime)
                    }
                }
                positiveButton(R.string.button_done) { _ ->
                    if (it is AppCompatTextView) {
                        it.text = Schedule.formatTime(it.context, startTime)
                        it.setTextColorFromResource(R.color.color_primary_text)
                        binding.endTimeTextView.setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }


        binding.endTimeTextView.setOnClickListener {
            MaterialDialog(it.context).show {
                lifecycleOwner(this@ScheduleEditor)
                title(R.string.dialog_pick_end_time)
                timePicker(
                    show24HoursView = is24HourFormat(requireContext()),
                    currentTime = endTime?.toZonedDateTimeToday()?.toCalendar()
                ) { _, time ->
                    endTime = time.toLocalTime()

                    if (startTime == null)
                        startTime = endTime

                    if (endTime!!.isBefore(startTime)
                        || endTime!!.compareTo(startTime) == 0
                    ) {

                        startTime = endTime
                            ?.minusHours(1)
                            ?.minusMinutes(30)
                        binding.startTimeTextView.text = Schedule.formatTime(it.context, startTime)
                    }
                }
                positiveButton(R.string.button_done) { _ ->
                    if (it is AppCompatTextView) {
                        it.text = Schedule.formatTime(it.context, endTime)
                        it.setTextColorFromResource(R.color.color_primary_text)
                        binding.startTimeTextView.setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.actionButton.setOnClickListener {
            // reset the variables
            if (requestKey == REQUEST_KEY_UPDATE) {
                daysOfWeek = 0
                weeksOfMonth = 0
            }

            binding.daysOfWeekGroup.forEach {
                if ((it as? Chip)?.isChecked == true) {
                    daysOfWeek += when (it.id) {
                        R.id.sundayChip -> Schedule.BIT_VALUE_SUNDAY
                        R.id.mondayChip -> Schedule.BIT_VALUE_MONDAY
                        R.id.tuesdayChip -> Schedule.BIT_VALUE_TUESDAY
                        R.id.wednesdayChip -> Schedule.BIT_VALUE_WEDNESDAY
                        R.id.thursdayChip -> Schedule.BIT_VALUE_THURSDAY
                        R.id.fridayChip -> Schedule.BIT_VALUE_FRIDAY
                        R.id.saturdayChip -> Schedule.BIT_VALUE_SATURDAY
                        else -> 0
                    }
                }
            }

            binding.weekOfMonthGroup.forEach {
                if ((it as? Chip)?.isChecked == true) {
                    weeksOfMonth += when (it.id) {
                        R.id.weekOneChip -> Schedule.BIT_VALUE_WEEK_ONE
                        R.id.weekTwoChip -> Schedule.BIT_VALUE_WEEK_TWO
                        R.id.weekThreeChip -> Schedule.BIT_VALUE_WEEK_THREE
                        R.id.weekFourChip -> Schedule.BIT_VALUE_WEEK_FOUR
                        else -> 0
                    }
                }
            }

            // This conditions is used to check if some fields are
            // blank or null, if these returned true,
            // we'll show a Toast then direct the focus to
            // the corresponding field then return to stop
            // the execution of the code

            if (startTime == null) {
                createToast(R.string.feedback_schedule_empty_start_time)
                binding.startTimeTextView.performClick()
                return@setOnClickListener
            }

            if (endTime == null) {
                createToast(R.string.feedback_schedule_empty_end_time)
                binding.endTimeTextView.performClick()
                return@setOnClickListener
            }

            if (daysOfWeek == 0) {
                createToast(R.string.feedback_schedule_empty_days)
                return@setOnClickListener
            }

            if (weeksOfMonth == 0) {
                createToast(R.string.feedback_schedule_empty_days)
                return@setOnClickListener
            }

            val schedule = Schedule(
                scheduleID = id ?: UUID.randomUUID().toString(),
                daysOfWeek = daysOfWeek,
                weeksOfMonth = weeksOfMonth,
                startTime = startTime,
                endTime = endTime,
                subject = subjectID
            )

            setFragmentResult(requestKey, bundleOf(EXTRA_SCHEDULE to schedule))
            this.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY_INSERT = "request:insert"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_SCHEDULE = "extra:schedule"
        const val EXTRA_SUBJECT_ID = "extra:subject:id"
    }
}