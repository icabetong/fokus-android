package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.view.View
import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar

abstract class BasePreference : PreferenceFragmentCompat() {

    fun <T : Preference> findPreference(@StringRes id: Int): T? {
        return findPreference(requireContext().getString(id)) as? T
    }

    protected fun createSnackbar(view: View, @StringRes id: Int): Snackbar {
        return Snackbar.make(view, id, Snackbar.LENGTH_SHORT)
    }

}