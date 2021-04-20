package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

abstract class BasePreference : PreferenceFragmentCompat() {

    fun setPreferenceSummary(key: String, summary: String?) {
        findPreference<Preference>(key)?.summary = summary
    }

}