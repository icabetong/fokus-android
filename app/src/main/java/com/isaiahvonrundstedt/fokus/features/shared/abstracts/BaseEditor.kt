package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.isaiahvonrundstedt.fokus.R

abstract class BaseEditor : BaseActivity() {

    override fun supportFinishAfterTransition() {
        currentFocus?.let {
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
        }
        super.supportFinishAfterTransition()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Inspired from:
    // https://github.com/serbelga/Material-Motion-Samples
    protected fun buildContainerTransform(targetView: View,
                                          transitionDuration: Long = TRANSITION_DEFAULT_DURATION,
                                          withMotion: Boolean = false) =
        MaterialContainerTransform().apply {
            addTarget(targetView)
            setAllContainerColors(MaterialColors.getColor(targetView, R.attr.colorSurface))
            duration = transitionDuration
            fadeMode = MaterialContainerTransform.FADE_MODE_IN
            interpolator = DecelerateInterpolator()
            if (withMotion)
                pathMotion = MaterialArcMotion()
        }

    protected val animation: Animation
        get() {
            return AnimationUtils.loadAnimation(this, R.anim.anim_fade_in)
        }

    companion object {
        const val TRANSITION_ELEMENT_ROOT = "transition:root:"
        const val TRANSITION_SHORT_DURATION = 250L
        const val TRANSITION_DEFAULT_DURATION = 300L
    }

}