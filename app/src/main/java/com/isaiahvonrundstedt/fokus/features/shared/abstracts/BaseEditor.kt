package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.os.Bundle
import android.view.ViewGroup
import android.view.Window

abstract class BaseEditor: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

        // Setup shared element transition
        findViewById<ViewGroup>(android.R.id.content).transitionName = transitionName

        super.onCreate(savedInstanceState)
    }

    companion object {
        const val transitionName = "shared_element_end_root"
    }
}