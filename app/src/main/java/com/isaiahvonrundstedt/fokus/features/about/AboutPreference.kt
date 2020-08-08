package com.isaiahvonrundstedt.fokus.features.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.Preference
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference

class AboutPreference : BasePreference() {

    companion object {
        const val ABOUT_REPOSITORY_URL = "https://github.com/reichsadmiral/fokus/issues/new"
        const val ABOUT_DEVELOPER_EMAIL = "isaiahcollins_02@live.com"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_about_main, rootKey)
    }

    override fun onStart() {
        super.onStart()

        findPreference<Preference>(R.string.key_translate)?.apply {
            setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("mailto:$ABOUT_DEVELOPER_EMAIL")
                }
                startActivity(intent)
                true
            }
        }

        findPreference<Preference>(R.string.key_report_issue)?.apply {
            setOnPreferenceClickListener {
                val browserIntent = CustomTabsIntent.Builder().build()
                browserIntent.launchUrl(requireContext(), Uri.parse(ABOUT_REPOSITORY_URL))

                true
            }
        }

        findPreference<Preference>(R.string.key_version)?.apply {
            summary = BuildConfig.VERSION_NAME
        }
    }
}