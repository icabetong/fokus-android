package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.extensions.android.getDimension
import me.saket.cascade.CascadePopupMenu

abstract class BaseFragment : Fragment() {

    private val parentDrawer: DrawerLayout?
        get() = getParentView()?.findViewById(R.id.drawerLayout) as? DrawerLayout

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
     *  @returns the view of the outermost fragment
     *  on the nested fragment setup
     */
    private fun getParentView(): View? {
        return parentFragment?.parentFragment?.view
    }

    /**
     *  @param toolbar adds the navigation icon at the start of
     *  the toolbar and registers an onClick callback on it
     */
    protected fun setupNavigation(toolbar: MaterialToolbar) {
        with(toolbar) {
            setNavigationIcon(R.drawable.ic_outline_menu_24)
            setNavigationOnClickListener { triggerNavigationDrawer() }
        }
    }

    /**
     *  This function triggers the drawerLayout in the
     *  outermost layer of the nested fragment setup
     */
    private fun triggerNavigationDrawer() {
        if (parentDrawer?.isDrawerOpen(GravityCompat.START) == true)
            parentDrawer?.closeDrawer(GravityCompat.START)
        else parentDrawer?.openDrawer(GravityCompat.START)
    }

    protected fun setInsets(
        root: View,
        topView: View,
        contentViews: Array<View> = emptyArray(),
        bottomView: View? = null
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val windowInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            topView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = windowInsets.top
            }
            bottomView?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = windowInsets.bottom + view.context.getDimension(R.dimen.container_padding_medium)
            }
            contentViews.forEach { contentView ->
                contentView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = windowInsets.bottom
                }
            }
            WindowInsetsCompat.CONSUMED
        }
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
                    ContextCompat.getColor(requireContext(), R.color.theme_background)
                )
            )
        }

    fun customPopupProvider(context: Context, anchor: View) =
        CascadePopupMenu(context, anchor,
            styler = CascadePopupMenu.Styler(
                background = {
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_cascade_background)
                },

            ))

    companion object {
        const val TRANSITION_DURATION = 300L
        const val TRANSITION_ELEMENT_ROOT = "transition:root:"
    }

}