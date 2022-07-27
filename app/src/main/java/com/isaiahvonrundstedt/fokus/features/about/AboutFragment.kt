package com.isaiahvonrundstedt.fokus.features.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.isaiahvonrundstedt.fokus.BuildConfig
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import com.isaiahvonrundstedt.fokus.databinding.FragmentAboutBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference

class AboutFragment : BaseFragment() {
    private var _binding: FragmentAboutBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(binding.root, binding.appBarLayout.toolbar)

        with(binding.appBarLayout.toolbar) {
            setTitle(R.string.activity_about)
            setupNavigation(this, R.drawable.ic_outline_arrow_back_24) {
                controller?.navigateUp()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
    }

    companion object {
        const val ABOUT_ISSUE_URL = "https://github.com/icabetong/fokus-android/issues/new"
        const val ABOUT_RELEASE_URL = "https://github.com/icabetong/fokus-android/releases"
        const val ABOUT_DEVELOPER_EMAIL = "isaiahcollins_02@live.com"

        class AboutFragment : BasePreference() {
            private var controller: NavController? = null

            override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                setPreferencesFromResource(R.xml.xml_about_main, rootKey)
            }

            override fun onStart() {
                super.onStart()
                controller = Navigation.findNavController(requireActivity(),
                    R.id.navigationHostFragment)

                findPreference<Preference>(PreferenceManager.PREFERENCE_NOTICES)
                    ?.setOnPreferenceClickListener {
                        controller?.navigate(R.id.navigation_notices)
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