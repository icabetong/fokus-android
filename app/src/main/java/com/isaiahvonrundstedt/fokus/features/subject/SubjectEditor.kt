package com.isaiahvonrundstedt.fokus.features.subject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat.setTransitionName
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.extensions.getCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.features.core.extensions.setCompoundDrawableAtStart
import com.isaiahvonrundstedt.fokus.features.core.extensions.setTextColorFromResource
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseEditor
import kotlinx.android.synthetic.main.layout_appbar_editor.*
import kotlinx.android.synthetic.main.layout_editor_subject.*
import org.joda.time.LocalTime
import java.util.*

class SubjectEditor: BaseEditor() {

    private var requestCode = 0
    private var subject = Subject()
    private var values: IntArray? = null
    private var selectedIndices: IntArray = emptyArray<Int>().toIntArray()
    private var colors: IntArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Check if the parent activity have passed some extras
        requestCode = if (intent.hasExtra(extraSubject)) updateRequestCode else insertRequestCode

        if (requestCode == insertRequestCode)
            findViewById<View>(android.R.id.content).transitionName = transition

        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_editor_subject)
        setPersistentActionBar(toolbar)

        if (requestCode == updateRequestCode) {
            subject = intent.getParcelableExtra(extraSubject)!!

            setTransitionName(codeEditText, SubjectAdapter.transitionCodeID + subject.id)
            setTransitionName(descriptionEditText, SubjectAdapter.transitionDescriptionID + subject.id)
        }

        // Get actual values for the items
        values = resources.getIntArray(R.array.days_of_week_values)
        colors = Subject.Tag.getColors()
    }

    override fun onStart() {
        super.onStart()

        daysOfWeekTextView.setOnClickListener { it ->
            MaterialDialog(it.context, BottomSheet()).show {
                lifecycleOwner(this@SubjectEditor)
                title(R.string.days_of_week_dialog_title)
                listItemsMultiChoice(R.array.days_of_week_items, initialSelection = selectedIndices)
                        { _, indices, _ ->
                    selectedIndices = indices
                    var sum = 0
                    indices.forEach { index ->
                        sum += values!![index]
                    }
                    subject.daysOfWeek = sum
                    (it as AppCompatTextView).text = subject.formatDaysOfWeek(this@SubjectEditor)
                }
                positiveButton(R.string.button_done) { _ ->
                    if (it is AppCompatTextView) {
                        it.setTextColorFromResource(R.color.colorPrimaryText)
                    }
                }
            }
        }

        startTimeTextView.setOnClickListener {
            MaterialDialog(it.context).show {
                lifecycleOwner(this@SubjectEditor)
                title(R.string.dialog_select_start_time)
                timePicker(show24HoursView = false,
                    currentTime = subject.startTime?.toDateTimeToday()?.toCalendar(Locale.getDefault())) { _, time ->
                    val startTime = LocalTime.fromCalendarFields(time)

                    subject.startTime = startTime
                    if (subject.endTime == null) subject.endTime = startTime
                    if (startTime.isAfter(subject.endTime) || startTime.isEqual(subject.endTime)) {
                        subject.endTime = subject.startTime?.plusHours(1)?.plusMinutes(30)
                        this@SubjectEditor.endTimeTextView.text = subject.formatEndTime()
                    }
                }
                positiveButton(R.string.button_done) { _ ->
                    if (it is AppCompatTextView) {
                        it.text = subject.formatStartTime()
                        it.setTextColorFromResource(R.color.colorPrimaryText)
                    }
                }
            }
        }

        endTimeTextView.setOnClickListener {
            MaterialDialog(it.context).show {
                lifecycleOwner(this@SubjectEditor)
                title(R.string.dialog_select_end_time)
                timePicker(show24HoursView = false,
                    currentTime = subject.endTime?.toDateTimeToday()?.toCalendar(Locale.getDefault())) { _, time ->
                    val endTime = LocalTime.fromCalendarFields(time)

                    subject.endTime = endTime
                    if (subject.startTime == null) subject.startTime = endTime
                    if (endTime.isBefore(subject.startTime) || endTime.isEqual(subject.startTime)) {
                        subject.startTime = subject.endTime?.minusHours(1)?.minusMinutes(30)
                        this@SubjectEditor.startTimeTextView.text = subject.formatStartTime()
                    }
                }
                positiveButton(R.string.button_done) { _ ->
                    if (it is AppCompatTextView) {
                        it.text = subject.formatEndTime()
                        it.setTextColorFromResource(R.color.colorPrimaryText)
                    }
                }
            }
        }

        tagView.setOnClickListener {
            MaterialDialog(it.context, BottomSheet()).show {
                lifecycleOwner(this@SubjectEditor)
                title(R.string.dialog_select_color_tag)
                colorChooser(colors!!) { _, color ->
                    subject.tag = Subject.Tag.convertColorToTag(color)!!

                    val tagDrawable = this@SubjectEditor.tagView
                        .getCompoundDrawableAtStart()?.let { drawable ->
                            subject.tintDrawable(drawable)
                        }
                    this@SubjectEditor.tagView.setCompoundDrawableAtStart(tagDrawable)
                    if (it is AppCompatTextView) {
                        it.text = getString(subject.tag.getNameResource())
                        it.setTextColorFromResource(R.color.colorPrimaryText)
                    }
                }
            }
        }

        actionButton.setOnClickListener {

            // This ifs is used to check if some fields are
            // blank or null, if these returned true,
            // we'll show a Snackbar then direct the focus to
            // the corresponding field then return to stop
            // the execution of the code
            if (codeEditText.text.isNullOrEmpty()) {
                createSnackbar(rootLayout, R.string.feedback_subject_empty_name).show()
                codeEditText.requestFocus()
                return@setOnClickListener
            }

            if (descriptionEditText.text.isNullOrEmpty()) {
                createSnackbar(rootLayout, R.string.feedback_subject_empty_description).show()
                descriptionEditText.requestFocus()
                return@setOnClickListener
            }

            if (subject.startTime == null) {
                createSnackbar(rootLayout, R.string.feedback_subject_empty_start_time).show()
                startTimeTextView.performClick()
                return@setOnClickListener
            }

            if (subject.endTime == null) {
                createSnackbar(rootLayout, R.string.feedback_subject_empty_end_time).show()
                endTimeTextView.performClick()
                return@setOnClickListener
            }

            subject.code = codeEditText.text.toString()
            subject.description = descriptionEditText.text.toString()

            // Pass the intent to the parent activity
            val data = Intent()
            data.putExtra(extraSubject, subject)
            setResult(Activity.RESULT_OK, data)
            supportFinishAfterTransition()
        }
    }

    override fun onResume() {
        super.onResume()


        // The extras passed by the parent activity will
        // be shown to the fields.
        if (requestCode == updateRequestCode) {

            with(subject) {
                codeEditText.setText(code)
                descriptionEditText.setText(description)
                startTimeTextView.text = formatStartTime()
                endTimeTextView.text = formatEndTime()
                daysOfWeekTextView.text = formatDaysOfWeek(this@SubjectEditor)
                tagView.setCompoundDrawableAtStart(tagView.getCompoundDrawableAtStart()
                    ?.let {
                        drawable -> tintDrawable(drawable)
                    })
                tagView.setText(tag.getNameResource())
            }

            startTimeTextView.setTextColorFromResource(R.color.colorPrimaryText)
            endTimeTextView.setTextColorFromResource(R.color.colorPrimaryText)
            daysOfWeekTextView.setTextColorFromResource(R.color.colorPrimaryText)
            tagView.setTextColorFromResource(R.color.colorPrimaryText)

            window.decorView.rootView.clearFocus()
        }
    }

    companion object {
        const val insertRequestCode = 27
        const val updateRequestCode = 13
        const val extraSubject = "extraSubject"
    }
}