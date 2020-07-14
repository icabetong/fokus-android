package com.isaiahvonrundstedt.fokus.features.about

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import kotlinx.android.synthetic.main.layout_appbar.*

class AboutActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private var controller: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_about)

        controller = Navigation.findNavController(this, R.id.navigationHostFragment)

        val configuration = AppBarConfiguration.Builder()
            .setFallbackOnNavigateUpListener { onNavigateUpOrFinish() }
            .build()
        toolbar.setupWithNavController(controller!!, configuration)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat?,
                                           pref: Preference?): Boolean {
        if (pref?.key == getString(R.string.key_notices))
            controller?.navigate(R.id.mainToNoticesFragment)
        return true
    }

    override fun onSupportNavigateUp(): Boolean = Navigation.findNavController(this, R.id.navigationHostFragment).navigateUp()
            || super.onSupportNavigateUp()

    private fun onNavigateUpOrFinish(): Boolean {
        if (!onNavigateUp())
            finish()
        else onNavigateUp()
        return true
    }
}