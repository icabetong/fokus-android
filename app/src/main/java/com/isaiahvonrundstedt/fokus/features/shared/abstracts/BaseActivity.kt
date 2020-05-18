package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.search.SearchActivity
import com.isaiahvonrundstedt.fokus.features.shared.PreferenceManager

abstract class BaseActivity: AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        when (PreferenceManager(this).theme){
            PreferenceManager.Theme.NEVER ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            PreferenceManager.Theme.ALWAYS ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            PreferenceManager.Theme.AUTOMATIC -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }
        }
    }

    protected fun setPersistentActionBar(toolbar: MaterialToolbar?, @StringRes id: Int?) {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener { finish() }
        supportActionBar?.title = if (id != null) getString(id) else null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            } else -> false
        }
    }

    protected fun showFeedback(view: View, @StringRes id: Int) {
        Snackbar.make(view, id, Snackbar.LENGTH_SHORT).show()
    }

}