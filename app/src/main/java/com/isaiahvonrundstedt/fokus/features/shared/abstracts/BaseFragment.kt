package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {

    protected fun startEditorWithTransition(view: View, intent: Intent, requestCode: Int) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(),
            view, BaseEditor.transitionName)
        startActivityForResult(intent, requestCode, options.toBundle())
    }

}