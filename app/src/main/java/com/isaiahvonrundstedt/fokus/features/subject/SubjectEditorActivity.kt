package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.AppCompatTextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.components.converter.DateTimeConverter
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_subject.*
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class SubjectEditorActivity: BaseActivity() {

    private var requestCode = 0
    private var subject = Subject()
    private var values: IntArray? = null
    private var selectedIndices: IntArray = emptyArray<Int>().toIntArray()
    private var colors: IntArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_subject)
        setPersistentActionBar(toolbar)

        requestCode = if (intent.hasExtra(extraSubject)) updateRequestCode else insertRequestCode
        if (requestCode == updateRequestCode)
            subject = intent.getParcelableExtra(extraSubject)!!

        values = resources.getIntArray(R.array.days_of_week_values)
        colors = Subject.Tag.getColors()
    }

    override fun onStart() {
        super.onStart()

        if (requestCode == updateRequestCode) {
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

            this@SubjectEditorActivity.tagHolderView
                .setImageDrawable(subject.tintDrawable(this@SubjectEditorActivity.tagHolderView.drawable))
            tagView.text = getString(Subject.Tag.getName(subject.tag))
        }

        daysOfWeekTextView.setOnClickListener {
            MaterialDialog(it.context).show {
                lifecycleOwner(this@SubjectEditorActivity)
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
                lifecycleOwner(this@SubjectEditorActivity)
                title(R.string.dialog_select_start_time)
                timePicker(show24HoursView = false,
                    currentTime = subject.startTime?.toDateTimeToday()?.toCalendar(Locale.getDefault())) { _, time ->
                    val startTime = LocalTime.fromCalendarFields(time)

                    subject.startTime = startTime
                    if (subject.endTime == null) subject.endTime = startTime
                    if (startTime.isAfter(subject.endTime) || startTime.isEqual(subject.endTime)) {
                        subject.endTime = subject.startTime?.plusHours(1)?.plusMinutes(30)
                        this@SubjectEditorActivity.endTimeTextView.text = formatTime(subject.endTime!!)
                    }
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) v.text = formatTime(subject.startTime!!)
                }
            }
        }

        endTimeTextView.setOnClickListener { v ->
            MaterialDialog(v.context).show {
                lifecycleOwner(this@SubjectEditorActivity)
                title(R.string.dialog_select_end_time)
                timePicker(show24HoursView = false,
                    currentTime = subject.endTime?.toDateTimeToday()?.toCalendar(Locale.getDefault())) { _, time ->
                    val endTime = LocalTime.fromCalendarFields(time)

                    subject.endTime = endTime
                    if (subject.startTime == null) subject.startTime = endTime
                    if (endTime.isBefore(subject.startTime) || endTime.isEqual(subject.startTime)) {
                        subject.startTime = subject.endTime?.minusHours(1)?.minusMinutes(30)
                        this@SubjectEditorActivity.startTimeTextView.text = formatTime(subject.startTime!!)
                    }
                }
                positiveButton(R.string.button_done) {
                    if (v is AppCompatTextView) v.text = formatTime(subject.endTime!!)
                }
            }
        }

        tagView.setOnClickListener { v ->
            MaterialDialog(v.context).show {
                lifecycleOwner(this@SubjectEditorActivity)
                title(R.string.dialog_select_color_tag)
                colorChooser(colors!!) { _, color ->
                    subject.tag = Subject.Tag.convertColorToTag(color)!!

                    this@SubjectEditorActivity.tagHolderView
                        .setImageDrawable(subject.tintDrawable(this@SubjectEditorActivity.tagHolderView.drawable))
                    if (v is AppCompatTextView)
                        v.text = getString(Subject.Tag.getName(subject.tag))
                }
            }
        }

        actionButton.setOnClickListener {
            if (codeEditText.text.isNullOrEmpty()) {
                showFeedback(window.decorView.rootView, R.string.feedback_subject_empty_name)
                codeEditText.requestFocus()
                return@setOnClickListener
            }

            if (descriptionEditText.text.isNullOrEmpty()) {
                showFeedback(window.decorView.rootView, R.string.feedback_subject_empty_description)
                descriptionEditText.requestFocus()
                return@setOnClickListener
            }

            if (subject.startTime == null) {
                showFeedback(window.decorView.rootView, R.string.feedback_subject_empty_start_time)
                startTimeTextView.performClick()
                return@setOnClickListener
            }

            if (subject.endTime == null) {
                showFeedback(window.decorView.rootView,
                    R.string.feedback_subject_empty_end_time)
                endTimeTextView.performClick()
                return@setOnClickListener
            }

            subject.code = codeEditText.text.toString()
            subject.description = descriptionEditText.text.toString()

            val data = Intent()
            data.putExtra(extraSubject, subject)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    private fun formatTime(time: LocalTime): String {
        return DateTimeFormat.forPattern(DateTimeConverter.timeFormat).print(time)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    companion object {
        const val insertRequestCode = 27
        const val updateRequestCode = 13
        const val extraSubject = "extraSubject"
    }
}