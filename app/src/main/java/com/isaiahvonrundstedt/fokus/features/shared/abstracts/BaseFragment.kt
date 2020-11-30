package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialElevationScale
import com.isaiahvonrundstedt.fokus.R

abstract class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = 300
        }
        returnTransition = MaterialElevationScale(true).apply {
            duration = 300
        }

        super.onViewCreated(view, savedInstanceState)
    }

    protected fun buildTransitionOptions(container: View,
                                         name: String = BaseEditor.TRANSITION_ELEMENT_ROOT) =
        ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), container,
            name).toBundle()

    val activityToolbar: MaterialToolbar?
        get() = activity?.findViewById(R.id.toolbar) as? MaterialToolbar

}