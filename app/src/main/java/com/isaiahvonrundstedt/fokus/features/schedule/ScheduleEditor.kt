package com.isaiahvonrundstedt.fokus.features.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.forEach
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.chip.Chip
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import kotlinx.android.synthetic.main.layout_sheet_schedule.*
import kotlinx.android.synthetic.main.layout_sheet_schedule.actionButton
import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime
import java.util.*

class ScheduleEditor(private val dismissListener: DismissListener): BaseBottomSheet() {

    private var schedule: Schedule = Schedule()
    private var requestCode: Int = REQUEST_CODE_INSERT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.also {
            schedule.subject = it.getString(EXTRA_SUBJECT_ID)

            it.getParcelable<Schedule>(EXTRA_SCHEDULE)?.also { schedule ->
                this.schedule = schedule
                requestCode = REQUEST_CODE_UPDATE

                startTimeTextView.text = schedule.formatStartTime()
                endTimeTextView.text = schedule.formatEndTime()

                startTimeTextView.setTextColorFromResource(R.color.colorPrimaryText)
                endTimeTextView.setTextColorFromResource(R.color.colorPrimaryText)

                schedule.getDaysAsList().forEach { day ->
                    when (day) {
                        DateTimeConstants.SUNDAY -> sundayChip.isChecked = true
                        DateTimeConstants.MONDAY -> mondayChip.isChecked = true
                        DateTimeConstants.TUESDAY -> tuesdayChip.isChecked = true
                        DateTimeConstants.WEDNESDAY -> wednesdayChip.isChecked = true
                        DateTimeConstants.THURSDAY -> thursdayChip.isChecked = true
                        DateTimeConstants.FRIDAY -> fridayChip.isChecked = true
                        DateTimeConstants.SATURDAY -> saturdayChip.isChecked = true
                    }
                }
            }
        }

        startTimeTextView.setOnClickListener {
            MaterialDialog(it.context).show {
                lifecycleOwner(this@ScheduleEditor)
                title(R.string.dialog_select_start_time)
                timePicker(show24HoursView = false,
                    currentTime = schedule.startTime?.toDateTimeToday()?.toCalendar(Locale.getDefault())) { _, time ->
                    val startTime = LocalTime.fromCalendarFields(time)

                    schedule.startTime = startTime
                    if (schedule.endTime == null) schedule.endTime = startTime
                    if (startTime.isAfter(schedule.endTime) || startTime.isEqual(schedule.endTime)) {
                        schedule.endTime = schedule.startTime?.plusHours(1)?.plusMinutes(30)
                        this@ScheduleEditor.endTimeTextView.text = schedule.formatEndTime()
                    }
                }
                positiveButton(R.string.button_done) { _ ->
                    if (it is AppCompatTextView) {
                        it.text = schedule.formatStartTime()
                        it.setTextColorFromResource(R.color.colorPrimaryText)
                        this@ScheduleEditor.endTimeTextView.setTextColorFromResource(R.color.colorPrimaryText)
                    }
                }
            }
        }


        endTimeTextView.setOnClickListener {
            MaterialDialog(it.context).show {
                lifecycleOwner(this@ScheduleEditor)
                title(R.string.dialog_select_end_time)
                timePicker(show24HoursView = false,
                    currentTime = schedule.endTime?.toDateTimeToday()?.toCalendar(
                        Locale.getDefault())) { _, time ->
                    val endTime = LocalTime.fromCalendarFields(time)

                    schedule.endTime = endTime
                    if (schedule.startTime == null) schedule.startTime = endTime
                    if (endTime.isBefore(schedule.startTime) || endTime.isEqual(schedule.startTime)) {
                        schedule.startTime = schedule.endTime?.minusHours(1)?.minusMinutes(30)
                        this@ScheduleEditor.startTimeTextView.text = schedule.formatStartTime()
                    }
                }
                positiveButton(R.string.button_done) { _ ->
                    if (it is AppCompatTextView) {
                        it.text = schedule.formatEndTime()
                        it.setTextColorFromResource(R.color.colorPrimaryText)
                        this@ScheduleEditor.startTimeTextView.setTextColorFromResource(R.color.colorPrimaryText)
                    }
                }
            }
        }

        actionButton.setOnClickListener {
            schedule.daysOfWeek = 0
            daysOfWeekGroup.forEach {
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

            // This ifs is used to check if some fields are
            // blank or null, if these returned true,
            // we'll show a Snackbar then direct the focus to
            // the corresponding field then return to stop
            // the execution of the code
            if (schedule.daysOfWeek == 0) {
                showFeedback(R.string.feedback_schedule_empty_days).show()
                return@setOnClickListener
            }

            if (schedule.startTime == null) {
                showFeedback(R.string.feedback_schedule_empty_start_time).show()
                startTimeTextView.performClick()
                return@setOnClickListener
            }

            if (schedule.endTime == null) {
                showFeedback(R.string.feedback_schedule_empty_end_time).show()
                endTimeTextView.performClick()
                return@setOnClickListener
            }

            setResult(schedule, requestCode)
        }
    }

    private fun <T> setResult(t: T, requestCode: Int) {
        dismissListener.onDismiss(t, requestCode)
        dismiss()
    }

    companion object {
        const val REQUEST_CODE_INSERT = 43
        const val REQUEST_CODE_UPDATE = 89
        const val EXTRA_SCHEDULE = "extra:schedule"
        const val EXTRA_SUBJECT_ID = "extra:subject:id"
    }
}