package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.navigation.NavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.NavigationSheet

abstract class BaseFragment : Fragment() {

    val activityToolbar: MaterialToolbar?
        get() = activity?.findViewById(R.id.toolbar) as? MaterialToolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = TRANSITION_DURATION
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = TRANSITION_DURATION
        }
    }

    protected fun registerForFragmentResult(keys: Array<String>, listener: FragmentResultListener) {
        keys.forEach {
            childFragmentManager.setFragmentResultListener(it, viewLifecycleOwner, listener)
        }
    }

    protected fun setupNavigation(toolbar: MaterialToolbar, controller: NavController?) {
        toolbar.setNavigationIcon(R.drawable.ic_hero_menu_24)
        toolbar.setNavigationOnClickListener {
            NavigationSheet.show(childFragmentManager)
        }

        childFragmentManager.setFragmentResultListener(NavigationSheet.REQUEST_KEY, viewLifecycleOwner) { _, args ->
            args.getInt(NavigationSheet.EXTRA_DESTINATION).also {
                exitTransition = MaterialFadeThrough().apply {
                    duration = TRANSITION_DURATION
                }
                enterTransition = MaterialFadeThrough().apply {
                    duration = TRANSITION_DURATION
                }

                controller?.navigate(it)
            }
        }
    }

    companion object {
        const val TRANSITION_DURATION = 250L
        const val TRANSITION_ELEMENT_ROOT = "transition:root:"
    }

}