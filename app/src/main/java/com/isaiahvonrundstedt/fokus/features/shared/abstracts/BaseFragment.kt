package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.isaiahvonrundstedt.fokus.R

abstract class BaseFragment : Fragment() {

    protected fun buildTransitionOptions(container: View,
                                         name: String = BaseEditor.TRANSITION_ELEMENT_ROOT) =
        ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), container,
            name).toBundle()

    val activityToolbar: MaterialToolbar?
        get() = activity?.findViewById(R.id.toolbar) as? MaterialToolbar

}