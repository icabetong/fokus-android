package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.isaiahvonrundstedt.fokus.R

abstract class BaseFragment : Fragment() {

    protected fun startActivityWithTransition(views: Map<String, View>, intent: Intent,
                                              requestCode: Int) {
        val sharedElements = views.mapNotNull {
            Pair.create(it.value, it.key)
        }.toTypedArray()

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), *sharedElements)
        startActivityForResult(intent, requestCode, options.toBundle())
    }

    val activityToolbar: MaterialToolbar?
        get() = activity?.findViewById(R.id.toolbar) as? MaterialToolbar

}