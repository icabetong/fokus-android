package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.NavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.bottomsheet.NavigationSheet
import me.saket.cascade.CascadePopupMenu

abstract class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = TRANSITION_DURATION
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = TRANSITION_DURATION
        }
    }

    /**
     * @return returns the Parent Fragment with the Toolbar view
     * useful when dealing with MainFragment.kt then its ChildFragments
     */
    protected fun getParentToolbar(): MaterialToolbar? {
        // see https://stackoverflow.com/a/63200538
        return parentFragment?.parentFragment?.view?.findViewById(R.id.toolbar)
    }

    /**
     * @param view the root view of the fragment
     */
    protected fun hideKeyboardFromCurrentFocus(view: View) {
        if (view is ViewGroup)
            findCurrentFocus(view)
    }

    /**
     * @param viewGroup check if any of its children has focus then
     * hide the keyboard
     */
    private fun findCurrentFocus(viewGroup: ViewGroup) {
        viewGroup.children.forEach {
            if (it is ViewGroup)
            // If the current children is an instance of
            // a ViewGroup, then iterate its children too.
                findCurrentFocus(it)
            else {
                if (it.hasFocus()) {
                    hideKeyboardFromView(it)
                    return
                }
            }
        }
    }

    /**
     * @param view the view which has the focus
     * then get the inputMethodManager service then
     * try to hide the soft keyboard with the view's
     * window token
     */
    private fun hideKeyboardFromView(view: View) {
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).run {
            hideSoftInputFromWindow(view.windowToken, 0)
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

        childFragmentManager.setFragmentResultListener(
            NavigationSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, args ->
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

    fun buildContainerTransform(@IdRes id: Int = R.id.navigationHostFragment) =
        MaterialContainerTransform().apply {
            drawingViewId = id
            duration = TRANSITION_DURATION
            scrimColor = Color.TRANSPARENT
            fadeMode = MaterialContainerTransform.FADE_MODE_OUT
            interpolator = FastOutSlowInInterpolator()
            setAllContainerColors(
                MaterialColors.getColor(
                    requireContext(), R.attr.colorSurface,
                    ContextCompat.getColor(requireContext(), R.color.color_surface)
                )
            )
        }

    fun customPopupProvider(context: Context, anchor: View) =
        CascadePopupMenu(context, anchor,
            styler = CascadePopupMenu.Styler(
                background = {
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_cascade_background)
                }
            ))

    companion object {
        const val TRANSITION_DURATION = 300L
        const val TRANSITION_ELEMENT_ROOT = "transition:root:"
    }

}