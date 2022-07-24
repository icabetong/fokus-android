package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.isaiahvonrundstedt.fokus.R

abstract class BaseEditorFragment : BaseFragment() {

    protected val animation: Animation
        get() = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_fade_in)

}