package com.isaiahvonrundstedt.fokus.features.subject

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import kotlinx.android.synthetic.main.layout_sheet_subject.*
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class SubjectBottomSheet(): BaseBottomSheet() {

    constructor(dismissListener: DismissListener): this() {
        this.dismissListener = dismissListener
    }

    constructor(subject: Subject, dismissListener: DismissListener): this(dismissListener) {
        this.subject = subject
        this.dismissListener = dismissListener
        this.mode = modeUpdate
    }

    private var subject = Subject()
    private var values: IntArray? = null
    private var selectedIndices: IntArray = emptyArray<Int>().toIntArray()
    private var colors: IntArray? = null
    private var mode: Int = modeInsert
    private var dismissListener: DismissListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        values = resources.getIntArray(R.array.days_of_week_values)
        colors = Subject.Tag.getColors()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_subject, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mode == modeUpdate) {
            codeEditText.setText(subject.code)
            descriptionEditText.setText(subject.description)
            startTimeTextView.text = formatTime(subject.startTime!!)
            endTimeTextView.text = formatTime(subject.endTime!!)

            val builder = StringBuilder()
            val selectedDays = Subject.getDays(subject.daysOfWeek)

            selectedDays.forEachIndexed { index, dayOfWeek ->
                builder.append(getString(Subject.getDayNameResource(dayOfWeek)))

                if (index == selectedDays.size - 2)
                    builder.append(getString(R.string.and))
                else if (index < selectedDays.size - 2)
                    builder.append(", ")
            }
            daysOfWeekTextView.text = builder.toString()

            this@SubjectBottomSheet.tagHolderView
                .setImageDrawable(subject.tintDrawable(this@SubjectBottomSheet.tagHolderView.drawable))
            tagView.text = getString(Subject.Tag.getName(subject.tag))
        }

        daysOfWeekTextView.setOnClickListener {
            MaterialDialog(it.context).show {
                lifecycleOwner(this@SubjectBottomSheet)
                title(R.string.days_of_week_dialog_title)
                listItemsMultiChoice(R.array.days_of_week_items, initialSelection = selectedIndices)
                        { _, indices, items ->
                    selectedIndices = indices
                    var sum = 0
                    indices.forEach { index ->
                        sum += values!![index]
                    }
                    subject.daysOfWeek = sum
                    val builder = StringBuilder("")
                    items.forEachIndexed { index, charSequence ->
                        builder.append(charSequence)
                        if (index == items.size - 2)
                            builder.append(getString(R.string.and))
                        else if (index < items.size - 2)
                            builder.append(", ")
                    }
                    (it as AppCompatTextView).text = builder
                }
                positiveButton(R.string.button_done)
            }
        }

        startTimeTextView.setOnClickListener { v ->
            MaterialDialog(v.context).show {
                lifecycleOwner(this@SubjectBottomSheet)
                title(R.string.dialog_select_start_time)
                timePicker(show24HoursView = false,
                    currentTime = subject.startTime?.toDateTimeToday()?.toCalendar(Locale.getDefault())) { _, time ->
                    val startTime = LocalTime.fromCalendarFields(time)

                    subject.startTime = startTime
                    if (subject.endTime == null) subject.endTime = startTime
                    if (startTime.isBefore(subject.endTime)) {
                        subject.endTime = subject.startTime?.plusHours(1)?.plusMinutes(30)
                        this@SubjectBottomSheet.endTimeTextView.text = formatTime(subject.endTime!!)
                    }
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) v.text = formatTime(subject.startTime!!)
                }
            }
        }

        endTimeTextView.setOnClickListener { v ->
            MaterialDialog(v.context).show {
                lifecycleOwner(this@SubjectBottomSheet)
                title(R.string.dialog_select_end_time)
                timePicker(show24HoursView = false,
                    currentTime = subject.endTime?.toDateTimeToday()?.toCalendar(Locale.getDefault())) { _, time ->
                    val endTime = LocalTime.fromCalendarFields(time)

                    subject.endTime = endTime
                    if (subject.startTime == null) subject.startTime = endTime
                    if (endTime.isBefore(subject.startTime)) {
                        subject.startTime = subject.endTime?.minusHours(1)?.minusMinutes(30)
                        this@SubjectBottomSheet.startTimeTextView.text = formatTime(subject.startTime!!)
                    }
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) v.text = formatTime(subject.endTime!!)
                }
            }
        }

        tagView.setOnClickListener { v ->
            MaterialDialog(v.context).show {
                lifecycleOwner(this@SubjectBottomSheet)
                title(R.string.dialog_select_color_tag)
                colorChooser(colors!!) { _, color ->
                    subject.tag = Subject.Tag.convertColorToTag(color)!!

                    this@SubjectBottomSheet.tagHolderView
                        .setImageDrawable(subject.tintDrawable(this@SubjectBottomSheet.tagHolderView.drawable))
                    if (v is AppCompatTextView)
                        v.text = getString(Subject.Tag.getName(subject.tag))
                }
            }
        }

        actionButton.setOnClickListener {

            if (codeEditText.text.isNullOrEmpty()) {
                showFeedback(bottomSheetView, R.string.feedback_subject_empty_name)
                codeEditText.requestFocus()
                return@setOnClickListener
            }

            if (descriptionEditText.text.isNullOrEmpty()) {
                showFeedback(bottomSheetView, R.string.feedback_subject_empty_description)
                descriptionEditText.requestFocus()
                return@setOnClickListener
            }

            if (subject.startTime == null) {
                showFeedback(bottomSheetView, R.string.feedback_subject_empty_start_time)
                startTimeTextView.performClick()
                return@setOnClickListener
            }

            if (subject.endTime == null) {
                showFeedback(bottomSheetView, R.string.feedback_subject_empty_end_time)
                endTimeTextView.performClick()
                return@setOnClickListener
            }

            subject.code = codeEditText.text.toString()
            subject.description = descriptionEditText.text.toString()
            status = statusCommit

            this.dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss(status, mode, subject)
    }

    private fun formatTime(time: LocalTime): String {
        return DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(time)
    }
}