package com.isaiahvonrundstedt.fokus.features.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference

class AboutPreference: BasePreference() {

    companion object {
        const val url = "https://github.com/reichsadmiral/fokus/issues/new"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_about, rootKey)
    }

    override fun onStart() {
        super.onStart()

        findPreference<Preference>(R.string.key_report_issue)?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                true
            }
        }

        findPreference<Preference>(R.string.key_version)?.apply {
            summary = BuildConfig.VERSION_NAME
        }
    }
}