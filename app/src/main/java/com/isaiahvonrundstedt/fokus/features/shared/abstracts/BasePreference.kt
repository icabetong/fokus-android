package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

abstract class BasePreference : PreferenceFragmentCompat() {

    fun <T : Preference> findPreference(@StringRes id: Int): T? {
        return findPreference(requireContext().getString(id)) as? T
    }

    fun setPreferenceSummary(@StringRes id: Int, summary: String) {
        findPreference<Preference>(id)?.summary = summary
    }

}