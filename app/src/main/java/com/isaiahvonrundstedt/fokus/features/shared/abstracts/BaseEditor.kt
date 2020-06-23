package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.transition.TransitionInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R

abstract class BaseEditor: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        window.sharedElementEnterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.transition_enter)
        window.sharedElementReturnTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.transition_return)

        super.onCreate(savedInstanceState)
    }

    override fun supportFinishAfterTransition() {
        findViewById<ExtendedFloatingActionButton>(R.id.actionButton).isVisible = false
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

    protected fun createSnackbar(view: View, @StringRes id: Int): Snackbar {
        return Snackbar.make(view, id, Snackbar.LENGTH_SHORT)
    }

    protected val animation: Animation
        get() { return AnimationUtils.loadAnimation(this, R.anim.anim_fade_in) }

    companion object {
        const val RESULT_OK = Activity.RESULT_OK
        const val RESULT_DELETE = 2
    }

}