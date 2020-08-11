package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import kotlinx.android.synthetic.main.layout_appbar.*

abstract class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        when (PreferenceManager(this).theme) {
            PreferenceManager.Theme.LIGHT ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            PreferenceManager.Theme.DARK ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            PreferenceManager.Theme.SYSTEM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }
        }
    }

    private var toolbar: MaterialToolbar? = null

    protected fun setPersistentActionBar(toolbar: MaterialToolbar?) {
        this.toolbar = toolbar

        setSupportActionBar(toolbar)
        this.toolbar?.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun setToolbarTitle(@StringRes id: Int) {
        this.toolbar?.title = getString(id)
    }

}