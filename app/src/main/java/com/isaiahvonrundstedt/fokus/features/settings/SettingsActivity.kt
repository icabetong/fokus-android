package com.isaiahvonrundstedt.fokus.features.settings

import android.os.Bundle
import android.view.Menu
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import kotlinx.android.synthetic.main.layout_appbar.*

class SettingsActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setPersistentActionBar(toolbar, R.string.activity_settings)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }
}