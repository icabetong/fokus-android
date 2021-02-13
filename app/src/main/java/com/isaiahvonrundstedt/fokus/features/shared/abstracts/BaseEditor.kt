package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.graphics.Color
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialContainerTransform
import com.isaiahvonrundstedt.fokus.R

abstract class BaseEditor: BaseFragment() {

    protected val animation: Animation
        get() = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_fade_in)

    fun getTransition(@IdRes id: Int = R.id.navigationHostFragment): MaterialContainerTransform {
        return MaterialContainerTransform().apply {
            drawingViewId = id
            duration = TRANSITION_DURATION
            scrimColor = Color.TRANSPARENT
            fadeMode = MaterialContainerTransform.FADE_MODE_IN
            interpolator = DecelerateInterpolator()
            setAllContainerColors(MaterialColors.getColor(requireContext(), R.attr.colorSurface,
                ContextCompat.getColor(requireContext(), R.color.color_surface)))
        }
    }

}