package com.isaiahvonrundstedt.fokus.components.views

import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.isaiahvonrundstedt.fokus.R

class TwoLineRadioButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = R.attr.radioButtonStyle
) : RadioButtonCompat(context, attributeSet, defStyleAttr) {

    private val titleSpan: TextAppearanceSpan
    private val subtitleSpan: TextAppearanceSpan

    private var titleTextColorSpan: ForegroundColorSpan
    private var subtitleTextColorSpan: ForegroundColorSpan

    var title: String = ""
        set(value) {
            field = value
            renderText()
        }

    var subtitle: String? = null
        set(value) {
            field = value
            renderText()
        }

    var titleTextColor: Int = ContextCompat.getColor(context, R.color.color_secondary_text)
        set(value) {
            field = value
            renderText()
        }

    var subtitleTextColor: Int = ContextCompat.getColor(context, R.color.color_secondary_text)
        set(value) {
            field = value
            renderText()
        }

    init {
        val typedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.TwoLineRadioButton,
            defStyleAttr, 0
        )

        try {
            title = typedArray.getString(R.styleable.TwoLineRadioButton_titleText) ?: ""
            subtitle = typedArray.getString(R.styleable.TwoLineRadioButton_subtitleText)

            titleTextColor = typedArray.getColor(
                R.styleable.TwoLineRadioButton_titleTextColor,
                titleTextColor
            )
            subtitleTextColor = typedArray.getColor(
                R.styleable.TwoLineRadioButton_subtitleTextColor,
                subtitleTextColor
            )

            val titleTextAppearance = typedArray.getResourceId(
                R.styleable.TwoLineRadioButton_titleTextAppearance,
                R.style.TextAppearance_AppCompat_Body1
            )
            titleSpan = TextAppearanceSpan(context, titleTextAppearance)
            titleTextColorSpan = ForegroundColorSpan(titleTextColor)

            val subtitleTextAppearance = typedArray.getResourceId(
                R.styleable.TwoLineRadioButton_subtitleTextAppearance,
                R.style.TextAppearance_AppCompat_Caption
            )
            subtitleSpan = TextAppearanceSpan(context, subtitleTextAppearance)
            subtitleTextColorSpan = ForegroundColorSpan(subtitleTextColor)

        } finally {
            typedArray.recycle()
        }

        renderText()
    }

    private fun renderText() {
        titleTextColorSpan = ForegroundColorSpan(titleTextColor)
        subtitleTextColorSpan = ForegroundColorSpan(subtitleTextColor)

        val textToRender = if (subtitle.isNullOrEmpty()) title
        else "$title\n$subtitle"

        val builder = SpannableStringBuilder(textToRender).apply {
            setSpan(
                titleSpan, 0, title.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            setSpan(
                titleTextColorSpan, 0, title.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )

            subtitle?.also {
                if (it.isNotEmpty()) {
                    setSpan(
                        subtitleSpan, title.length,
                        title.length + it.length + 1, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                    setSpan(
                        subtitleTextColorSpan, title.length,
                        title.length + it.length + 1, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
            }
        }

        text = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P)
            builder.toString()
        else builder
    }
}