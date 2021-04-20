package com.isaiahvonrundstedt.fokus.features.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.Preference
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.databinding.ActivityAboutBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference

class AboutActivity : BaseActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPersistentActionBar(binding.appBarLayout.toolbar)
        setToolbarTitle(R.string.activity_about)
    }

    companion object {
        const val ABOUT_ISSUE_URL = "https://github.com/asayah-san/fokus-android/issues/new"
        const val ABOUT_RELEASE_URL = "https://github.com/asayah-san/fokus-android/releases"
        const val ABOUT_DEVELOPER_EMAIL = "isaiahcollins_02@live.com"

        class AboutFragment: BasePreference() {

            override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                setPreferencesFromResource(R.xml.xml_about_main, rootKey)
            }

            override fun onStart() {
                super.onStart()

                findPreference<Preference>(PreferenceManager.PREFERENCE_NOTICES)
                    ?.setOnPreferenceClickListener {
                        startActivity(Intent(context, NoticesActivity::class.java))
                        true
                    }

                findPreference<Preference>(PreferenceManager.PREFERENCE_TRANSLATE)
                    ?.setOnPreferenceClickListener {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("mailto:${ABOUT_DEVELOPER_EMAIL}")
                        }

                        if (intent.resolveActivity(requireContext().packageManager) != null)
                            startActivity(intent)
                        true
                    }

                findPreference<Preference>(PreferenceManager.PREFERENCE_REPORT_ISSUE)
                    ?.setOnPreferenceClickListener {
                        CustomTabsIntent.Builder().build()
                            .launchUrl(requireContext(), Uri.parse(ABOUT_ISSUE_URL))

                        true
                    }

                setPreferenceSummary(PreferenceManager.PREFERENCE_VERSION, BuildConfig.VERSION_NAME)
                findPreference<Preference>(PreferenceManager.PREFERENCE_VERSION)
                    ?.setOnPreferenceClickListener {
                        CustomTabsIntent.Builder().build()
                            .launchUrl(requireContext(), Uri.parse(ABOUT_RELEASE_URL))

                        true
                    }
            }
        }
    }
}