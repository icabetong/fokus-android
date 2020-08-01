package com.isaiahvonrundstedt.fokus.features.settings

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import kotlinx.android.synthetic.main.layout_appbar.*

class SettingsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private var controller: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_settings)

        controller = findNavController(R.id.navigationHostFragment)

        val configuration = AppBarConfiguration.Builder()
            .setFallbackOnNavigateUpListener { onNavigateUpOrFinish() }
            .build()
        toolbar.setupWithNavController(controller!!, configuration)
        toolbar.setNavigationIcon(R.drawable.ic_outline_arrow_back_24)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat?,
                                           pref: Preference?): Boolean {
        if (pref?.key == getString(R.string.key_backup_restore))
            controller?.navigate(R.id.mainToBackupRestoreFragment)
        return true
    }

    override fun onSupportNavigateUp(): Boolean =
        Navigation.findNavController(this, R.id.navigationHostFragment).navigateUp()
            || super.onSupportNavigateUp()

    private fun onNavigateUpOrFinish(): Boolean {
        if (!onNavigateUp())
            finish()
        else onNavigateUp()
        return true
    }
}