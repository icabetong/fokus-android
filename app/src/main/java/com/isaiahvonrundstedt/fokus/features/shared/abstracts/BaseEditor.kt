package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Activity
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val transition = "shared_element_end_root"
    }
}