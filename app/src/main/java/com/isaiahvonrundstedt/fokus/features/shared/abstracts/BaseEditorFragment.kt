package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.material.transition.platform.MaterialElevationScale
import com.isaiahvonrundstedt.fokus.R

abstract class BaseEditorFragment : BaseFragment() {

    protected val animation: Animation
        get() = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_fade_in)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = TRANSITION_DURATION
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = TRANSITION_DURATION
        }
    }
}