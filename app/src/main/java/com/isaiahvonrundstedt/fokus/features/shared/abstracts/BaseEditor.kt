package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager

abstract class BaseEditor: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

        // Setup shared element transition
        findViewById<ViewGroup>(android.R.id.content).transitionName = transitionName

        super.onCreate(savedInstanceState)
    }

    override fun supportFinishAfterTransition() {
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
        const val transitionName = "shared_element_end_root"
    }
}