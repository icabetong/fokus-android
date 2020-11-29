package com.isaiahvonrundstedt.fokus.features.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.forEach
import androidx.fragment.app.FragmentManager
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
import com.isaiahvonrundstedt.fokus.databinding.LayoutSheetScheduleEditorBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import java.time.DayOfWeek

class ScheduleEditor(manager: FragmentManager) : BaseBottomSheet<Schedule>(manager) {

    private var schedule: Schedule = Schedule()
    private var requestKey: String = REQUEST_CODE_INSERT
    private var _binding: LayoutSheetScheduleEditorBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = LayoutSheetScheduleEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.also {
            schedule.subject = it.getString(EXTRA_SUBJECT_ID)

            it.getParcelable<Schedule>(EXTRA_SCHEDULE)?.also { schedule ->
                this.schedule = schedule
                requestKey = REQUEST_CODE_UPDATE

                binding.startTimeTextView.text = schedule.formatStartTime(binding.root.context)
                binding.endTimeTextView.text = schedule.formatEndTime(binding.root.context)

                binding.startTimeTextView.setTextColorFromResource(R.color.color_primary_text)
                binding.endTimeTextView.setTextColorFromResource(R.color.color_primary_text)

                binding.weekOneChip.isChecked = schedule.hasWeek(Schedule.BIT_VALUE_WEEK_ONE)
                binding.weekTwoChip.isChecked = schedule.hasWeek(Schedule.BIT_VALUE_WEEK_TWO)
                binding.weekThreeChip.isChecked = schedule.hasWeek(Schedule.BIT_VALUE_WEEK_THREE)
                binding.weekFourChip.isChecked = schedule.hasWeek(Schedule.BIT_VALUE_WEEK_FOUR)

                schedule.getDays().forEach { day ->
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
                timePicker(show24HoursView = false,
                    currentTime = schedule.startTime?.toZonedDateTimeToday()?.toCalendar()) { _, time ->
                    val startTime = time.toLocalTime()

                    schedule.startTime = startTime
                    if (schedule.endTime == null) schedule.endTime = startTime

                    if (startTime.isAfter(schedule.endTime) || startTime.compareTo(schedule.endTime) == 0) {
                        schedule.endTime = schedule.startTime?.plusHours(1)
                            ?.plusMinutes(30)
                        binding.endTimeTextView.text = schedule.formatEndTime(it.context)
                    }
                }
                positiveButton(R.string.button_done) { _ ->
                    if (it is AppCompatTextView) {
                        it.text = schedule.formatStartTime(it.context)
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
                timePicker(show24HoursView = false,
                    currentTime = schedule.endTime?.toZonedDateTimeToday()?.toCalendar()) { _, time ->
                    val endTime = time.toLocalTime()

                    schedule.endTime = endTime
                    if (schedule.startTime == null) schedule.startTime = endTime

                    if (endTime.isBefore(schedule.startTime) || endTime.compareTo(schedule.startTime) == 0) {
                        schedule.startTime = schedule.endTime?.minusHours(1)
                            ?.minusMinutes(30)
                        binding.startTimeTextView.text = schedule.formatStartTime(it.context)
                    }
                }
                positiveButton(R.string.button_done) { _ ->
                    if (it is AppCompatTextView) {
                        it.text = schedule.formatEndTime(it.context)
                        it.setTextColorFromResource(R.color.color_primary_text)
                        binding.startTimeTextView.setTextColorFromResource(R.color.color_primary_text)
                    }
                }
            }
        }

        binding.actionButton.setOnClickListener {

            binding.daysOfWeekGroup.forEach {
                if ((it as? Chip)?.isChecked == true) {
                    schedule.daysOfWeek += when (it.id) {
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
                    schedule.weeksOfMonth += when (it.id) {
                        R.id.weekOneChip -> Schedule.BIT_VALUE_WEEK_ONE
                        R.id.weekTwoChip -> Schedule.BIT_VALUE_WEEK_TWO
                        R.id.weekThreeChip -> Schedule.BIT_VALUE_WEEK_THREE
                        R.id.weekFourChip -> Schedule.BIT_VALUE_WEEK_FOUR
                        else -> 0
                    }
                }
            }

            // This ifs is used to check if some fields are
            // blank or null, if these returned true,
            // we'll show a Snackbar then direct the focus to
            // the corresponding field then return to stop
            // the execution of the code
            if (schedule.daysOfWeek == 0) {
                createToast(R.string.feedback_schedule_empty_days)
                return@setOnClickListener
            }

            if (schedule.startTime == null) {
                createToast(R.string.feedback_schedule_empty_start_time)
                binding.startTimeTextView.performClick()
                return@setOnClickListener
            }

            if (schedule.endTime == null) {
                createToast(R.string.feedback_schedule_empty_end_time)
                binding.endTimeTextView.performClick()
                return@setOnClickListener
            }

            receiver?.onReceive(schedule)
            this.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val REQUEST_CODE_INSERT = "request:insert"
        const val REQUEST_CODE_UPDATE = "request:update"
        const val EXTRA_SCHEDULE = "extra:schedule"
        const val EXTRA_SUBJECT_ID = "extra:subject:id"
    }
}