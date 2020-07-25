package com.isaiahvonrundstedt.fokus.features.core.activities

import android.os.Bundle
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import kotlinx.android.synthetic.main.activity_doki.*
import kotlinx.android.synthetic.main.layout_appbar.*

class DokiActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doki)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_doki)

        dokiContentView.loadContent()
    }

}