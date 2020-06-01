package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import androidx.annotation.StringRes
import androidx.preference.PreferenceFragmentCompat

abstract class BasePreference: PreferenceFragmentCompat() {

    /**
     *   Function to retrieve the Preference Key
     *   in the string resource
     *   @param id - Resource ID of the String resource
     *   @return the Preference Key in String format
     */
    fun getKey(@StringRes id: Int): String
        = getString(id)

}