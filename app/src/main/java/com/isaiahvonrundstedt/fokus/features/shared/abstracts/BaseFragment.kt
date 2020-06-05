package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {

    protected fun startActivityWithTransition(views: Map<String, View>, intent: Intent, requestCode: Int) {
        val list = views.mapNotNull {
            Pair.create(it.value, it.key)
        }.toTypedArray()

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), *list)
        startActivityForResult(intent, requestCode, options.toBundle())
    }

    companion object {
        const val transition = "shared_element_end_root"
    }
}