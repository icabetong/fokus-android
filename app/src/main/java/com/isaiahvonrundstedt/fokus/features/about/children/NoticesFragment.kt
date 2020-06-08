package com.isaiahvonrundstedt.fokus.features.about.children

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BasePreference

class NoticesFragment: BasePreference() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_notices, rootKey)
    }

    override fun onStart() {
        super.onStart()

        findPreference<Preference>(R.string.key_credit_libraries)?.apply {
            setOnPreferenceClickListener {
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.activity_open_source_licenses))
                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                true
            }
        }

        findPreference<Preference>(R.string.key_credit_notification_sound)?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                    soundLink)))
                true
            }
        }

        findPreference<Preference>(R.string.key_credit_launcher_icon)?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                    launcherIconLink)))
                true
            }
        }

    }

    companion object {
        const val launcherIconLink = "https://flaticon.com/authors/freepik"
        const val soundLink = "https://www.zapsplat.com/music/ui-alert-prompt-warm-wooden-mallet-style-notification-tone-generic-11/"
    }
}